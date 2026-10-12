package com.civilonia.gameoflife.model;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class BoardTest {

    @Test
    void createsAnAllDeadBoard() {
        Board board = new Board(3, 2);

        assertEquals(3, board.width());
        assertEquals(2, board.height());
        assertFalse(board.isAlive(0, 0));
        assertFalse(board.isAlive(2, 1));
    }

    @Test
    void builderCreatesBoardWithTheConfiguredCellStates() {
        Board board = Board.builder(3, 2)
                .setAlive(1, 0, true)
                .setAlive(0, 1, true)
                .setAlive(2, 1, true)
                .build();

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
        Board board = Board.builder(2, 2)
                .setAlive(0, 0, true)
                .setAlive(1, 0, true)
                .setAlive(0, 1, true)
                .setAlive(1, 1, true)
                .build();

        assertFalse(board.isAlive(-1, 0));
        assertFalse(board.isAlive(0, -1));
        assertFalse(board.isAlive(2, 0));
        assertFalse(board.isAlive(0, 2));
        assertDoesNotThrow(() -> board.isAlive(Integer.MAX_VALUE, Integer.MAX_VALUE));
    }

    @Test
    void rejectsInvalidDimensions() {
        assertThrows(IllegalArgumentException.class, () -> new Board(0, 1));
        assertThrows(IllegalArgumentException.class, () -> new Board(1, 0));
        assertThrows(IllegalArgumentException.class, () -> new Board(Integer.MAX_VALUE, 2));
    }

    @Test
    void builderCreatesIndependentImmutableBoardSnapshots() {
        Board.Builder builder = Board.builder(2, 1).setAlive(0, 0, true);

        Board first = builder.build();
        builder.setAlive(0, 0, false).setAlive(1, 0, true);
        Board second = builder.build();

        assertTrue(first.isAlive(0, 0));
        assertFalse(first.isAlive(1, 0));
        assertFalse(second.isAlive(0, 0));
        assertTrue(second.isAlive(1, 0));
    }

    @Test
    void builderRejectsInvalidDimensionsAndCoordinates() {
        assertThrows(IllegalArgumentException.class, () -> Board.builder(0, 1));

        Board.Builder builder = Board.builder(1, 1);
        assertThrows(IndexOutOfBoundsException.class, () -> builder.setAlive(-1, 0, true));
        assertThrows(IndexOutOfBoundsException.class, () -> builder.setAlive(1, 0, true));
        assertThrows(IndexOutOfBoundsException.class, () -> builder.setAlive(0, -1, true));
        assertThrows(IndexOutOfBoundsException.class, () -> builder.setAlive(0, 1, true));
    }
}
