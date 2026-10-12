package com.civilonia.gameoflife.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.civilonia.gameoflife.input.SimulationInput;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class ExampleGoliResourcesTest {

    @Test
    void bundledGoliInputsDecodeToTheirDocumentedSimulations() throws IOException {
        assertInput("goli/empty-3x3-zero.goli", 3, 3, 0, bytes(
                0x47, 0x4F, 0x4C, 0x49, 0x01,
                0x00, 0x00, 0x00, 0x03,
                0x00, 0x00, 0x00, 0x03,
                0x00, 0x00, 0x00, 0x00,
                0x00, 0x00));
        assertInput("goli/blinker-5x5-two-transitions.goli", 5, 5, 2, bytes(
                0x47, 0x4F, 0x4C, 0x49, 0x01,
                0x00, 0x00, 0x00, 0x05,
                0x00, 0x00, 0x00, 0x05,
                0x00, 0x00, 0x00, 0x02,
                0x01, 0x08, 0x40, 0x00));
        assertInput("goli/glider-5x5-four-transitions.goli", 5, 5, 4, bytes(
                0x47, 0x4F, 0x4C, 0x49, 0x01,
                0x00, 0x00, 0x00, 0x05,
                0x00, 0x00, 0x00, 0x05,
                0x00, 0x00, 0x00, 0x04,
                0x41, 0x38, 0x00, 0x00));
    }

    private static void assertInput(String resourceName, int width, int height, int transitions,
            byte[] expectedBytes) throws IOException {
        try (InputStream resource = Program.class.getClassLoader().getResourceAsStream(resourceName)) {
            assertNotNull(resource, "missing example resource: " + resourceName);
            byte[] encoded = resource.readAllBytes();
            assertArrayEquals(expectedBytes, encoded);
            SimulationInput input = SimulationInput.fromBinary(encoded);
            assertEquals(width, input.initialBoard().width());
            assertEquals(height, input.initialBoard().height());
            assertEquals(transitions, input.generations());
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
