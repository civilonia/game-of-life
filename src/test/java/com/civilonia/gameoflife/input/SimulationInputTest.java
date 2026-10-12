package com.civilonia.gameoflife.input;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.civilonia.gameoflife.model.Board;
import org.junit.jupiter.api.Test;

class SimulationInputTest {

    @Test
    void createsInputFromAnInitialBoardAndGenerationCount() {
        Board initialBoard = new Board(3, 2, new boolean[][] {
                {false, true, false},
                {true, false, true}
        });

        SimulationInput input = new SimulationInput(initialBoard, 4);

        assertSame(initialBoard, input.initialBoard());
        assertEquals(4, input.generations());
    }

    @Test
    void permitsZeroGenerationsForAnInitialStateOnlySimulation() {
        SimulationInput input = new SimulationInput(new Board(1, 1), 0);

        assertEquals(0, input.generations());
    }

    @Test
    void rejectsANullInitialBoard() {
        NullPointerException exception = assertThrows(
                NullPointerException.class,
                () -> new SimulationInput(null, 0));

        assertEquals("initialBoard must not be null", exception.getMessage());
    }

    @Test
    void rejectsNegativeGenerationCounts() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> new SimulationInput(new Board(1, 1), -1));

        assertEquals("generations must not be negative", exception.getMessage());
    }
}
