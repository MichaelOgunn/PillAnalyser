package com.example.pillanalyser;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    private Map<String, Color> colorMap = new HashMap<>();
    private HashMap<String,List<String>> allPillsPixels;
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
        enablePillSelection();
    }
    public void showImageDetails(File selectedFile) {
        if (selectedFile!= null) {
            originalImage.setImage(new Image(selectedFile.toURI().toString()));
        }
    }
    public void convertImageToBandW(ActionEvent event) {
        Image img = originalImage.getImage();
        if (img!= null) {
            PixelReader pixelReader = img.getPixelReader();
            WritableImage bwImage = new WritableImage((int) img.getWidth(), (int) img.getHeight());
            PixelWriter pixelWriter = bwImage.getPixelWriter();
            for (int y = 0; y < img.getHeight(); y++) {
                for (int x = 0; x < img.getWidth(); x++) {
                    Color color = pixelReader.getColor(x, y);
                    double brightness = color.getBrightness();
                    if (brightness < 0.5) {
                        pixelWriter.setColor(x, y, Color.BLACK);
                    } else {
                        pixelWriter.setColor(x, y, Color.WHITE);
                    }
                }
            }
            processedImage.setImage(bwImage);
        }
    }
    private void enablePillSelection() {
        originalImage.setOnMouseClicked(event -> {
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
        });
    }


    private void recordSelectedPill (Color pixelColor, double imageX, double imageY) {
        String positionKey = String.format("%.2f,%.2f", imageX, imageY);
        colorMap.put(positionKey, pixelColor);
        unionFind.parent.put(positionKey, positionKey);
    }
    private void processPillSelection( double imageX, double imageY) {
        String positionKey = String.format("%.2f,%.2f", imageX, imageY);
        Color selectedColor = colorMap.get(positionKey);
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) {
                    continue;
                }
                String neighborPositionKey = String.format("%.2f,%.2f", imageX + dx, imageY + dy);
                Color neighborColor = colorMap.get(neighborPositionKey);
                if (neighborColor!= null &&!neighborColor.equals(selectedColor)) {
                    unionFind.union(positionKey, neighborPositionKey);
                }
            }
        }
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
                // Convert coordinates to pixel index
                int index = (int) (y * imageWidth + x);

                String positionKey = String.format("%.2f,%.2f", x, y );

                // Find the root of the pixel
                String root = unionFind.find(positionKey);


                // Get the color associated with the root and set it to the corresponding pixel in the new image
                Color color = colorMap.get(root);
                if (color != null) {
                    writableImage.getPixelWriter().setColor((int) x, (int) y, color);
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
            int imageWidth = (int) originalImage.getFitWidth();
            int imageHeight = (int) originalImage.getFitHeight();

            for (int y = 0; y < imageHeight; y++) {
                for (int x = 0; x < imageWidth; x++) {


                    String positionKey = String.format("%d,%d", (int) x, (int) y);
                    String root = unionFind.find(positionKey);

                    List<String> pixels = allPillsPixels.getOrDefault(root, new ArrayList<>());
                    pixels.add(positionKey);
                    allPillsPixels.put(root, pixels);
                    selectPill(createRectangleAroundPill(x, y));
                }

            }
        }
    }

    private Rectangle createRectangleAroundPill(double x, double y) {
        double relativeX = x * (originalImage.getFitWidth() / originalPicture.getWidth());
        double relativeY = y * (originalImage.getFitHeight() / originalPicture.getHeight());

        Rectangle rectangle = new Rectangle(relativeX, relativeY, 1, 1);
        rectangle.setFill(Color.TRANSPARENT);
        rectangle.setStroke(Color.RED);
        rectangle.setStrokeWidth(2);
        return rectangle;
    }



}
