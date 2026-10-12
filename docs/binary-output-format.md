# Game of Life binary output format

This document specifies version 1 of the Game of Life simulation output
format. The key words **MUST**, **MUST NOT**, **REQUIRED**, **SHOULD**, and
**MAY** are to be interpreted as normative requirements.

The format contains simulation states only. It uses no Java object
serialization, compression, checksums, timestamps, or other metadata. A decoder
in any language can decode it using this document alone.

## Conventions

- A **byte** has eight bits.
- Multi-byte unsigned integers use big-endian byte order (most-significant byte
  first).
- `u8` is one unsigned byte and `u32` is a four-byte unsigned integer.
- Hexadecimal bytes are written as two uppercase digits, for example `4F`.
- A cell with value `1` is alive; a cell with value `0` is dead.

## File layout

Every file consists of the 17-byte header followed immediately by the board
payload. The layout is fixed; there are no optional fields.

| Offset | Size | Type | Field | Value |
| ---: | ---: | --- | --- | --- |
| 0 | 4 | bytes | magic | ASCII `GOLF` (`47 4F 4C 46`) |
| 4 | 1 | `u8` | version | `1` (`01`) |
| 5 | 4 | `u32` | width | number of columns |
| 9 | 4 | `u32` | height | number of rows |
| 13 | 4 | `u32` | transitions | number of state transitions, `N` |
| 17 | variable | bytes | payload | encoded board states |

`width` and `height` MUST each be greater than zero. The values are unsigned,
so a conforming decoder MUST retain their full `u32` range while reading them.
A decoder MAY reject dimensions or payload lengths that exceed its documented
resource limits, but it MUST do so before attempting an overflowed size
calculation or allocation.

## Generation semantics

`transitions` is the number of state transitions, not the number of stored
boards. The initial board is generation `t=0` and is always present. The
payload therefore contains exactly `N + 1` consecutive boards in this order:

```text
t=0, t=1, ..., t=N
```

For `N=0`, the payload contains exactly one board: the initial board. The
number of boards MUST be computed using a type capable of representing
`u32 + 1`; it is `4,294,967,296` when `N` is `0xFFFFFFFF`.

## Board encoding

Each board occupies:

```text
bytesPerBoard = ceil(width * height / 8)
              = (width * height + 7) / 8
```

where the multiplication and addition are performed with checked arithmetic
wide enough to represent the result. The payload length is exactly
`(N + 1) * bytesPerBoard`, again calculated with checked arithmetic.

Cells are traversed in row-major order: `(0,0)`, `(1,0)`, through
`(width-1,0)`, then `(0,1)`, continuing to `(width-1,height-1)`. This matches
the board coordinate system: `(0,0)` is the top-left cell, `x` increases right,
and `y` increases down.

The first traversed cell is stored in bit 7 (the most-significant bit) of the
first byte; the next cell is stored in bit 6, and so on. After bit 0, the next
cell starts at bit 7 of the next byte. Formally, for zero-based cell index
`i = y * width + x`:

```text
byteIndex = floor(i / 8)
bitIndex  = 7 - (i mod 8)
```

If the board's cell count is not divisible by eight, the unused low-order bits
of its final byte are padding and MUST be zero. Padding is present separately
for every board.

## Validity and error handling

A conforming decoder MUST reject the input as invalid when any of the following
is true:

- fewer than 17 header bytes are available;
- the magic bytes are not `47 4F 4C 46`;
- the version is not `01` (an unsupported version is not interpreted as version
  1);
- width or height is zero;
- the derived board or payload length overflows the decoder's checked size
  representation, or exceeds a documented resource limit;
- fewer payload bytes than the exact derived payload length are available;
- a board's final-byte padding bits are nonzero; or
- bytes remain after the exact payload (the format represents one complete
  simulation per file or bounded stream).

A decoder MUST NOT silently substitute missing bytes, ignore nonzero padding,
or treat a truncated payload as fewer generations. It MAY report distinct error
categories for malformed headers, unsupported versions, oversized input, and
truncated input.

## Reference examples

### A 3 × 3 initial board (`N=0`)

The following single board has live cells at `(0,0)`, `(2,0)`, `(1,1)`,
`(0,2)`, and `(2,2)`:

```text
row 0: 1 0 1
row 1: 0 1 0
row 2: 1 0 1
```

Its row-major cell bits are `101010101`. The first eight cells form `AA`; the
ninth cell is the most-significant bit of the next byte, making that byte
`80`. Its remaining seven padding bits are zero. The complete 19-byte file is:

```text
47 4F 4C 46 01 00 00 00 03 00 00 00 03 00 00 00 00 AA 80
```

This example demonstrates the required handling of a cell count that is not a
multiple of eight.

### A 2 × 2 simulation with one transition (`N=1`)

At `t=0`, the live cells are `(0,0)` and `(1,1)`, producing bits `1001` and
the byte `90` followed by four zero padding bits. With Conway's rules, both
cells die at `t=1`, so the second board byte is `00`. The complete file is:

```text
47 4F 4C 46 01 00 00 00 02 00 00 00 02 00 00 00 01 90 00
```

The final two bytes are respectively the `t=0` and `t=1` boards. They show
that a value of `N=1` stores two boards, rather than one.
