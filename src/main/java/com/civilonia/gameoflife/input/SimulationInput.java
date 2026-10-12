package com.civilonia.gameoflife.input;

import com.civilonia.gameoflife.model.Board;
import java.util.Objects;

/**
 * Immutable input for a finite, deterministic Game of Life simulation.
 *
 * <p>A generation count is the number of state transitions to perform. Zero is
 * valid and represents a simulation containing only the supplied initial board.</p>
 */
public final class SimulationInput {

    private static final int HEADER_LENGTH = 17;
    private static final int FORMAT_VERSION = 1;
    private final Board initialBoard;
    private final int generations;

    /**
     * Creates simulation input from an initial board and transition count.
     *
     * @param initialBoard immutable board at generation zero; must not be null
     * @param generations number of state transitions to simulate; must not be negative
     * @throws NullPointerException if {@code initialBoard} is null
     * @throws IllegalArgumentException if {@code generations} is negative
     */
    public SimulationInput(Board initialBoard, int generations) {
        this.initialBoard = Objects.requireNonNull(initialBoard, "initialBoard must not be null");
        if (generations < 0) {
            throw new IllegalArgumentException("generations must not be negative");
        }
        this.generations = generations;
    }

    /** Returns the immutable board at generation zero. */
    public Board initialBoard() {
        return initialBoard;
    }

    /** Returns the non-negative number of state transitions to simulate. */
    public int generations() {
        return generations;
    }

    /**
     * Encodes this input in version 1 of the {@code GOLI} binary input format.
     *
     * <p>The format stores the initial board only. Its board payload uses the same row-major,
     * most-significant-bit-first representation as the {@code GOLF} output format.</p>
     *
     * @return a complete, independently owned binary input document
     */
    public byte[] toBinary() {
        int bytesPerBoard = bytesPerBoard(initialBoard.width(), initialBoard.height());
        byte[] encoded = new byte[HEADER_LENGTH + bytesPerBoard];
        encoded[0] = 'G';
        encoded[1] = 'O';
        encoded[2] = 'L';
        encoded[3] = 'I';
        encoded[4] = FORMAT_VERSION;
        writeUnsignedInt(encoded, 5, initialBoard.width());
        writeUnsignedInt(encoded, 9, initialBoard.height());
        writeUnsignedInt(encoded, 13, generations);

        for (int y = 0; y < initialBoard.height(); y++) {
            for (int x = 0; x < initialBoard.width(); x++) {
                if (initialBoard.isAlive(x, y)) {
                    int cellIndex = y * initialBoard.width() + x;
                    encoded[HEADER_LENGTH + cellIndex / Byte.SIZE] |= (byte) (1 << (7 - cellIndex % Byte.SIZE));
                }
            }
        }
        return encoded;
    }

    /**
     * Decodes a complete version 1 {@code GOLI} binary input document.
     *
     * @param encoded the entire binary input document
     * @return simulation input ready to pass to {@code GameOfLifeSimulator.simulate}
     * @throws NullPointerException if {@code encoded} is null
     * @throws IllegalArgumentException if the document is malformed, unsupported, or exceeds the
     *     limits of this Java implementation
     */
    public static SimulationInput fromBinary(byte[] encoded) {
        Objects.requireNonNull(encoded, "encoded must not be null");
        if (encoded.length < HEADER_LENGTH) {
            throw new IllegalArgumentException("input is shorter than the 17-byte header");
        }
        if (encoded[0] != 'G' || encoded[1] != 'O' || encoded[2] != 'L' || encoded[3] != 'I') {
            throw new IllegalArgumentException("input magic must be GOLI");
        }
        if (Byte.toUnsignedInt(encoded[4]) != FORMAT_VERSION) {
            throw new IllegalArgumentException("unsupported input format version");
        }

        long encodedWidth = readUnsignedInt(encoded, 5);
        long encodedHeight = readUnsignedInt(encoded, 9);
        long encodedGenerations = readUnsignedInt(encoded, 13);
        if (encodedWidth == 0 || encodedHeight == 0) {
            throw new IllegalArgumentException("input width and height must be positive");
        }
        if (encodedWidth > Integer.MAX_VALUE || encodedHeight > Integer.MAX_VALUE
                || encodedGenerations > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("input values exceed this implementation's limits");
        }

        int width = (int) encodedWidth;
        int height = (int) encodedHeight;
        if ((long) width * height > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("input board dimensions exceed this implementation's limits");
        }
        int bytesPerBoard = bytesPerBoard(width, height);
        if (encoded.length != HEADER_LENGTH + bytesPerBoard) {
            throw new IllegalArgumentException("input payload length does not match its dimensions");
        }
        validatePadding(encoded, bytesPerBoard, width, height);

        Board.Builder board = Board.builder(width, height);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int cellIndex = y * width + x;
                boolean alive = (encoded[HEADER_LENGTH + cellIndex / Byte.SIZE]
                        & (1 << (7 - cellIndex % Byte.SIZE))) != 0;
                if (alive) {
                    board.setAlive(x, y, true);
                }
            }
        }
        return new SimulationInput(board.build(), (int) encodedGenerations);
    }

    private static int bytesPerBoard(int width, int height) {
        long cellCount = Math.multiplyExact((long) width, height);
        return Math.toIntExact((cellCount + Byte.SIZE - 1) / Byte.SIZE);
    }

    private static void writeUnsignedInt(byte[] target, int offset, int value) {
        target[offset] = (byte) (value >>> 24);
        target[offset + 1] = (byte) (value >>> 16);
        target[offset + 2] = (byte) (value >>> 8);
        target[offset + 3] = (byte) value;
    }

    private static long readUnsignedInt(byte[] source, int offset) {
        return ((long) Byte.toUnsignedInt(source[offset]) << 24)
                | ((long) Byte.toUnsignedInt(source[offset + 1]) << 16)
                | ((long) Byte.toUnsignedInt(source[offset + 2]) << 8)
                | Byte.toUnsignedInt(source[offset + 3]);
    }

    private static void validatePadding(byte[] encoded, int bytesPerBoard, int width, int height) {
        int cellsInFinalByte = (int) ((long) width * height % Byte.SIZE);
        if (cellsInFinalByte == 0) {
            return;
        }
        int paddingMask = (1 << (Byte.SIZE - cellsInFinalByte)) - 1;
        int finalByte = Byte.toUnsignedInt(encoded[HEADER_LENGTH + bytesPerBoard - 1]);
        if ((finalByte & paddingMask) != 0) {
            throw new IllegalArgumentException("input board padding bits must be zero");
        }
    }
}
