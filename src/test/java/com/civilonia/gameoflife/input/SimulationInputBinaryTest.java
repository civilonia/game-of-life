package com.civilonia.gameoflife.input;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.civilonia.gameoflife.GameOfLifeSimulator;
import com.civilonia.gameoflife.model.Board;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class SimulationInputBinaryTest {

    @Test
    void encodesAndDecodesTheDocumentedGoliExample() {
        SimulationInput input = new SimulationInput(Board.builder(5, 5)
                .setAlive(2, 1, true)
                .setAlive(2, 2, true)
                .setAlive(2, 3, true)
                .build(), 2);

        byte[] encoded = input.toBinary();

        assertArrayEquals(bytes(
                0x47, 0x4F, 0x4C, 0x49, 0x01,
                0x00, 0x00, 0x00, 0x05,
                0x00, 0x00, 0x00, 0x05,
                0x00, 0x00, 0x00, 0x02,
                0x01, 0x08, 0x40, 0x00), encoded);

        SimulationInput decoded = SimulationInput.fromBinary(encoded);
        assertEquals(2, decoded.generations());
        assertEquals(5, decoded.initialBoard().width());
        assertEquals(5, decoded.initialBoard().height());
        assertTrue(decoded.initialBoard().isAlive(2, 1));
        assertTrue(decoded.initialBoard().isAlive(2, 2));
        assertTrue(decoded.initialBoard().isAlive(2, 3));
        assertFalse(decoded.initialBoard().isAlive(1, 2));
    }

    @Test
    void decodedInputCanBePassedDirectlyToTheSimulator() throws IOException {
        SimulationInput decoded = SimulationInput.fromBinary(bytes(
                0x47, 0x4F, 0x4C, 0x49, 0x01,
                0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x00,
                0x80));
        ByteArrayOutputStream output = new ByteArrayOutputStream();

        GameOfLifeSimulator.create(output).simulate(decoded);

        assertArrayEquals(bytes(
                0x47, 0x4F, 0x4C, 0x46, 0x01,
                0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x00,
                0x80), output.toByteArray());
    }

    @Test
    void rejectsMalformedDocuments() {
        assertThrows(IllegalArgumentException.class, () -> SimulationInput.fromBinary(new byte[16]));
        assertThrows(IllegalArgumentException.class, () -> SimulationInput.fromBinary(bytes(
                0x47, 0x4F, 0x4C, 0x46, 0x01,
                0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x00,
                0x00)));
        assertThrows(IllegalArgumentException.class, () -> SimulationInput.fromBinary(bytes(
                0x47, 0x4F, 0x4C, 0x49, 0x01,
                0x00, 0x00, 0x00, 0x03,
                0x00, 0x00, 0x00, 0x03,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x01)));
    }

    @Test
    void rejectsEveryOtherDocumentValidationFailure() {
        assertThrows(IllegalArgumentException.class, () -> SimulationInput.fromBinary(bytes(
                0x47, 0x4F, 0x4C, 0x49, 0x02,
                0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x00,
                0x00)));
        assertThrows(IllegalArgumentException.class, () -> SimulationInput.fromBinary(bytes(
                0x47, 0x4F, 0x4C, 0x49, 0x01,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x00,
                0x00)));
        assertThrows(IllegalArgumentException.class, () -> SimulationInput.fromBinary(bytes(
                0x47, 0x4F, 0x4C, 0x49, 0x01,
                0x80, 0x00, 0x00, 0x00,
                0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x00)));
        assertThrows(IllegalArgumentException.class, () -> SimulationInput.fromBinary(bytes(
                0x47, 0x4F, 0x4C, 0x49, 0x01,
                0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x00)));
        assertThrows(IllegalArgumentException.class, () -> SimulationInput.fromBinary(bytes(
                0x47, 0x4F, 0x4C, 0x49, 0x01,
                0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x01,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00)));
    }

    private static byte[] bytes(int... values) {
        byte[] result = new byte[values.length];
        for (int index = 0; index < values.length; index++) {
            result[index] = (byte) values[index];
        }
        return result;
    }
}
