package com.example.pillanalyser;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PillApiTest {

    @Test
    void addPill() {
        PillApi pillApi = new PillApi();
        Pill pill = new Pill(Color.RED);
        pillApi.addPill(pill);

    }

    @Test
    void getPills() {
        PillApi pillApi = new PillApi();
        Pill pill = new Pill(Color.RED);
        pillApi.addPill(pill);
        List<Pill> pills = pillApi.getPills();
        assertEquals(1, pills.size());
    }
}