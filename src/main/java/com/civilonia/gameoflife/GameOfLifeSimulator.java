package com.civilonia.gameoflife;

import com.civilonia.gameoflife.core.ConwayTransition;
import com.civilonia.gameoflife.core.SimulationConditions;
import com.civilonia.gameoflife.core.SimulationOutput;
import com.civilonia.gameoflife.input.SimulationInput;
import com.civilonia.gameoflife.model.Board;
import com.civilonia.gameoflife.output.BinaryStreamEncoder;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Public entry point for a synchronous, finite Game of Life simulation.
 *
 * <p>Use {@link #create(OutputStream)} to create an independent simulator for a caller-owned
 * output stream. The stream is never closed by this class.</p>
 */
public final class GameOfLifeSimulator {

    private final SimulationOutput output;

    private GameOfLifeSimulator(OutputStream output) {
        this.output = new BinaryStreamEncoder(output);
    }

    /**
     * Creates a simulator that writes version 1 binary output to {@code output}.
     *
     * <p>Each call creates an independent simulator and encoder, so no simulation state is
     * shared. The caller retains ownership of the supplied stream.</p>
     *
     * @param output destination for the binary simulation result; it is never closed
     * @return a new simulator configured for {@code output}
     * @throws NullPointerException if {@code output} is null
     */
    public static GameOfLifeSimulator create(OutputStream output) {
        return new GameOfLifeSimulator(Objects.requireNonNull(output, "output must not be null"));
    }

    /**
     * Simulates {@code input}, writing the board at {@code t=0} and every transition through
     * {@code t=N}.
     *
     * <p>Zero transitions produce exactly one board. If writing fails, the {@link IOException}
     * is propagated and the output stream can contain a partial simulation.</p>
     *
     * @param input validated simulation input
     * @throws NullPointerException if {@code input} is null
     * @throws IOException if the configured output cannot be written
     */
    public void simulate(SimulationInput input) throws IOException {
        Objects.requireNonNull(input, "input must not be null");

        Board initialBoard = input.initialBoard();
        int transitions = input.generations();
        output.writeConditions(new SimulationConditions(
                initialBoard.width(), initialBoard.height(), transitions));

        Board currentBoard = initialBoard;
        output.writeBoard(currentBoard);
        for (int generation = 0; generation < transitions; generation++) {
            currentBoard = ConwayTransition.nextGeneration(currentBoard);
            output.writeBoard(currentBoard);
        }
    }
}
