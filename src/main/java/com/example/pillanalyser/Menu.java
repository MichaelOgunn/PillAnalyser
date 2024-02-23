package com.example.pillanalyser;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.attribute.AclEntry;
import java.util.*;

//import static com.sun.tools.javac.resources.CompilerProperties.Fragments.Bound;

public class Menu {
    @FXML
    private ImageView originalImage;


    @FXML
    private ImageView processedImage;
    @FXML
    private Pane processedImagePane;

    @FXML
    private Image originalPicture;
    @FXML
    private Slider thresholdSlider;

    private Map<String, Color> colorMap = new HashMap<>();
    private HashMap<Integer,List<Integer>> allPillsPixels;
    int[] pixelArray;

    private UnionFind unionFind;

    public void imgLoader(ActionEvent event) {
        unionFind = new UnionFind();
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files",
                "*.png", "*.jpg", "*.jpeg"));
        File selectedFile = fileChooser.showOpenDialog(null);
        if (selectedFile!= null) {
            originalPicture = new Image(selectedFile.toURI().toString());
            originalImage.setImage(originalPicture);
        }
        int imageWidth = (int) originalPicture.getWidth();
        int imageHeight = (int) originalPicture.getHeight();
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

        thresholdSlider.valueProperty().addListener((observable, oldValue, newValue) ->{
            convertImageToBandW();
        });

    }
    public void showImageDetails(File selectedFile) {
        if (selectedFile!= null) {
            originalImage.setImage(new Image(selectedFile.toURI().toString()));
        }
    }
    public void convertImageToBandW() {
        Image img = originalImage.getImage();
        if (img != null) {
            PixelReader pixelReader = img.getPixelReader();
            WritableImage bwImage = new WritableImage((int) img.getWidth(), (int) img.getHeight());
            PixelWriter pixelWriter = bwImage.getPixelWriter();
            pixelArray = new int[(int) img.getWidth() * (int) img.getHeight()];
            thresholdSlider.setMin(0);
            thresholdSlider.setMax(1);
            double thr = thresholdSlider.getValue();// Assuming thresholdSlider is defined elsewhere
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    int index = y * (int) img.getWidth() + x;
                    Color color = pixelReader.getColor(x, y);
                    if (color.getRed() < thr && color.getBlue() < thr && color.getGreen() < thr) {
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
        Image img = originalImage.getImage();

            if (img != null) {
                double mouseX = event.getX();
                double mouseY = event.getY();

                // Convert mouse coordinates to image coordinates
                double imageX = mouseX * (img.getWidth() / originalImage.getFitWidth());
                double imageY = mouseY * (img.getHeight() / originalImage.getFitHeight());

                // Get the color of the pixel at the clicked coordinates
                PixelReader pixelReader = img.getPixelReader();
                Color sampledColor = pixelReader.getColor((int) imageX, (int) imageY);




//                recordSelectedPill(sampledColor, imageX, imageY);// Implement this method
//                processPillSelection(imageX, imageY);
                displayPillsSelected();


            }

    }
    /**
     * The `disJoinSelection` method processes an image by checking neighboring pixels and performing unions based on a
     * threshold condition.
     *
     * @param event The `event` parameter in the `disJoinSelection` method is of type `ActionEvent`. This parameter is
     * commonly used in JavaFX applications to represent an event that occurred, such as a button click or menu selection.
     * In this method, the `event` parameter is likely used to trigger the
     */
    public void disJoinSelection(ActionEvent event) {
        Image img = originalImage.getImage();
        if (img != null) {
            int width = (int)img.getWidth();
            int height = (int)img.getHeight();
            for (int y= 0; y< height; y++) {
                for (int x = 0; x < width; x++) {
                    int index = y * width + x;
                    if (pixelArray[index]!= -1) {

                        // The condition `if (x+1 < width && y+1 < height && pixelArray[index+1]!= -1 )` is checking if the
                        // pixel to the right of the current pixel (in the same row) is within the image boundaries and if
                        // it is not part of the background (pixelArray[index+1] != -1). This condition is used in the
                        // `disJoinSelection` method to determine if the current pixel should be unioned with its right
                        // neighbor pixel based on the threshold set in the image processing.
                        if (x+1 < width && y+1 < height && pixelArray[index+1]!= -1 ){
                            unionFind.union(pixelArray, index,index+1);
                        }
                        if (index + width <pixelArray.length && pixelArray[index + width]!= -1) {
                            unionFind.union(pixelArray, index, index + width);
                        }
                    }

                }
            }
        }
    }


//    private void recordSelectedPill (Color pixelColor, double imageX, double imageY) {
//
////        String positionKey = String.format("%.2f,%.2f", imageX, imageY);
//        String positionKey = (int)imageX + "," + (int) imageY;
//        colorMap.put(positionKey, pixelColor);
//        unionFind.parent.put(positionKey, positionKey);
//    }
//    private void processPillSelection( double imageX, double imageY) {
//
////        String positionKey = String.format("%.2f,%.2f", imageX, imageY);
////        Color selectedColor = colorMap.get(positionKey);
////        for (int dx = -1; dx <= 1; dx++) {
////            for (int dy = -1; dy <= 1; dy++) {
////                if (dx == 0 && dy == 0) {
////                    continue;
////                }
//////                String neighborPositionKey = String.format("%.2f,%.2f", imageX + dx, imageY + dy);
////                Color neighborColor = colorMap.get(neighborPositionKey);
////                if (neighborColor!= null &&!neighborColor.equals(selectedColor)) {
////                    unionFind.union(positionKey, neighborPositionKey);
////                }
////            }
////        }
//    }
//    List<Integer> disjointSets= new ArrayList<>();

    public void selectPill(Rectangle r) {

        // Get the dimensions and position of the rectangle
        double rectangleX = r.getX();
        double rectangleY = r.getY();
        double rectangleWidth = r.getWidth();
        double rectangleHeight = r.getHeight();

        int imageWidth = (int) originalPicture.getWidth();
        int imageHeight = (int) originalPicture.getHeight();
        WritableImage writableImage = new WritableImage(imageWidth, imageHeight);

        // Iterate through the pixels within the bounding box of the rectangle
        for (double y = rectangleY; y < rectangleY + rectangleHeight; y++) {
            for (double x = rectangleX; x < rectangleX + rectangleWidth; x++) {
                int index = (int) (y * imageWidth + x);
                for (int i = index; i < pixelArray.length; i++) {
                    // Convert coordinates to pixel index



                    // Find the root of the pixel
                    int root = unionFind.find(pixelArray,index);
                    if (root != -1 && i==unionFind.find(pixelArray,index)) {}
                          {
                              // Get the color associated with the root and set it to the corresponding pixel in the new image
                              Color color = colorMap.get(root);
                              if (color != null) {
                                  writableImage.getPixelWriter().setColor((int) x, (int) y, color);
                              }
                              else{
                                  index++;
                              }
                          }

                }
            }
        }

        originalImage.setImage(writableImage);

    }
    public void reset(){
        ((Pane) originalImage.getParent()).getChildren().removeIf(r -> r instanceof Rectangle);
    }
    public void displayPillsoldSelected() {
        if (colorMap != null) {
            // Remove existing rectangles and labels
            ((Pane) originalImage.getParent()).getChildren().removeIf(r -> r instanceof Rectangle);


            // Iterate over the pixels and identify the pills
            this.allPillsPixels = new HashMap<>();
            double top = 0;
            double bottom = 0;
            double left = 0;
            double right = 0;
            int imageWidth = (int) originalImage.getFitWidth();
            int imageHeight = (int) originalImage.getFitHeight();
            Bounds bounds = originalImage.getLayoutBounds();
            double scaleX = bounds.getWidth() / originalImage.getImage().getWidth();
            double scaleY = bounds.getHeight() / originalImage.getImage().getHeight();


            for (int i = 0; i < pixelArray.length; i++) {
                int root = unionFind.find(pixelArray, i);
                if (root != -1) {
                    if (!allPillsPixels.containsKey(root)) {
                        allPillsPixels.put(root, new ArrayList<>());
                    }
                    allPillsPixels.get(root).add(i);
                }
            }

            for (int root : allPillsPixels.keySet()) {
                // The line `top= Collections.min(allPillsPixels.get(root))/(int)originalPicture.getWidth();` is
                // attempting to calculate the top position of a pill based on the minimum pixel value in the list
                // of pixels associated with a particular pill root, divided by the width of the original picture.
                List<Integer> pixels = allPillsPixels.get(root);
                if (!pixels.isEmpty()) {
                    double minX = Double.MAX_VALUE;
                    double minY = Double.MAX_VALUE;
                    double maxX = Double.MIN_VALUE;
                    double maxY = Double.MIN_VALUE;

                    for (int pixelIndex : pixels) {
                        int x = pixelIndex % imageWidth;
                        int y = pixelIndex / imageWidth;

                        minX = Math.min(minX, x);
                        minY = Math.min(minY, y);
                        maxX = Math.max(maxX, x);
                        maxY = Math.max(maxY, y);
                    }

                    double centerX = (minX + maxX) / 2;
                    double centerY = (minY + maxY) / 2;
                    double width = maxX - minX + 1;
                    double height = maxY - minY + 1;

                    // Create rectangle based on the calculated boundaries
                    Rectangle rectangle = new Rectangle(centerX, centerY, width, height);
                    rectangle.setFill(Color.TRANSPARENT);
                    rectangle.setStroke(Color.RED);
                    rectangle.setStrokeWidth(1);
                    rectangle.onMouseClickedProperty().set(e -> selectPill(rectangle));
                    ((Pane) originalImage.getParent()).getChildren().add(rectangle);
                }
            }
        }
    }
    public void displayPillsSelected() {
        if (colorMap != null) {
            // Remove existing rectangles and labels
            ((Pane) originalImage.getParent()).getChildren().removeIf(r -> r instanceof Rectangle);
            ((Pane) originalImage.getParent()).getChildren().removeIf(r -> r instanceof Label);

            // Iterate over the pixels and identify the pills
            this.allPillsPixels = new HashMap<>();
            int imageWidth = (int) originalPicture.getWidth();
            int imageHeight = (int) originalPicture.getHeight();

            for (int i = 0; i < pixelArray.length; i++) {
                int root = unionFind.find(pixelArray, i);
                if (root != -1) {
                    if (!allPillsPixels.containsKey(root)) {
                        allPillsPixels.put(root, new ArrayList<>());
                    }
                    allPillsPixels.get(root).add(i);
                }
            }

            for (int root : allPillsPixels.keySet()) {
                List<Integer> pixels = allPillsPixels.get(root);
                int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
                int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;

                // Find the bounding box of the pill
                for (int pixel : pixels) {
                    int x = pixel % imageWidth;
                    int y = pixel / imageWidth;
                    minX = Math.min(minX, x);
                    minY = Math.min(minY, y);
                    maxX = Math.max(maxX, x);
                    maxY = Math.max(maxY, y);
                }

                // Calculate the center of the bounding box
                double centerX = (minX + maxX) / 2.0;
                double centerY = (minY + maxY) / 2.0;
                double width = maxX - minX + 1;
                double height = maxY - minY + 1;

                // Create rectangle based on the calculated boundaries
                Rectangle rectangle = new Rectangle(centerX, centerY, width, height);


                rectangle.setOnMouseClicked(e -> selectPill(rectangle));
                rectangle.setFill(Color.TRANSPARENT);
                rectangle.setStroke(Color.BLUE);
                rectangle.setStrokeWidth(1);

                // Add the rectangle to the pane
                ((Pane) originalImage.getParent()).getChildren().add(rectangle);
            }
        }
    }

//    public void selectPill(Rectangle r) {
//        // Get the position of the rectangle
//        double rectangleX = r.getX();
//        double rectangleY = r.getY();
//
//        // Iterate through the pixels within the bounding box of the rectangle
//        for (int y = (int) rectangleY; y < rectangleY + r.getHeight(); y++) {
//            for (int x = (int) rectangleX; x < rectangleX + r.getWidth(); x++) {
//                int index = y * (int) originalPicture.getWidth() + x;
//                int root = unionFind.find(pixelArray, index);
//                if (root != -1) {
//                    // Get the color associated with the root and set it to the corresponding pixel in the new image
//                    Color color = colorMap.get(root);
//                    if (color != null) {
//                        processedImage.getImage().getPixelWriter().setColor(x, y, color);
//                    }
//                }
//            }
//        }
//    }




    private Rectangle createRectangleAroundPill(double x, double y) {


        Rectangle rectangle = new Rectangle(x, y, 1, 1);

        return rectangle;
    }



}
