package com.civilonia.gameoflife.output;

import com.civilonia.gameoflife.core.SimulationConditions;
import com.civilonia.gameoflife.core.SimulationOutput;
import com.civilonia.gameoflife.model.Board;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Objects;

/**
 * Writes Game of Life simulations in version 1 of the binary output format.
 *
 * <p>The supplied stream remains owned by the caller and is never closed by this
 * encoder. If a write fails, the {@link IOException} is propagated; bytes written
 * before that failure remain in the supplied stream.</p>
 */
public final class BinaryStreamEncoder implements SimulationOutput {

    private static final int FORMAT_VERSION = 1;
    private static final long MAX_U32 = 0xFFFF_FFFFL;
    private final OutputStream output;
    private SimulationConditions conditions;

    /**
     * Creates an encoder which writes to a caller-owned stream.
     *
     * @param output stream receiving encoded bytes; it is never closed by this encoder
     */
    public BinaryStreamEncoder(OutputStream output) {
        this.output = Objects.requireNonNull(output, "output must not be null");
    }

    /**
     * Writes the binary-format header for the simulation.
     *
     * @param conditions dimensions and transition count for the simulation
     * @throws IllegalArgumentException if {@code transitions} cannot be represented as a
     *     format {@code u32}
     */
    @Override
    public void writeConditions(SimulationConditions conditions) throws IOException {
        Objects.requireNonNull(conditions, "conditions must not be null");
        if (conditions.transitions() > MAX_U32) {
            throw new IllegalArgumentException("transitions must be an unsigned 32-bit value");
        }
        writeHeader(conditions);
        this.conditions = conditions;
    }

    /**
     * Writes one board payload. Conditions must be written first.
     *
     * <p>If writing fails, the {@link IOException} is propagated and bytes already written
     * remain in the caller's stream.</p>
     */
    @Override
    public void writeBoard(Board board) throws IOException {
        Objects.requireNonNull(board, "board must not be null");
        if (conditions == null) {
            throw new IllegalStateException("writeConditions must be called before writeBoard");
        }
        if (board.width() != conditions.width() || board.height() != conditions.height()) {
            throw new IllegalArgumentException("board dimensions must match the simulation conditions");
        }
        writeBoardPayload(board);
    }

    private void writeHeader(SimulationConditions conditions) throws IOException {
        output.write('G');
        output.write('O');
        output.write('L');
        output.write('F');
        output.write(FORMAT_VERSION);
        writeUnsignedInt(conditions.width());
        writeUnsignedInt(conditions.height());
        writeUnsignedInt(conditions.transitions());
    }

    private void writeUnsignedInt(long value) throws IOException {
        output.write((int) (value >>> 24) & 0xFF);
        output.write((int) (value >>> 16) & 0xFF);
        output.write((int) (value >>> 8) & 0xFF);
        output.write((int) value & 0xFF);
    }

    private void writeBoardPayload(Board board) throws IOException {
        int bitsInByte = 0;
        int encodedByte = 0;
        for (int y = 0; y < board.height(); y++) {
            for (int x = 0; x < board.width(); x++) {
                if (board.isAlive(x, y)) {
                    encodedByte |= 1 << (7 - bitsInByte);
                }
                bitsInByte++;
                if (bitsInByte == Byte.SIZE) {
                    output.write(encodedByte);
                    bitsInByte = 0;
                    encodedByte = 0;
                }
            }
        }
        if (bitsInByte != 0) {
            output.write(encodedByte);
        }
    }
}
