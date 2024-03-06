package com.example.pillanalyser;

import java.util.ArrayList;
import java.util.List;

public class PillApi {


    private List<Pill> pills;

    
    public PillApi(){
        this.pills = new ArrayList<>();
    }

    public void addPill(Pill pill){
        pills.add(pill);
    }
    public List<Pill> getPills() {
        return pills;
    }


}
