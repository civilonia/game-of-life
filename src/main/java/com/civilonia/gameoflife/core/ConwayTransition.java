package com.civilonia.gameoflife.core;

import com.civilonia.gameoflife.model.Board;
import java.util.Objects;

/**
 * Stateless operations for one Conway's Game of Life cell transition.
 *
 * <p>All calculations use the eight-cell Moore neighborhood. {@link Board}
 * treats out-of-bounds coordinates as dead, so no coordinate wrapping occurs.
 */
public final class ConwayTransition {

    private ConwayTransition() {
    }

    /**
     * Counts the live cells surrounding {@code (x, y)}.
     *
     * @param board board whose cells are inspected
     * @param x x coordinate of the center cell
     * @param y y coordinate of the center cell
     * @return the number of live neighbors, from zero through eight
     */
    public static int countLiveNeighbors(Board board, int x, int y) {
        Objects.requireNonNull(board, "board must not be null");

        int liveNeighbors = 0;
        for (int offsetY = -1; offsetY <= 1; offsetY++) {
            for (int offsetX = -1; offsetX <= 1; offsetX++) {
                if ((offsetX != 0 || offsetY != 0) && board.isAlive(x + offsetX, y + offsetY)) {
                    liveNeighbors++;
                }
            }
        }
        return liveNeighbors;
    }

    /**
     * Applies Conway's transition rules to one cell.
     *
     * @param currentlyAlive whether the cell is alive in the current generation
     * @param liveNeighbors number of live cells in its Moore neighborhood
     * @return whether the cell is alive in the next generation
     */
    public static boolean nextState(boolean currentlyAlive, int liveNeighbors) {
        return liveNeighbors == 3 || (currentlyAlive && liveNeighbors == 2);
    }
}
