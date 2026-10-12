package com.civilonia.gameoflife.core;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.civilonia.gameoflife.model.Board;
import org.junit.jupiter.api.Test;

class ConwayTransitionTest {

    @Test
    void countsAllEightLiveNeighborsForAnInteriorCell() {
        Board board = new Board(3, 3, new boolean[][] {
                {true, true, true},
                {true, false, true},
                {true, true, true}
        });

        assertEquals(8, ConwayTransition.countLiveNeighbors(board, 1, 1));
    }

    @Test
    void countsOnlyInBoundsNeighborsAtAnEdgeAndCorner() {
        Board board = new Board(3, 3, new boolean[][] {
                {false, true, true},
                {true, true, false},
                {false, false, false}
        });

        assertEquals(3, ConwayTransition.countLiveNeighbors(board, 0, 0));
        assertEquals(3, ConwayTransition.countLiveNeighbors(board, 1, 0));
    }

    @Test
    void excludesTheCenterCellFromItsOwnNeighborCount() {
        Board board = new Board(1, 1, new boolean[][] {{true}});

        assertEquals(0, ConwayTransition.countLiveNeighbors(board, 0, 0));
    }

    @Test
    void rejectsANullBoard() {
        assertThrows(NullPointerException.class, () -> ConwayTransition.countLiveNeighbors(null, 0, 0));
    }

    @Test
    void liveCellWithFewerThanTwoNeighborsDies() {
        assertFalse(ConwayTransition.nextState(true, 0));
        assertFalse(ConwayTransition.nextState(true, 1));
    }

    @Test
    void liveCellWithTwoOrThreeNeighborsSurvives() {
        assertTrue(ConwayTransition.nextState(true, 2));
        assertTrue(ConwayTransition.nextState(true, 3));
    }

    @Test
    void liveCellWithMoreThanThreeNeighborsDies() {
        assertFalse(ConwayTransition.nextState(true, 4));
        assertFalse(ConwayTransition.nextState(true, 8));
    }

    @Test
    void deadCellWithExactlyThreeNeighborsBecomesAlive() {
        assertTrue(ConwayTransition.nextState(false, 3));
        assertFalse(ConwayTransition.nextState(false, 2));
        assertFalse(ConwayTransition.nextState(false, 4));
    }
}
