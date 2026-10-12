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
}
