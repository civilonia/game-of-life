package com.civilonia.gameoflife.core;

import com.civilonia.gameoflife.model.Board;
import java.io.IOException;
import java.util.Objects;

/** Runs consecutive Conway's Game of Life board states. */
public final class Simulator {

    private Simulator() {
    }

    /**
     * Runs a simulation and sends each board to the supplied output boundary.
     *
     * <p>The conditions are written first, followed by the initial board and every
     * subsequent Conway generation through {@code transitions}. The simulator owns the
     * execution loop; output implementations only receive simulation events.</p>
     *
     * @param initialBoard board at generation zero
     * @param transitions number of transitions after the initial board; must not be negative
     * @param output boundary that receives the conditions and boards
     * @throws IOException if the output boundary cannot write an event
     */
    public static void run(Board initialBoard, long transitions, SimulationOutput output)
            throws IOException {
        Objects.requireNonNull(initialBoard, "initialBoard must not be null");
        Objects.requireNonNull(output, "output must not be null");
        if (transitions < 0) {
            throw new IllegalArgumentException("transitions must not be negative");
        }

        output.writeConditions(new SimulationConditions(
                initialBoard.width(), initialBoard.height(), transitions));
        Board currentBoard = initialBoard;
        output.writeBoard(currentBoard);
        for (long generation = 0; generation < transitions; generation++) {
            currentBoard = ConwayTransition.nextGeneration(currentBoard);
            output.writeBoard(currentBoard);
        }
    }
}
