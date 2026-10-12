package com.civilonia.gameoflife.example;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.civilonia.gameoflife.input.SimulationInput;
import java.io.IOException;
import java.io.InputStream;
import org.junit.jupiter.api.Test;

class ExampleGoliResourcesTest {

    @Test
    void bundledGoliInputsDecodeToTheirDocumentedSimulations() throws IOException {
        assertInput("goli/empty-3x3-zero.goli", 3, 3, 0);
        assertInput("goli/blinker-5x5-two-transitions.goli", 5, 5, 2);
        assertInput("goli/glider-5x5-four-transitions.goli", 5, 5, 4);
    }

    private static void assertInput(String resourceName, int width, int height, int transitions)
            throws IOException {
        try (InputStream resource = Program.class.getClassLoader().getResourceAsStream(resourceName)) {
            assertNotNull(resource, "missing example resource: " + resourceName);
            SimulationInput input = SimulationInput.fromBinary(resource.readAllBytes());
            assertEquals(width, input.initialBoard().width());
            assertEquals(height, input.initialBoard().height());
            assertEquals(transitions, input.generations());
        }
    }
}
