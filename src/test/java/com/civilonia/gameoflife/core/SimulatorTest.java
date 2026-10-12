package com.civilonia.gameoflife.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.civilonia.gameoflife.model.Board;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SimulatorTest {

    @Test
    void writesConditionsThenTheInitialBoardAndEachFollowingGeneration() throws IOException {
        Board initial = Board.builder(3, 3)
                .setAlive(0, 1, true)
                .setAlive(1, 1, true)
                .setAlive(2, 1, true)
                .build();

        RecordingOutput output = new RecordingOutput();

        Simulator.run(initial, 1, output);

        assertEquals(new SimulationConditions(3, 3, 1), output.conditions);
        assertEquals(2, output.boards.size());
        assertSame(initial, output.boards.get(0));
        Board next = output.boards.get(1);
        assertTrue(next.isAlive(1, 0));
        assertTrue(next.isAlive(1, 1));
        assertTrue(next.isAlive(1, 2));
        assertFalse(next.isAlive(0, 1));
        assertFalse(next.isAlive(2, 1));
    }

    @Test
    void rejectsNegativeTransitionCounts() {
        assertThrows(IllegalArgumentException.class,
                () -> Simulator.run(new Board(1, 1), -1, new RecordingOutput()));
    }

    private static final class RecordingOutput implements SimulationOutput {
        private SimulationConditions conditions;
        private final List<Board> boards = new ArrayList<>();

        @Override
        public void writeConditions(SimulationConditions conditions) {
            this.conditions = conditions;
        }

        @Override
        public void writeBoard(Board board) {
            boards.add(board);
        }
    }
}
