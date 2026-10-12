package com.civilonia.gameoflife.example;

import com.civilonia.gameoflife.GameOfLifeSimulator;
import com.civilonia.gameoflife.input.SimulationInput;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Runs a Game of Life simulation from a {@code GOLI} input file.
 */
public final class Program {

    private Program() {
    }

    /**
     * Reads a {@code GOLI} input file and writes the resulting {@code GOLF} simulation.
     *
     * @param args {@code <input.goli> [output.golf]}; without an output path, writes binary data
     *     to standard output
     * @throws IOException if an input or output file cannot be read or written
     * @throws IllegalArgumentException if the arguments or the {@code GOLI} document are invalid
     */
    public static void main(String[] args) throws IOException {
        if (args.length < 1 || args.length > 2) {
            throw new IllegalArgumentException("usage: Program <input.goli> [output.golf]");
        }

        Path inputPath = Path.of(args[0]);
        SimulationInput input = SimulationInput.fromBinary(Files.readAllBytes(inputPath));

        if (args.length == 1) {
            GameOfLifeSimulator.create(System.out).simulate(input);
            System.out.flush();
            return;
        }

        Path outputPath = Path.of(args[1]);
        Path outputDirectory = outputPath.getParent();
        if (outputDirectory != null) {
            Files.createDirectories(outputDirectory);
        }
        try (OutputStream output = Files.newOutputStream(outputPath)) {
            GameOfLifeSimulator.create(output).simulate(input);
        }

        System.err.println("Wrote " + outputPath);
    }
}
