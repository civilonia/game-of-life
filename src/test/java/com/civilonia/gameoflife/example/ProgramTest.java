package com.civilonia.gameoflife.example;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.civilonia.gameoflife.input.SimulationInput;
import com.civilonia.gameoflife.model.Board;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ProgramTest {

    @Test
    void decodesGoliArgumentAndWritesTheSimulation(@TempDir Path temporaryDirectory)
            throws IOException {
        Path inputPath = temporaryDirectory.resolve("blinker.goli");
        Path outputPath = temporaryDirectory.resolve("result/output.golf");
        SimulationInput input = new SimulationInput(Board.builder(5, 5)
                .setAlive(2, 1, true)
                .setAlive(2, 2, true)
                .setAlive(2, 3, true)
                .build(), 2);
        Files.write(inputPath, input.toBinary());

        Program.main(new String[] {inputPath.toString(), outputPath.toString()});

        assertArrayEquals(bytes(
                0x47, 0x4F, 0x4C, 0x46, 0x01,
                0x00, 0x00, 0x00, 0x05,
                0x00, 0x00, 0x00, 0x05,
                0x00, 0x00, 0x00, 0x02,
                0x01, 0x08, 0x40, 0x00,
                0x00, 0x1C, 0x00, 0x00,
                0x01, 0x08, 0x40, 0x00), Files.readAllBytes(outputPath));
    }

    @Test
    void requiresAnInputAndOutputPath() {
        assertThrows(IllegalArgumentException.class, () -> Program.main(new String[0]));
    }

    @Test
    void writesToStandardOutputWhenOutputPathIsOmitted(@TempDir Path temporaryDirectory)
            throws IOException {
        Path inputPath = temporaryDirectory.resolve("input.goli");
        Files.write(inputPath, new SimulationInput(new Board(1, 1), 0).toBinary());
        PrintStream originalStandardOutput = System.out;
        ByteArrayOutputStream capturedOutput = new ByteArrayOutputStream();

        try {
            System.setOut(new PrintStream(capturedOutput));
            Program.main(new String[] {inputPath.toString()});
        } finally {
            System.setOut(originalStandardOutput);
        }

        assertArrayEquals(bytes(
                0x47, 0x4F, 0x4C, 0x46, 0x01,
                0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x00,
                0x00), capturedOutput.toByteArray());
    }

    @Test
    void rejectsSimulationsThatExceedTheDefaultTransitionLimit(@TempDir Path temporaryDirectory)
            throws IOException {
        Path inputPath = temporaryDirectory.resolve("too-many-transitions.goli");
        Files.write(inputPath, new SimulationInput(new Board(1, 1), 10_001).toBinary());

        assertThrows(IllegalArgumentException.class,
                () -> Program.main(new String[] {inputPath.toString()}));
    }

    @Test
    void reportsStandardOutputWriteFailures(@TempDir Path temporaryDirectory) throws IOException {
        Path inputPath = temporaryDirectory.resolve("input.goli");
        Files.write(inputPath, new SimulationInput(new Board(1, 1), 0).toBinary());
        PrintStream originalStandardOutput = System.out;
        OutputStream failingOutput = new OutputStream() {
            @Override
            public void write(int value) throws IOException {
                throw new IOException("simulated standard output failure");
            }
        };

        try {
            System.setOut(new PrintStream(failingOutput));
            assertThrows(IOException.class, () -> Program.main(new String[] {inputPath.toString()}));
        } finally {
            System.setOut(originalStandardOutput);
        }
    }

    @Test
    void boundsInputReadsUsingTheConfiguredLimit(@TempDir Path temporaryDirectory) throws IOException {
        Path inputPath = temporaryDirectory.resolve("oversized.goli");
        Files.write(inputPath, new byte[] {0, 0, 0});
        String previousLimit = System.getProperty("gameoflife.maxInputBytes");

        try {
            System.setProperty("gameoflife.maxInputBytes", "2");
            assertThrows(IllegalArgumentException.class,
                    () -> Program.main(new String[] {inputPath.toString()}));
        } finally {
            restoreProperty("gameoflife.maxInputBytes", previousLimit);
        }
    }

    private static void restoreProperty(String propertyName, String previousValue) {
        if (previousValue == null) {
            System.clearProperty(propertyName);
        } else {
            System.setProperty(propertyName, previousValue);
        }
    }

    private static byte[] bytes(int... values) {
        byte[] result = new byte[values.length];
        for (int index = 0; index < values.length; index++) {
            result[index] = (byte) values[index];
        }
        return result;
    }
}
