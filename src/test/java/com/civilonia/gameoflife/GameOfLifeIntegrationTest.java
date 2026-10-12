package com.civilonia.gameoflife;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.civilonia.gameoflife.input.SimulationInput;
import com.civilonia.gameoflife.model.Board;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * End-to-end tests for the public simulation API.
 *
 * <p>The byte fixtures in this class are written from the documented format and expected board
 * states. They deliberately do not use {@code BinaryStreamEncoder} as an oracle.</p>
 */
class GameOfLifeIntegrationTest {

    @Test
    void encodesAnEmptyBoardAndAStableBlockUsingFixedReferenceBytes() throws IOException {
        assertSimulationBytes(new Board(3, 3), 0, bytes(
                0x47, 0x4F, 0x4C, 0x46, 0x01,
                0x00, 0x00, 0x00, 0x03,
                0x00, 0x00, 0x00, 0x03,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00));

        Board block = Board.builder(4, 4)
                .setAlive(1, 1, true).setAlive(2, 1, true)
                .setAlive(1, 2, true).setAlive(2, 2, true)
                .build();
        assertSimulationBytes(block, 2, bytes(
                0x47, 0x4F, 0x4C, 0x46, 0x01,
                0x00, 0x00, 0x00, 0x04,
                0x00, 0x00, 0x00, 0x04,
                0x00, 0x00, 0x00, 0x02,
                0x06, 0x60, 0x06, 0x60, 0x06, 0x60));
    }

    @Test
    void encodesOscillatorAndMovingPatternThroughEachGeneration() throws IOException {
        Board blinker = Board.builder(5, 5)
                .setAlive(2, 1, true).setAlive(2, 2, true).setAlive(2, 3, true)
                .build();
        assertSimulationBytes(blinker, 2, bytes(
                0x47, 0x4F, 0x4C, 0x46, 0x01,
                0x00, 0x00, 0x00, 0x05,
                0x00, 0x00, 0x00, 0x05,
                0x00, 0x00, 0x00, 0x02,
                0x01, 0x08, 0x40, 0x00,
                0x00, 0x1C, 0x00, 0x00,
                0x01, 0x08, 0x40, 0x00));

        Board glider = Board.builder(5, 5)
                .setAlive(1, 0, true).setAlive(2, 1, true)
                .setAlive(0, 2, true).setAlive(1, 2, true).setAlive(2, 2, true)
                .build();
        assertSimulationBytes(glider, 4, bytes(
                0x47, 0x4F, 0x4C, 0x46, 0x01,
                0x00, 0x00, 0x00, 0x05,
                0x00, 0x00, 0x00, 0x05,
                0x00, 0x00, 0x00, 0x04,
                0x41, 0x38, 0x00, 0x00,
                0x05, 0x18, 0x80, 0x00,
                0x01, 0x28, 0xC0, 0x00,
                0x02, 0x0C, 0xC0, 0x00,
                0x01, 0x04, 0xE0, 0x00));
    }

    @Test
    void includesExactlyOneInitialBoardForZeroTransitionsAndNPlusOneBoardsOtherwise()
            throws IOException {
        Board board = Board.builder(3, 3).setAlive(0, 0, true).build();

        assertEquals(19, simulate(board, 0).length, "17-byte header plus one 2-byte board");
        assertEquals(25, simulate(board, 3).length, "17-byte header plus four 2-byte boards");
    }

    @Test
    void isByteForByteReproducibleAcrossIndependentRuns() throws IOException {
        Board input = Board.builder(5, 5)
                .setAlive(1, 0, true).setAlive(2, 1, true)
                .setAlive(0, 2, true).setAlive(1, 2, true).setAlive(2, 2, true)
                .build();

        assertArrayEquals(simulate(input, 4), simulate(input, 4));
    }

    @Test
    void rejectsInvalidInputAndPropagatesOutputStreamFailures() {
        assertThrows(IllegalArgumentException.class, () -> new SimulationInput(new Board(1, 1), -1));

        OutputStream failsAfterHeader = new OutputStream() {
            private int writesRemaining = 17;

            @Override
            public void write(int value) throws IOException {
                if (writesRemaining-- == 0) {
                    throw new IOException("simulated stream failure");
                }
            }
        };
        assertThrows(IOException.class, () -> GameOfLifeSimulator.create(failsAfterHeader)
                .simulate(new SimulationInput(new Board(1, 1), 0)));
    }

    @Test
    void builtJarCanBeCompiledAgainstAndRunByAnIndependentJavaConsumer(@TempDir Path temporaryDir)
            throws Exception {
        Path libraryJar = Path.of(System.getProperty("libraryJar"));
        assertTrue(Files.isRegularFile(libraryJar), "test task must provide the built library JAR");

        Path sourceDirectory = Files.createDirectories(temporaryDir.resolve("consumer-src/example"));
        Path classesDirectory = Files.createDirectories(temporaryDir.resolve("consumer-classes"));
        Path sourceFile = sourceDirectory.resolve("Consumer.java");
        Files.writeString(sourceFile, """
                package example;
                import com.civilonia.gameoflife.GameOfLifeSimulator;
                import com.civilonia.gameoflife.input.SimulationInput;
                import com.civilonia.gameoflife.model.Board;
                import java.io.ByteArrayOutputStream;
                public final class Consumer {
                    public static void main(String[] args) throws Exception {
                        ByteArrayOutputStream output = new ByteArrayOutputStream();
                        GameOfLifeSimulator.create(output).simulate(new SimulationInput(new Board(1, 1), 0));
                        if (output.toByteArray().length != 18) throw new AssertionError("unexpected output");
                    }
                }
                """);

        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        assertTrue(compiler != null, "tests require a JDK compiler");
        assertEquals(0, compiler.run(null, null, null,
                "-classpath", libraryJar.toString(), "-d", classesDirectory.toString(), sourceFile.toString()));

        try (URLClassLoader consumerLoader = new URLClassLoader(new URL[] {
                classesDirectory.toUri().toURL(), libraryJar.toUri().toURL()
        }, ClassLoader.getPlatformClassLoader())) {
            consumerLoader.loadClass("example.Consumer").getMethod("main", String[].class)
                    .invoke(null, (Object) new String[0]);
        }
    }

    private static void assertSimulationBytes(Board board, int transitions, byte[] expected)
            throws IOException {
        assertArrayEquals(expected, simulate(board, transitions));
    }

    private static byte[] simulate(Board board, int transitions) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        GameOfLifeSimulator.create(output).simulate(new SimulationInput(board, transitions));
        return output.toByteArray();
    }

    private static byte[] bytes(int... values) {
        byte[] result = new byte[values.length];
        for (int index = 0; index < values.length; index++) {
            result[index] = (byte) values[index];
        }
        return result;
    }
}
