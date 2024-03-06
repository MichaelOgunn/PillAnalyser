package com.example.pillanalyser;

import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.paint.Color;

public class ColourFinder {
    public Color getPixelColour(Image img, int x, int y) {
        PixelReader pixelReader = img.getPixelReader();
        return pixelReader.getColor(x, y);
    }
    public boolean areColorsSimilar(Color color1, Color color2, double hueThreshhold,double saturationThreshold,
                                    double brightnessThreshold) {
        double hueDifference = Math.abs(color1.getHue() - color2.getHue());
        double saturationDifference = Math.abs(color1.getSaturation() - color2.getSaturation());
        double brightnessDifference = Math.abs(color1.getBrightness() - color2.getBrightness());
        return hueDifference <= hueThreshhold && saturationDifference <= saturationThreshold &&
                brightnessDifference <= brightnessThreshold;


    }
    public boolean areColorsSimilarForSelection(Color color1, Color color2, double colorThreshold) {
        return Math.abs(color1.getHue()-color2.getHue())<(colorThreshold*10);


    }
}
