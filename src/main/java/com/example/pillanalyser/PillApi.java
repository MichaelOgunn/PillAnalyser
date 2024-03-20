package com.example.pillanalyser;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.List;

public class PillApi {
    private int pillCount ;
    private Rectangle pillRectangle;
    private List<Pill> pills;

    
    public PillApi(){
        this.pills = new ArrayList<>();
        this.pillRectangle = new Rectangle();
        this.pillCount = 0;
    }

    public void addPill(Pill pill){
        pills.add(pill);
    }

    public List<Pill> getPills() {
        return pills;
    }


}
