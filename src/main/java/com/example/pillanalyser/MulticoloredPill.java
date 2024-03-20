package com.example.pillanalyser;

import javafx.scene.paint.Color;

public class MulticoloredPill extends Pill {
    private Color colorOne;
    private Color colorTwo;

    public MulticoloredPill(Color colorOne, Color colorTwo) {
        super(colorOne);
        this.colorTwo = colorTwo;
    }



}
