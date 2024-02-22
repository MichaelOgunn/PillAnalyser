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




                recordSelectedPill(sampledColor, imageX, imageY);// Implement this method
                processPillSelection(imageX, imageY);
                displayPillsSelected();


            }

    }


    private void recordSelectedPill (Color pixelColor, double imageX, double imageY) {

//        String positionKey = String.format("%.2f,%.2f", imageX, imageY);
        String positionKey = (int)imageX + "," + (int) imageY;
        colorMap.put(positionKey, pixelColor);
        unionFind.parent.put(positionKey, positionKey);
    }
    private void processPillSelection( double imageX, double imageY) {

//        String positionKey = String.format("%.2f,%.2f", imageX, imageY);
//        Color selectedColor = colorMap.get(positionKey);
//        for (int dx = -1; dx <= 1; dx++) {
//            for (int dy = -1; dy <= 1; dy++) {
//                if (dx == 0 && dy == 0) {
//                    continue;
//                }
////                String neighborPositionKey = String.format("%.2f,%.2f", imageX + dx, imageY + dy);
//                Color neighborColor = colorMap.get(neighborPositionKey);
//                if (neighborColor!= null &&!neighborColor.equals(selectedColor)) {
//                    unionFind.union(positionKey, neighborPositionKey);
//                }
//            }
//        }
    }
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
    public void displayPillsSelected() {
        if (colorMap != null) {
            // Remove existing rectangles and labels
            ((Pane) originalImage.getParent()).getChildren().removeIf(r -> r instanceof Rectangle);
            ((Pane) originalImage.getParent()).getChildren().removeIf(r -> r instanceof Label);

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
                top = Collections.min(allPillsPixels.get(root)) / (int) originalPicture.getWidth();
                bottom = Collections.max(allPillsPixels.get(root)) / (int) originalPicture.getWidth();
                left = Collections.min(allPillsPixels.get(root), (a, b) -> a % imageWidth - b % imageWidth) % imageWidth;//negative if a should come before b and pos if a comes after, zero if theyre the same
                right = Collections.max(allPillsPixels.get(root), (a, b) -> a % imageWidth - b % imageWidth) % imageWidth;
                if (root != -1) {
                    top *= scaleX;
                    right *= scaleY;
                    bottom *= scaleX;
                    left *= scaleY;
                    double centerX = (left + right) / 2;
                    double centerY = (top + bottom) / 2;


                    Rectangle rectangle = new Rectangle(centerX, centerY, 1, 1);

                    rectangle.onMouseClickedProperty().set(e -> selectPill(rectangle));
                    System.out.println("here4");
                }
            }
        }
    }



    private Rectangle createRectangleAroundPill(double x, double y) {


        Rectangle rectangle = new Rectangle(x, y, 1, 1);
        rectangle.setFill(Color.TRANSPARENT);
        rectangle.setStroke(Color.RED);
        rectangle.setStrokeWidth(2);
        return rectangle;
    }



}
