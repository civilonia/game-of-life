package com.civilonia.gameoflife.output;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.civilonia.gameoflife.core.SimulationConditions;
import com.civilonia.gameoflife.core.Simulator;
import com.civilonia.gameoflife.model.Board;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.junit.jupiter.api.Test;

class BinaryStreamEncoderTest {

    @Test
    void encodesTheNonByteAlignedReferenceFixture() throws IOException {
        Board board = Board.builder(3, 3)
                .setAlive(0, 0, true)
                .setAlive(2, 0, true)
                .setAlive(1, 1, true)
                .setAlive(0, 2, true)
                .setAlive(2, 2, true)
                .build();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Simulator.run(board, 0, new BinaryStreamEncoder(output));

        assertArrayEquals(bytes(
                0x47, 0x4F, 0x4C, 0x46, 0x01,
                0x00, 0x00, 0x00, 0x03,
                0x00, 0x00, 0x00, 0x03,
                0x00, 0x00, 0x00, 0x00,
                0xAA, 0x80), output.toByteArray());
    }

    @Test
    void encodesTheInitialAndFollowingReferenceBoardsInOrder() throws IOException {
        Board board = Board.builder(2, 2)
                .setAlive(0, 0, true)
                .setAlive(1, 1, true)
                .build();

        ByteArrayOutputStream output = new ByteArrayOutputStream();
        Simulator.run(board, 1, new BinaryStreamEncoder(output));

        assertArrayEquals(bytes(
                0x47, 0x4F, 0x4C, 0x46, 0x01,
                0x00, 0x00, 0x00, 0x02,
                0x00, 0x00, 0x00, 0x02,
                0x00, 0x00, 0x00, 0x01,
                0x90, 0x00), output.toByteArray());
    }

    @Test
    void producesIdenticalBytesForIdenticalInput() throws IOException {
        Board board = Board.builder(4, 2)
                .setAlive(0, 0, true)
                .setAlive(3, 1, true)
                .build();
        ByteArrayOutputStream first = new ByteArrayOutputStream();
        ByteArrayOutputStream second = new ByteArrayOutputStream();

        Simulator.run(board, 3, new BinaryStreamEncoder(first));
        Simulator.run(board, 3, new BinaryStreamEncoder(second));

        assertArrayEquals(first.toByteArray(), second.toByteArray());
    }

    @Test
    void leavesTheCallerOwnedStreamOpen() throws IOException {
        CloseTrackingOutputStream output = new CloseTrackingOutputStream();

        Board board = new Board(1, 1);
        Simulator.run(board, 0, new BinaryStreamEncoder(output));

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

        assertThrows(IOException.class,
                () -> {
                    Board board = new Board(1, 1);
                    Simulator.run(board, 0, new BinaryStreamEncoder(failingOutput));
                });
    }

    @Test
    void rejectsTransitionsOutsideTheUnsigned32BitRange() {
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        assertThrows(IllegalArgumentException.class,
                () -> new BinaryStreamEncoder(output).writeConditions(
                        new SimulationConditions(1, 1, 0x1_0000_0000L)));
        assertThrows(IllegalArgumentException.class,
                () -> new SimulationConditions(1, 1, -1));
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
