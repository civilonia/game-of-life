# Game of Life binary input format

This document specifies version 1 of the binary format used to create a
`SimulationInput`. The format represents one initial board and its requested
number of transitions. It is deliberately distinct from `GOLF` output, which
contains every generated board.

## File layout

All multi-byte unsigned integers use big-endian byte order.

| Offset | Size | Type | Field | Value |
| ---: | ---: | --- | --- | --- |
| 0 | 4 | bytes | magic | ASCII `GOLI` (`47 4F 4C 49`) |
| 4 | 1 | `u8` | version | `1` (`01`) |
| 5 | 4 | `u32` | width | number of columns |
| 9 | 4 | `u32` | height | number of rows |
| 13 | 4 | `u32` | transitions | requested state transitions |
| 17 | variable | bytes | initial board | one packed board payload |

The initial-board payload is exactly `ceil(width * height / 8)` bytes. Its
cells use the same representation as a board in the
[binary output format](binary-output-format.md): row-major order, with the
first cell in bit 7 of the first byte. Unused low-order bits in the final byte
are padding and must be zero. No bytes may follow the payload.

## Java API

`SimulationInput.fromBinary(bytes)` validates and decodes a complete `GOLI`
document. The returned object is the DTO accepted directly by
`GameOfLifeSimulator.simulate(input)`. `input.toBinary()` emits the equivalent
version 1 document.

The Java implementation rejects dimensions or transition counts above
`Integer.MAX_VALUE`, and board dimensions whose product exceeds
`Integer.MAX_VALUE`, because those values cannot be represented by its public
`Board` and `SimulationInput` APIs.

## Example

A 5 × 5 vertical blinker with two requested transitions is encoded as:

```text
47 4F 4C 49 01 00 00 00 05 00 00 00 05 00 00 00 02
01 08 40 00
```

The final four bytes are the initial board. They can also be found as the
first board payload in the corresponding `GOLF` simulation output.
