package com.civilonia.gameoflife.core;

import com.civilonia.gameoflife.model.Board;
import java.io.IOException;

/** Output boundary for a simulation run. */
public interface SimulationOutput {

    /**
     * Writes the simulation conditions once, before any boards are written.
     *
     * @param conditions metadata for the complete simulation
     * @throws IOException if the output cannot be written
     */
    void writeConditions(SimulationConditions conditions) throws IOException;

    /**
     * Writes one board at its current simulation time.
     *
     * @param board board to write
     * @throws IOException if the output cannot be written
     */
    void writeBoard(Board board) throws IOException;
}
