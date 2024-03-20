package com.example.pillanalyser;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

//import static com.sun.tools.javac.resources.CompilerProperties.Fragments.Bound;

public class Menu {
    @FXML
    private ImageView originalImage;
    @FXML
    private RadioButton twoColoredPill;
    @FXML
    private ColorPicker colorPicker1;
    @FXML
    private ColorPicker colorPicker2;

    private Color firstSampleColor;

    private Color secondSampleColor;
    boolean firstColor = true;

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
    @FXML
    Label statusLabel ;
    @FXML
    private TextField pillsName;



    private UnionFind unionFind;
    private  ColourFinder colourFinder;
    @FXML
    private RadioButton selectpill;
    @FXML
    private RadioButton findpill;
    @FXML
    private Slider sizeSlider, maxSizeSlider;
    private int counter = 1;
    @FXML
    private TextArea pillDetails;
    @FXML
    private TextField rootDiffrence;

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
        sizeSlider.setMin(1);
        sizeSlider.setMax(100);
        hueSlider.setMin(0);
        hueSlider.setMax(180);
        saturationSlider.setMin(0);
        saturationSlider.setMax(1);
        brightnessSlider.setMin(-1);
        brightnessSlider.setMax(1);


        thresholdSlider.valueProperty().addListener((observable, oldValue, newValue) ->{
            convertImageToBandW();
            disJoinSelection();
        });
        hueSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
           convertPillToBandW(firstSampleColor,hueSlider.getValue(),saturationSlider.getValue(),brightnessSlider.getValue());
           disJoinSelection();
        });
        saturationSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            convertPillToBandW(firstSampleColor,hueSlider.getValue(),saturationSlider.getValue(),brightnessSlider.getValue());
            disJoinSelection();
        });
        brightnessSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            convertPillToBandW(firstSampleColor,hueSlider.getValue(),saturationSlider.getValue(),brightnessSlider.getValue());
            disJoinSelection();
        });

        sizeSlider .valueProperty().addListener((observable, oldValue, newValue) -> {
            applySizeFilter();
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
            thresholdSlider.setMin(0);
            thresholdSlider.setMax(1);
            double thr = thresholdSlider.getValue();
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
                        if (color.getRed() < thr && color.getBlue() < thr && color.getGreen() < thr) {
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
    public void convertPillToBandW(Color sampledColor, double hueThreshold, double saturationThreshold, double brightnessThreshold) {
        Image img = picture;
        if (img != null) {

            PixelReader pixelReader = img.getPixelReader();
            WritableImage bwImage = new WritableImage((int) img.getWidth(), (int) img.getHeight());
            PixelWriter pixelWriter = bwImage.getPixelWriter();
            pixelArray = new int[(int) img.getWidth() * (int) img.getHeight()];

            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int index = y * (int) img.getWidth() + x;
                    Color pixelColor = pixelReader.getColor(x, y);
                    // Here we check if the current pixel is similar to the selected pill color
                    if (colourFinder.isColorSimilar(pixelColor, sampledColor, hueSlider.getValue(),saturationSlider.getValue(),brightnessSlider.getValue())) {
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


    public void enablePillSelection(MouseEvent event) {
        Image img = picture;

        if (img != null) {
            if (selectpill.isSelected() && !findpill.isSelected()) {
                findpill.setSelected(false);
                double mouseX = event.getX();
                double mouseY = event.getY();
                if (mouseX >= 0 && mouseX < img.getWidth() &&
                        mouseY >= 0 && mouseY < img.getHeight()) {
                    PixelReader pixelReader = img.getPixelReader();
                    if (pixelReader != null) {

                        Color averageColor = sampleAverageColor(picture, (int) mouseX, (int) mouseY, 3); // Sample size of 5x5

                        pills.addPill(new Pill(averageColor));
                    }
                    System.out.println("pill color selected");
                }
            }


        }
        if (findpill.isSelected() && !selectpill.isSelected()) {


            int x = (int) event.getX();
            int y = (int) event.getY();

            PixelReader pixelReader = img.getPixelReader();
            Color color = pixelReader.getColor(x, y);
            firstSampleColor = color;
            colorPicker1.setValue(firstSampleColor);
            selectPill(event);
            System.out.println("pill selected");


        }
        if (twoColoredPill.isSelected()) {
            double mouseX = event.getX();
            double mouseY = event.getY();

            if (firstColor) {
                firstSampleColor = sampleAverageColor(picture, (int) mouseX, (int) mouseY, 3);
                colorPicker1.setValue(firstSampleColor);

                firstColor = false;
            } else {
                secondSampleColor = sampleAverageColor(picture, (int) mouseX, (int) mouseY, 3);
                colorPicker2.setValue(secondSampleColor);
                firstColor = true;
            }
            pills.addPill(new MulticoloredPill(firstSampleColor, secondSampleColor));
        }
    }
    public void make2Pill(ActionEvent event){
        process2ColoredPill(colorPicker1.getValue(),colorPicker2.getValue(),thresholdSlider.getValue());
    }


    public void process2ColoredPill(Color firstSampleColor, Color secondSampleColor, double value) {
        PixelReader pixelReader = picture.getPixelReader();
        int width = (int) picture.getWidth();
        int height = (int) picture.getHeight();

        List<Integer> firstColorPixels = new ArrayList<>();
        List<Integer> secondColorPixels = new ArrayList<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y * width + x;
                Color color = pixelReader.getColor(x, y);
                if (colourFinder.areColorsSimilarForSelection(color, firstSampleColor, value)) {
                    firstColorPixels.add(index);
                } else if (colourFinder.areColorsSimilarForSelection(color, secondSampleColor, value)) {
                    secondColorPixels.add(index);
                }


            }
        }
        List<Set<Integer>> mergedSets = mergePixelSetsBasedOnProximity(firstColorPixels, secondColorPixels);
        for (Set<Integer> mergedSet : mergedSets) {
            drawRectangleAroundPixels(new ArrayList<>(mergedSet));
        }
    }

    private List<Set<Integer>> mergePixelSetsBasedOnProximity(List<Integer> firstColorPixels, List<Integer> secondColorPixels) {
        List<Set<Integer>> mergedSets = new ArrayList<>();
        for (int i = 0; i < firstColorPixels.size(); i++) {
            for (int j = 0; j < secondColorPixels.size(); j++) {
                int firstColorIndex = firstColorPixels.get(i);
                int secondColorIndex = secondColorPixels.get(j);
                statusLabel.setText(String.valueOf(firstColorIndex - secondColorIndex ));

                int diff = secondColorIndex - firstColorIndex;

                // The above code is checking if the absolute difference between the values of `firstColorIndex` and
                // `secondColorIndex` is less than or equal to 1. If the condition is true, then the code inside the if
                // block (represented by `
                if (Math.abs(firstColorIndex - secondColorIndex) <= diff) {
                    Set<Integer> firstSet = new HashSet<>();
                    firstSet.add(firstColorIndex);
                    Set<Integer> secondSet = new HashSet<>();
                    secondSet.add(secondColorIndex);
                    // The above code is creating a new set called `mergedSets` and adding a new set that is a copy of the
                    // elements in `firstSet` using Java streams and Collectors.
                    mergedSets.add(firstSet.stream().collect(Collectors.toSet()));
                    mergedSets.add(secondSet.stream().collect(Collectors.toSet()));
                }
            }
        }
        return mergedSets;
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
            if (findpill.isSelected()){
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++) {
                        int index = y * width + x;
                        if (pixelArray[index] != -1) {
                            Color currentPixelColor = colorPicker1.getValue();
                            if (x + 1 < width && y + 1 < height && pixelArray[index + 1] != -1) {
                                Color neighborPixelColor = colourFinder.getPixelColour(img, x + 1, y);
                                if (colourFinder.isColorSimilar(currentPixelColor, neighborPixelColor, hueSlider.getValue(),saturationSlider.getValue(), brightnessSlider.getValue())) {
                                    unionFind.union(pixelArray, index, index + 1);
                                }
                            }
                            if (index + width <pixelArray.length && pixelArray[index + width]!= -1) {
                                Color bottomNeighborPixelColor = colourFinder.getPixelColour(img, x, y+1);
                                if (colourFinder.isColorSimilar(currentPixelColor, bottomNeighborPixelColor, hueSlider.getValue(),saturationSlider.getValue(), brightnessSlider.getValue())) {
                                    unionFind.union(pixelArray, index, index + width);
                                }
                            }
                        }
                    }
                }
            }


            for (int y= 0; y< height; y++) {
                for (int x = 0; x < width; x++) {
                    int index = y * width + x;
                    if (pixelArray[index]!= -1) {
                        Color currentPixelColor = colourFinder.getPixelColour(img, x, y);

//                        colorMap.put(String.format("%d,%d", x, y), currentPixelColor);

                        // The condition `if (x+1 < width && y+1 < height && pixelArray[index+1]!= -1 )` is checking if the
                        // pixel to the right of the current pixel (in the same row) is within the image boundaries and if
                        // it is not part of the background (pixelArray[index+1] != -1). This condition is used in the
                        // `disJoinSelection` method to determine if the current pixel should be unioned with its right
                        // neighbor pixel based on the threshold set in the image processing.
                        if (x+1 < width && y+1 < height && pixelArray[index+1]!= -1 ){
                            Color neighborPixelColor = colourFinder.getPixelColour(img, x+1, y);
                            if (colourFinder.areColorsSimilar4S(currentPixelColor, neighborPixelColor,thresholdSlider.getValue())) {
                                unionFind.union(pixelArray, index, index + 1);
                            }
                        }
                        if (index + width <pixelArray.length && pixelArray[index + width]!= -1) {
                            Color bottomNeighborPixelColor = colourFinder.getPixelColour(img, x, y+1);
                            if (colourFinder.areColorsSimilar4S(currentPixelColor,bottomNeighborPixelColor,thresholdSlider.getValue())) {
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
                ArrayList<Integer> sortedRoots=new ArrayList<>(colorMap.keySet());
                Collections.sort(sortedRoots);
                for (int root : sortedRoots) { //colorMap.keySet()) {
                    if (colourFinder.isColorSimilar(colorMap.get(root), colorMap.get(origRoot), hueSlider.getValue(),saturationSlider.getValue(), brightnessSlider.getValue())) {

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
    public void applySizeFilter() {
        // Example thresholds, you may make these user-adjustable via sliders
        int minPillSize = (int) sizeSlider.getValue();
        int maxPillSize = (int) maxSizeSlider.getValue();

        // Temporary map to hold the size of each root
        Map<Integer, Integer> rootSizes = new HashMap<>();

        // Count the size (number of pixels) of each root
        for (int i = 0; i < pixelArray.length; i++) {
            int root = unionFind.find(pixelArray, i);
            if (root != -1) {
                rootSizes.put(root, rootSizes.getOrDefault(root, 0) + 1);
            }
        }

        // Filter out roots (sets) that are outside the size thresholds
        for (Map.Entry<Integer, Integer> entry : rootSizes.entrySet()) {
            if (entry.getValue() < minPillSize || entry.getValue() > maxPillSize) {
                // This root is considered noise, so we can mark its pixels as black
                for (int i = 0; i < pixelArray.length; i++) {
                    if (unionFind.find(pixelArray, i) == entry.getKey()) {
                        pixelArray[i] = -1; // Set to black
                    }
                }
            }
        }

        // Re-draw the image with the noise removed
        redrawImage();
    }
    public void redrawImage() {
        Image img = picture;
        if (img != null) {
            WritableImage filteredImage = new WritableImage((int) img.getWidth(), (int) img.getHeight());
            PixelWriter pixelWriter = filteredImage.getPixelWriter();

            // Iterate over the pixelArray to set the colors in the new image
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int index = y * (int) img.getWidth() + x;
                    if (pixelArray[index] != -1) { // If not marked as noise
                        pixelWriter.setColor(x, y, Color.WHITE);
                    } else {
                        pixelWriter.setColor(x, y, Color.BLACK);
                    }
                }
            }

            // Update the processed image view with the new image
            processedImage.setImage(filteredImage);
        }
    }





    public void drawRectangleAroundPixels(List<Integer> pixelsInSet) {
        System.out.println("hi" +counter);
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

        Label label = new Label(String.valueOf(counter));
        label.setLayoutX(minX + originalImage.getLayoutX());
        label.setLayoutY(minY + originalImage.getLayoutY());
        ((Pane) originalImage.getParent()).getChildren().add(label);

        // Increment the counter for the next pill
        if( width >2 && height>2 ){counter++;}

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
        int pillPixelCount = 0;
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
                pillPixelCount++;
            }else {
                randomColor.getPixelWriter().setColor(x, y, Color.BLACK);
            }

        }
        processedImage.setImage(randomColor);

//        for (int i = 0; i < imageWidth; i++) {
//            for (int j = 0; j < imageWidth; j++) {
//                Color color = img.getPixelReader().getColor(i, j);
//                if (color.equals(Color.BLACK)) {
//                    pillPixelCount++;
//                }
//
//
//            }
//
//        }

        pillDetails.setText("Pill Name:"+pillsName.getText() +"\n" +"The total number of pills are "
                + colorRootsMap.size() + "\n" + "The size of pills are " + pillPixelCount);

    }
    public void pillColor(ActionEvent event) {
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
                Color color = colorPicker1.getValue();
                randomColor.getPixelWriter().setColor(x, y, color);
            }else {
                randomColor.getPixelWriter().setColor(x, y, Color.BLACK);
            }
            processedImage.setImage(randomColor);
        }
    }
    public Color getRandomColor() {
        int red = (int)(Math.random()*256);
        int green = (int)(Math.random()*256);
        int blue = (int)(Math.random()*256);
        return  Color.rgb(red, green, blue);
    }
    private Color sampleAverageColor(Image img, int clickX, int clickY, int sampleSize) {
        PixelReader reader = img.getPixelReader();
        int startX = Math.max(clickX - sampleSize / 2, 0);
        int startY = Math.max(clickY - sampleSize / 2, 0);
        int endX = Math.min(clickX + sampleSize / 2, (int) img.getWidth());
        int endY = Math.min(clickY + sampleSize / 2, (int) img.getHeight());

        long totalR = 0, totalG = 0, totalB = 0;
        int pixelCount = 0;

        for (int x = startX; x < endX; x++) {
            for (int y = startY; y < endY; y++) {
                Color color = reader.getColor(x, y);
                totalR += (long) (color.getRed() * 255);
                totalG += (long) (color.getGreen() * 255);
                totalB += (long) (color.getBlue() * 255);
                pixelCount++;
            }
        }

        if (pixelCount == 0) {
            return Color.BLACK; // Fallback color if sample size is zero
        }

        // Calculate average color
        int avgR = (int) (totalR / pixelCount);
        int avgG = (int) (totalG / pixelCount);
        int avgB = (int) (totalB / pixelCount);

        return Color.rgb(avgR, avgG, avgB);
    }







    public void reset(){
        counter = 1;
        ((Pane) originalImage.getParent()).getChildren().removeIf(r -> r instanceof Rectangle);
        ((Pane) originalImage.getParent()).getChildren().removeIf(r -> r instanceof Label);


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
                    Color color = pixelReader.getColor(i % (int) picture.getWidth(),
                            i / (int) picture.getWidth());
                    colorMap.put(root, color);
                }
            }
        }
        System.out.println(colorMap);
    }











    public void displayDSAsText(){
        int width= (int) picture.getWidth();
        for(int i=0;i<pixelArray.length;i++)
            // The code is printing the result of calling the `find` method of the `UnionFind` class on the `pixelArray` at
            // index `i`. It then checks if `(i+1)%width` is equal to 0, and if so, it prints a new line character `\n`,
            // otherwise it prints a space character. This code is likely part of a loop that iterates over elements in a
            // 2D array represented as a 1D array (`pixelArray`) and prints the results of the `find` method with
            // appropriate formatting.
            System.out.print(UnionFind.find(pixelArray,i) + ((i+1)%width==0 ? "\n" : " "));

    }


}
