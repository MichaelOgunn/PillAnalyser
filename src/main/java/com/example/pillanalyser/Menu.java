package com.example.pillanalyser;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.Slider;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.*;

//import static com.sun.tools.javac.resources.CompilerProperties.Fragments.Bound;

public class Menu {
    @FXML
    private ImageView originalImage;


    @FXML
    private ImageView processedImage;


    private Image picture;
    @FXML
     Slider thresholdSlider,brightnessSlider;
    @FXML
    Slider hueSlider,saturationSlider;

    private Map<Integer, Color> colorMap = new HashMap<>();
    private HashMap<Integer,List<Integer>> allPillsPixels;
    int[] pixelArray;
    Map<Integer, Color> colorRootsMap = new HashMap<>();

    private PillApi pills;


    private UnionFind unionFind;
    private  ColourFinder colourFinder;
    @FXML
    private RadioButton selectpill;
    @FXML
    private RadioButton findpill;
    @FXML
    private Slider sizeSlider;


    public void imgLoader(ActionEvent event) {
        unionFind = new UnionFind();
        colourFinder = new ColourFinder();
        this.allPillsPixels = new HashMap<>();
        this.pills = new PillApi();
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files",
                "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile!= null) {
            picture = new Image(selectedFile.toURI().toString(),originalImage.getFitWidth(),originalImage.getFitHeight(),false,false);
//            picture = new Image(selectedFile.toURI().toString());
            originalImage.setImage(picture);

        }
        int imageWidth = (int) picture.getWidth();
        int imageHeight = (int) picture.getHeight();
        pixelArray = new int[imageWidth * imageHeight];
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                int index = y * imageWidth + x;
                pixelArray[index] = index;
            }
        }

        showImageDetails(selectedFile);

    }
    public void initialize() {


        hueSlider.valueProperty().addListener((observable, oldValue, newValue) ->{
            convertImageToBandW();
            disJoinSelection();
        });
        saturationSlider.valueProperty().addListener((observable, oldValue, newValue) ->{
            convertImageToBandW();
            disJoinSelection();
        });
        brightnessSlider.valueProperty().addListener((observable, oldValue, newValue) ->{
            convertImageToBandW();
            disJoinSelection();
        });


    }

    public void showImageDetails(File selectedFile) {
        if (selectedFile!= null) {
            originalImage.setImage(new Image(selectedFile.toURI().toString()));
        }
    }

    public void convertImageToBandW() {
        Image img = picture;
        if (img != null) {
            PixelReader pixelReader = img.getPixelReader();
            WritableImage bwImage = new WritableImage((int) img.getWidth(), (int) img.getHeight());
            PixelWriter pixelWriter = bwImage.getPixelWriter();
            pixelArray = new int[(int) img.getWidth() * (int) img.getHeight()];
             // Set max value of the hue slider to 360 (hue value range)
            hueSlider.setMin(0);
            hueSlider.setMax(360);
            saturationSlider.setMin(0);
            saturationSlider.setMax(1); // Set max value of the saturation slider to 1 (saturation range)
            brightnessSlider.setMin(0);
            brightnessSlider.setMax(1); // Set max value of the brightness slider to 1 (brightness range)
            double hueThreshold = hueSlider.getValue();
            double saturationThreshold = saturationSlider.getValue();
            double brightnessThreshold = brightnessSlider.getValue();
            List<Color> pillColors = new ArrayList<>();
            for (Pill pill : pills.getPills()) {
                pillColors.add(pill.getColor());
            }
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int index = y * (int) img.getWidth() + x;
                    Color color = pixelReader.getColor(x, y);
                    boolean isPillColor = false;
                    for (Color pillcolor : pillColors){
                        if (colourFinder.areColorsSimilar(color,pillcolor,hueSlider.getValue(),saturationSlider.getValue(),
                                brightnessSlider.getValue())){
                            isPillColor = true;
                        }
                    }
                    if (isPillColor) {
                        pixelWriter.setColor(x, y, Color.WHITE);
                        pixelArray[index] = index;
                    } else {
                        pixelWriter.setColor(x, y, Color.BLACK);
                        pixelArray[index] = -1;
                    }
                }
            }
            processedImage.setImage(bwImage);
        }

    }

    public void enablePillSelection() {
        Image img = picture;

        if (img != null) {
            if (selectpill.isSelected()) {
                findpill.setSelected(false);
                originalImage.setOnMouseClicked(event -> {
                    double mouseX = event.getX();
                    double mouseY = event.getY();
                    if (mouseX >= 0 && mouseX < img.getWidth() &&
                            mouseY >= 0 && mouseY < img.getHeight()) {
                        PixelReader pixelReader = img.getPixelReader();
                        if (pixelReader != null) {
                            Color currentPixelColor = pixelReader.getColor((int) event.getX(), (int) event.getY());
                            pills.addPill(new Pill(currentPixelColor));
                        }
                        System.out.println("pill color selected");
                    }
                });

            }

            if(findpill.isSelected()) {
//                PixelReader pixelReader = img.getPixelReader();
//                int imageWidth = (int) img.getWidth();
//                int imageHeight = (int) img.getHeight();
//
//                // Iterate over all pixels in the image
//                for (int y = 0; y < imageHeight; y++) {
//                    for (int x = 0; x < imageWidth; x++) {
//                        int index = y * imageWidth + x;
//                        // Get the color of the current pixel
//                        Color color = pixelReader.getColor(x, y);
//
//                        // Store the color in the color map
//                        colorMap.put(index, color);
//                    }

//                }
                selectpill.setSelected(false);
                originalImage.setOnMouseClicked(this::selectPill);

                // After storing all colors, proceed to select the pill

            }
        }
    }

    /**
     * The `disJoinSelection` method processes an image by checking neighboring pixels and performing unions based on a
     * threshold condition.
     */
    public void disJoinSelection() {
        Image img = picture;
        if (img != null) {
            int width = (int)img.getWidth();
            int height = (int)img.getHeight();


            for (int y= 0; y< height; y++) {
                for (int x = 0; x < width; x++) {
                    int index = y * width + x;
                    if (pixelArray[index]!= -1) {
                        Color currentPixelColor = colourFinder.getPixelColour(img, x, y);
                        double hThr = hueSlider.getValue();
                        double sThr= saturationSlider.getValue();
                        double bThr= brightnessSlider.getValue();
//                        colorMap.put(String.format("%d,%d", x, y), currentPixelColor);

                        // The condition `if (x+1 < width && y+1 < height && pixelArray[index+1]!= -1 )` is checking if the
                        // pixel to the right of the current pixel (in the same row) is within the image boundaries and if
                        // it is not part of the background (pixelArray[index+1] != -1). This condition is used in the
                        // `disJoinSelection` method to determine if the current pixel should be unioned with its right
                        // neighbor pixel based on the threshold set in the image processing.
                        if (x+1 < width && y+1 < height && pixelArray[index+1]!= -1 ){
                            Color neighborPixelColor = colourFinder.getPixelColour(img, x+1, y);
                            if (colourFinder.areColorsSimilar(currentPixelColor, neighborPixelColor,hThr,sThr,bThr)) {
                                unionFind.union(pixelArray, index, index + 1);
                            }
                        }
                        if (index + width <pixelArray.length && pixelArray[index + width]!= -1) {
                            Color bottomNeighborPixelColor = colourFinder.getPixelColour(img, x, y+1);
                            if (colourFinder.areColorsSimilar(currentPixelColor,bottomNeighborPixelColor,hThr,sThr,bThr)) {
                                unionFind.union(pixelArray, index, index + width);
                            }
                        }
                    }

                }
            }
            displayDSAsText();
        }
    }






    public void selectPill(MouseEvent event) {
        reset();
        makeSet();
        Image img = picture;

        if (img != null) {
            double mouseX = event.getX();
            double mouseY = event.getY();

            // Convert mouse coordinates to image coordinates
            double imageX = mouseX * (img.getWidth() / originalImage.getFitWidth());
            double imageY = mouseY * (img.getHeight() / originalImage.getFitHeight());

            int pixelIndex = (int) (imageY * img.getWidth() + imageX);

            // Find the root of the clicked pixel
            int origRoot = unionFind.find(pixelArray, pixelIndex);

            if (origRoot != -1) {
                for (int root : colorMap.keySet()) {
                    if (colourFinder.areColorsSimilar(colorMap.get(root), colorMap.get(origRoot),hueSlider.getValue(),saturationSlider.getValue(),
                            brightnessSlider.getValue())) {

                        List<Integer> pixelsInSet = unionFind.getAllInSet(pixelArray, root);

                        // Find the bounding box of the set
                        double minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
                        double maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

                        for (int pixel : pixelsInSet) {
                            int x = pixel % (int) img.getWidth();
                            int y = pixel / (int) img.getWidth();
                            minX = Math.min(minX, x);
                            minY = Math.min(minY, y);
                            maxX = Math.max(maxX, x);
                            maxY = Math.max(maxY, y);
                        }

                        drawRectangleAroundPixels(pixelsInSet);


                    }
                }
            }
        }
}

    public void drawRectangleAroundPixels(List<Integer> pixelsInSet) {
        Image img = picture;
        // Find the bounding box of the set
        double minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        double maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

        // The above code is iterating over a set of pixels represented by integers and calculating the minimum and maximum
        // x and y coordinates of those pixels within an image. It is using the modulo operator to calculate the x
        // coordinate and integer division to calculate the y coordinate based on the image width. The minX, minY, maxX,
        // and maxY variables are updated to keep track of the bounding box that encloses all the pixels in the set.
        for (int pixel : pixelsInSet) {
            int x = pixel % (int) img.getWidth();
            int y = pixel / (int) img.getWidth();
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
        }

        double width = maxX - minX + 1;
        double height = maxY - minY + 1;

        // Create a rectangle based on the bounding box
        Rectangle rectangle = new Rectangle(minX + originalImage.getLayoutX(), minY + originalImage.getLayoutY(), width, height);
        rectangle.setFill(Color.TRANSPARENT);
        rectangle.setStroke(Color.BLUE);
        rectangle.setStrokeWidth(1);

        // Add the rectangle to the pane
        ((Pane) originalImage.getParent()).getChildren().add(rectangle);
    }
    public void randomiseColourNull(ActionEvent event) {
        Image img = processedImage.getImage();
        int imageWidth = (int) img.getWidth();
        int imageHeight = (int) img.getHeight();
        WritableImage randomColor = new WritableImage(imageWidth, imageHeight);
        PixelReader pixelReader = randomColor.getPixelReader();
        PixelWriter randomPixelWriter = randomColor.getPixelWriter();
        for (int y = 0; y < imageHeight; y++) {
            for (int x = 0; x < imageWidth; x++) {
                Color color = img.getPixelReader().getColor(x, y);
                if (color.equals(Color.WHITE)) {
                    Color color1 = getRandomColor();
                    randomPixelWriter.setColor(x, y, color1);
                }
                else{
                    randomPixelWriter.setColor(x, y, Color.BLACK);
                }
            }
            processedImage.setImage(randomColor);
        }
    }
    public void randomiseColour(ActionEvent event) {
        Image img = picture;
        int imageWidth = (int) img.getWidth();
        int imageHeight = (int) img.getHeight();
        WritableImage randomColor = new WritableImage(imageWidth, imageHeight);
        this.colorRootsMap = new HashMap<>();
        for (int i = 0; i < pixelArray.length; i++) {
            int root = unionFind.find(pixelArray, i);
            if (root!= -1&& !colorRootsMap.containsKey(root)) {
                colorRootsMap.put(root, getRandomColor());

            }
        }
        for (int i = 0; i < pixelArray.length; i++) {
            int x =i % (int) img.getWidth();
            int y =i / (int) img.getWidth();
            int root = unionFind.find(pixelArray, i);
            if (root!= -1) {
                Color color = colorRootsMap.get(root);
                randomColor.getPixelWriter().setColor(x, y, color);
            }else {
                randomColor.getPixelWriter().setColor(x, y, Color.BLACK);
            }

        }
        processedImage.setImage(randomColor);

    }
    public Color getRandomColor() {
        int red = (int)(Math.random()*256);
        int green = (int)(Math.random()*256);
        int blue = (int)(Math.random()*256);
        return  Color.rgb(red, green, blue);
    }






    public void reset(){
        ((Pane) originalImage.getParent()).getChildren().removeIf(r -> r instanceof Rectangle);
    }
    public void makeSet() {

        // Iterate over each pixel in the pixel array
        for (int i = 0; i < pixelArray.length; i++) {
            int root = unionFind.find(pixelArray, i);
            if (root != -1) {
                // Get the color associated with the root
//                Color color = colorMap.get(root);
                if (!colorMap.containsKey(root)) {
                    PixelReader pixelReader = picture.getPixelReader();
                    Color color = pixelReader.getColor(i % (int) picture.getWidth(), i / (int) picture.getWidth());
                    colorMap.put(root, color);
                }
            }
        }
        System.out.println(colorMap);
    }










    public void displayDSAsText(){
        int width= (int) picture.getWidth();
        for(int i=0;i<pixelArray.length;i++)
            System.out.print(UnionFind.find(pixelArray,i) + ((i+1)%width==0 ? "\n" : " "));

    }

}
