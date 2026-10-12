package com.civilonia.gameoflife.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BoardTest {

    @Test
    void exposesDimensionsAndRowMajorCellStates() {
        Board board = new Board(3, 2, new boolean[][] {
                {false, true, false},
                {true, false, true}
        });

        assertTrue(board.isAlive(1, 0));
        assertTrue(board.isAlive(0, 1));
        assertTrue(board.isAlive(2, 1));
        assertFalse(board.isAlive(0, 0));
        assertFalse(board.isAlive(2, 0));
        assertEquals(3, board.width());
        assertEquals(2, board.height());
    }

    @Test
    void isAliveTreatsCoordinatesOutsideTheBoardAsDead() {
        Board board = new Board(2, 2, new boolean[][] {{true, true}, {true, true}});

        assertFalse(board.isAlive(-1, 0));
        assertFalse(board.isAlive(0, -1));
        assertFalse(board.isAlive(2, 0));
        assertFalse(board.isAlive(0, 2));
        assertDoesNotThrow(() -> board.isAlive(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    @Test
    void copiesInitialStateDefensively() {
        boolean[][] initialCells = {{true, false}};
        Board board = new Board(2, 1, initialCells);

        initialCells[0][0] = false;
        initialCells[0][1] = true;

        assertTrue(board.isAlive(0, 0));
        assertFalse(board.isAlive(1, 0));
    }

    @Test
    void rejectsInvalidDimensionsAndInitialStateShapes() {
        assertThrows(IllegalArgumentException.class, () -> new Board(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new Board(1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Board(Integer.MAX_VALUE, 2));
        assertThrows(IllegalArgumentException.class, () -> new Board(2, 2, new boolean[][] {{true, false}}));
        assertThrows(IllegalArgumentException.class, () -> new Board(2, 1, new boolean[][] {{true}}));
        assertThrows(NullPointerException.class, () -> new Board(1, 1, null));
        assertThrows(NullPointerException.class, () -> new Board(1, 1, new boolean[][] {null}));
    }
}
