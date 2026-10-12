package com.civilonia.gameoflife.core;

import com.civilonia.gameoflife.model.Board;
import java.util.Objects;

/**
 * Stateless operations for Conway's Game of Life transitions.
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
     * @param liveNeighbors number of live cells in its Moore neighborhood, from
     *     zero through eight
     * @return whether the cell is alive in the next generation
     * @throws IllegalArgumentException if {@code liveNeighbors} is outside the
     *     range from zero through eight; such a count is impossible in a Game of
     *     Life Moore neighborhood
     */
    public static boolean nextState(boolean currentlyAlive, int liveNeighbors) {
        if (liveNeighbors < 0 || liveNeighbors > 8) {
            throw new IllegalArgumentException("liveNeighbors must be between 0 and 8");
        }
        return liveNeighbors == 3 || (currentlyAlive && liveNeighbors == 2);
    }

    /**
     * Computes the board for the next generation.
     *
     * <p>Every cell is evaluated against {@code board}, then the resulting states
     * are used to construct a separate immutable board. Consequently, cells that
     * change state during this operation cannot affect other cells in the same
     * generation.</p>
     *
     * @param board board for the current generation
     * @return a new board with the same dimensions and the next generation's states
     * @throws NullPointerException if {@code board} is null
     */
    public static Board nextGeneration(Board board) {
        Objects.requireNonNull(board, "board must not be null");

        int width = board.width();
        int height = board.height();
        boolean[][] nextCells = new boolean[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                nextCells[y][x] = nextState(board.isAlive(x, y), countLiveNeighbors(board, x, y));
            }
        }
        return new Board(width, height, nextCells);
    }
}
