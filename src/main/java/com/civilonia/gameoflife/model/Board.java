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
        this(width, height, emptyCells(width, height));
    }

    private Board(int width, int height, BitSet liveCells) {
        validateDimensions(width, height);
        Objects.requireNonNull(liveCells, "liveCells must not be null");

        this.width = width;
        this.height = height;
        this.liveCells = (BitSet) liveCells.clone();
    }

    private static BitSet emptyCells(int width, int height) {
        validateDimensions(width, height);
        return new BitSet(cellCount(width, height));
    }

    /**
     * Creates a builder for an immutable board with the supplied dimensions.
     *
     * <p>The builder keeps its mutable construction state private. Each call to
     * {@link Builder#build()} produces an independent immutable board.</p>
     *
     * @param width number of columns; must be positive
     * @param height number of rows; must be positive
     * @return a builder for a board of the supplied dimensions
     * @throws IllegalArgumentException if either dimension is not positive or the
     *     total cell count cannot be represented as an {@code int}
     */
    public static Builder builder(int width, int height) {
        return new Builder(width, height);
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

    /**
     * Mutable construction helper for a {@link Board}.
     *
     * <p>This type deliberately exposes cell coordinates rather than the board's
     * internal storage representation.</p>
     */
    public static final class Builder {

        private final int width;
        private final int height;
        private final BitSet liveCells;

        private Builder(int width, int height) {
            validateDimensions(width, height);
            this.width = width;
            this.height = height;
            this.liveCells = new BitSet(cellCount(width, height));
        }

        /**
         * Sets the state of a cell in the board being built.
         *
         * @param x zero-based column coordinate
         * @param y zero-based row coordinate
         * @param alive whether the cell is alive
         * @return this builder
         * @throws IndexOutOfBoundsException if {@code (x, y)} is outside the board
         */
        public Builder setAlive(int x, int y, boolean alive) {
            if (x < 0 || x >= width || y < 0 || y >= height) {
                throw new IndexOutOfBoundsException("cell coordinates are outside the board");
            }
            liveCells.set(y * width + x, alive);
            return this;
        }

        /**
         * Builds an immutable snapshot of the cells configured so far.
         *
         * @return an immutable board independent from this builder
         */
        public Board build() {
            return new Board(width, height, liveCells);
        }
    }
}
