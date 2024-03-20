package com.example.pillanalyser;

import javafx.scene.paint.Color;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PillTest {


    @Test
    void getColor() {
        Pill pill = new Pill(Color.RED);
        assertEquals(Color.RED, pill.getColor());
    }
}