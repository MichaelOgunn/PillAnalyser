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
    public boolean areColorsSimilar4S(Color color1, Color color2, double colorThreshold) {
        double redDiff = Math.abs(color1.getRed() - color2.getRed());
        double greenDiff = Math.abs(color1.getGreen() - color2.getGreen());
        double blueDiff = Math.abs(color1.getBlue() - color2.getBlue());
        return redDiff + greenDiff + blueDiff <= colorThreshold;
    }
    public boolean areColorsSimilarForSelection(Color color1, Color color2, double colorThreshold) {
        return Math.abs(color1.getHue()-color2.getHue())<(colorThreshold*10);
    }
    public boolean isColorSimilar(Color color1, Color color2, double hueThreshold, double saturationThreshold, double brightnessThreshold) {
        double hue1 = color1.getHue();
        double saturation1 = color1.getSaturation();
        double brightness1 = color1.getBrightness();

        double hue2 = color2.getHue();
        double saturation2 = color2.getSaturation();
        double brightness2 = color2.getBrightness();

        return Math.abs(hue1 - hue2) < hueThreshold &&
                Math.abs(saturation1 - saturation2) < saturationThreshold &&
                Math.abs(brightness1 - brightness2) < brightnessThreshold;
    }
}
