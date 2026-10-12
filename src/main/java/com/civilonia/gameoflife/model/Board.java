package com.civilonia.gameoflife.model;

import java.util.BitSet;
import java.util.Objects;

/**
 * An immutable, finite Game of Life board.
 *
 * <p>Coordinates are zero based. {@code (0, 0)} is the top-left cell, {@code x}
 * increases to the right, and {@code y} increases downward. Internally, a cell at
 * {@code (x, y)} is stored at bit index {@code y * width + x}; this is row-major
 * order.</p>
 *
 * <p>Coordinates outside this board are considered dead. The board never wraps at
 * its edges.</p>
 */
public final class Board {

    private final int width;
    private final int height;
    private final BitSet liveCells;

    /**
     * Creates an all-dead board with the supplied dimensions.
     *
     * @param width number of columns; must be positive
     * @param height number of rows; must be positive
     * @throws IllegalArgumentException if either dimension is not positive or the
     *     total cell count cannot be represented as an {@code int}
     */
    public Board(int width, int height) {
        validateDimensions(width, height);
        this.width = width;
        this.height = height;
        this.liveCells = new BitSet(cellCount(width, height));
    }

    /**
     * Creates a board from row-major cell states.
     *
     * <p>{@code initialCells[y][x]} supplies the state at {@code (x, y)}. The
     * array must have exactly {@code height} rows, each with exactly {@code width}
     * columns. Its contents are copied, so later mutations do not affect this
     * board.</p>
     *
     * @param width number of columns; must be positive
     * @param height number of rows; must be positive
     * @param initialCells initial states in row-major order
     * @throws IllegalArgumentException if dimensions or array shape are invalid, or
     *     the total cell count cannot be represented as an {@code int}
     * @throws NullPointerException if {@code initialCells} or one of its rows is null
     */
    public Board(int width, int height, boolean[][] initialCells) {
        validateDimensions(width, height);
        Objects.requireNonNull(initialCells, "initialCells must not be null");
        if (initialCells.length != height) {
            throw new IllegalArgumentException("initialCells must contain exactly " + height + " rows");
        }

        this.width = width;
        this.height = height;
        this.liveCells = new BitSet(cellCount(width, height));
        for (int y = 0; y < height; y++) {
            boolean[] row = Objects.requireNonNull(initialCells[y], "initialCells rows must not be null");
            if (row.length != width) {
                throw new IllegalArgumentException("each initialCells row must contain exactly " + width + " columns");
            }
            for (int x = 0; x < width; x++) {
                if (row[x]) {
                    liveCells.set(indexOf(x, y));
                }
            }
        }
    }

    /** Returns the number of columns. */
    public int width() {
        return width;
    }

    /** Returns the number of rows. */
    public int height() {
        return height;
    }

    /**
     * Returns whether the cell at {@code (x, y)} is alive.
     * Coordinates outside the board return {@code false}.
     */
    public boolean isAlive(int x, int y) {
        return isWithinBounds(x, y) && liveCells.get(indexOf(x, y));
    }

    private boolean isWithinBounds(int x, int y) {
        return x >= 0 && x < width && y >= 0 && y < height;
    }

    private int indexOf(int x, int y) {
        return y * width + x;
    }

    private static void validateDimensions(int width, int height) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be positive");
        }
        cellCount(width, height);
    }

    private static int cellCount(int width, int height) {
        try {
            return Math.multiplyExact(width, height);
        } catch (ArithmeticException exception) {
            throw new IllegalArgumentException("board dimensions are too large", exception);
        }
    }
}
