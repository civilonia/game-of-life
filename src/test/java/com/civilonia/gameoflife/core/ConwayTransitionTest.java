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

    @Test
    void rejectsNeighborCountsOutsideTheMooreNeighborhoodRange() {
        assertThrows(IllegalArgumentException.class, () -> ConwayTransition.nextState(true, -1));
        assertThrows(IllegalArgumentException.class, () -> ConwayTransition.nextState(false, 9));
    }

    @Test
    void evolvesABlockAsAStillLife() {
        Board block = board(new boolean[][] {
                {false, false, false, false},
                {false, true, true, false},
                {false, true, true, false},
                {false, false, false, false}
        });

        assertBoardEquals(block, ConwayTransition.nextGeneration(block));
    }

    @Test
    void evolvesABlinkerThroughItsPeriodTwoOscillation() {
        Board horizontal = board(new boolean[][] {
                {false, false, false, false, false},
                {false, false, false, false, false},
                {false, true, true, true, false},
                {false, false, false, false, false},
                {false, false, false, false, false}
        });
        Board vertical = board(new boolean[][] {
                {false, false, false, false, false},
                {false, false, true, false, false},
                {false, false, true, false, false},
                {false, false, true, false, false},
                {false, false, false, false, false}
        });

        Board firstGeneration = ConwayTransition.nextGeneration(horizontal);

        assertBoardEquals(vertical, firstGeneration);
        assertBoardEquals(horizontal, ConwayTransition.nextGeneration(firstGeneration));
    }

    @Test
    void evolvesAGliderDiagonally() {
        Board glider = board(new boolean[][] {
                {false, false, false, false, false, false},
                {false, false, true, false, false, false},
                {false, false, false, true, false, false},
                {false, true, true, true, false, false},
                {false, false, false, false, false, false},
                {false, false, false, false, false, false}
        });
        Board expectedAfterFourGenerations = board(new boolean[][] {
                {false, false, false, false, false, false},
                {false, false, false, false, false, false},
                {false, false, false, true, false, false},
                {false, false, false, false, true, false},
                {false, false, true, true, true, false},
                {false, false, false, false, false, false}
        });

        Board evolved = glider;
        for (int generation = 0; generation < 4; generation++) {
            evolved = ConwayTransition.nextGeneration(evolved);
        }

        assertBoardEquals(expectedAfterFourGenerations, evolved);
    }

    @Test
    void returnsANewBoardWithoutChangingTheInputAndIsDeterministic() {
        Board original = board(new boolean[][] {
                {false, true, false},
                {false, true, false},
                {false, true, false}
        });
        Board expectedInput = board(new boolean[][] {
                {false, true, false},
                {false, true, false},
                {false, true, false}
        });

        Board firstResult = ConwayTransition.nextGeneration(original);
        Board secondResult = ConwayTransition.nextGeneration(original);

        assertFalse(firstResult == original);
        assertEquals(original.width(), firstResult.width());
        assertEquals(original.height(), firstResult.height());
        assertBoardEquals(expectedInput, original);
        assertBoardEquals(firstResult, secondResult);
    }

    @Test
    void rejectsANullBoardWhenComputingTheNextGeneration() {
        assertThrows(NullPointerException.class, () -> ConwayTransition.nextGeneration(null));
    }

    private static Board board(boolean[][] cells) {
        return new Board(cells[0].length, cells.length, cells);
    }

    private static void assertBoardEquals(Board expected, Board actual) {
        assertEquals(expected.width(), actual.width());
        assertEquals(expected.height(), actual.height());
        for (int y = 0; y < expected.height(); y++) {
            for (int x = 0; x < expected.width(); x++) {
                assertEquals(expected.isAlive(x, y), actual.isAlive(x, y),
                        "cell state differs at (" + x + ", " + y + ")");
            }
        }
    }
}
