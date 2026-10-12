package com.civilonia.gameoflife.core;

/** Immutable metadata describing one simulation run. */
public record SimulationConditions(int width, int height, long transitions) {

    /**
     * Creates simulation metadata.
     *
     * @throws IllegalArgumentException if a dimension is not positive or the transition
     *     count is negative
     */
    public SimulationConditions {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be positive");
        }
        if (transitions < 0) {
            throw new IllegalArgumentException("transitions must not be negative");
        }
    }
}
