package com.civilonia.gameoflife;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.civilonia.gameoflife.input.SimulationInput;
import com.civilonia.gameoflife.model.Board;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.jupiter.api.Test;

class GameOfLifeSimulatorTest {

    @Test
    void writesOnlyTheInitialBoardForZeroTransitions() throws IOException {
        Board initial = Board.builder(2, 2)
                .setAlive(0, 0, true)
                .setAlive(1, 1, true)
                .build();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        GameOfLifeSimulator.create(output).simulate(new SimulationInput(initial, 0));

        assertArrayEquals(bytes(
                0x47, 0x4F, 0x4C, 0x46, 0x01,
                0x00, 0x00, 0x00, 0x02,
                0x00, 0x00, 0x00, 0x02,
                0x00, 0x00, 0x00, 0x00,
                0x90), output.toByteArray());
    }

    @Test
    void writesTheInitialAndEachTransitionInOrder() throws IOException {
        Board initial = Board.builder(3, 3)
                .setAlive(0, 1, true)
                .setAlive(1, 1, true)
                .setAlive(2, 1, true)
                .build();
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        GameOfLifeSimulator.create(output).simulate(new SimulationInput(initial, 1));

        assertArrayEquals(bytes(
                0x47, 0x4F, 0x4C, 0x46, 0x01,
                0x00, 0x00, 0x00, 0x03,
                0x00, 0x00, 0x00, 0x03,
                0x00, 0x00, 0x00, 0x01,
                0x1C, 0x00, 0x49, 0x00), output.toByteArray());
    }

    @Test
    void invocationsAreIndependentAndDeterministic() throws IOException {
        SimulationInput input = new SimulationInput(Board.builder(3, 3)
                .setAlive(0, 1, true)
                .setAlive(1, 1, true)
                .setAlive(2, 1, true)
                .build(), 2);
        ByteArrayOutputStream first = new ByteArrayOutputStream();
        ByteArrayOutputStream second = new ByteArrayOutputStream();

        GameOfLifeSimulator.create(first).simulate(input);
        GameOfLifeSimulator.create(second).simulate(input);

        assertArrayEquals(first.toByteArray(), second.toByteArray());
    }

    @Test
    void leavesTheCallerOwnedStreamOpen() throws IOException {
        CloseTrackingOutputStream output = new CloseTrackingOutputStream();

        GameOfLifeSimulator.create(output).simulate(new SimulationInput(new Board(1, 1), 0));

        assertDoesNotThrow(() -> output.write(0));
        assertDoesNotThrow(output::close);
    }

    @Test
    void propagatesWriteFailures() {
        OutputStream failingOutput = new OutputStream() {
            @Override
            public void write(int value) throws IOException {
                throw new IOException("write failed");
            }
        };

        assertThrows(IOException.class, () -> GameOfLifeSimulator.create(failingOutput).simulate(
                new SimulationInput(new Board(1, 1), 0)));
    }

    private static byte[] bytes(int... values) {
        byte[] result = new byte[values.length];
        for (int index = 0; index < values.length; index++) {
            result[index] = (byte) values[index];
        }
        return result;
    }

    private static final class CloseTrackingOutputStream extends ByteArrayOutputStream {
        private boolean closed;

        @Override
        public void close() {
            closed = true;
        }

        @Override
        public synchronized void write(int value) {
            if (closed) {
                throw new IllegalStateException("stream is closed");
            }
            super.write(value);
        }
    }
}
