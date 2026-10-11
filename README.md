# Game of Life

A dependency-free Java library for deterministic simulations of Conway's Game
of Life. It is intended to be reusable by Java applications, including
Minecraft Fabric mods, while remaining independent of any framework, filesystem,
or network API.

## Project status

The project currently provides the Java 21 Gradle library scaffold. The board
model, simulation API, binary encoder, publishing workflow, and standalone
example are specified in the project's issues and will be added incrementally.
Sections marked **Planned API** or **Planned workflow** describe that agreed
contract; they are not available in the current artifact.

## Conway's Game of Life

The library simulates a finite, two-dimensional grid of cells. Each cell is
either alive or dead. For each synchronous transition, the eight surrounding
cells (the Moore neighborhood) determine the next state:

- A live cell with fewer than two live neighbors dies.
- A live cell with two or three live neighbors survives.
- A live cell with more than three live neighbors dies.
- A dead cell with exactly three live neighbors becomes alive.

Cells outside the board are always dead. Boards do not wrap at their edges.

## Requirements and build

- Java 21
- No runtime dependencies outside the Java standard library
- Gradle Wrapper (included)

On macOS or Linux:

```bash
./gradlew build
./gradlew test
```

On Windows:

```bat
gradlew.bat build
gradlew.bat test
```

`build` compiles the library, runs the JUnit 5 test suite, and creates the JAR
under `build/libs/`. The project deliberately does not use JPMS and has no
`module-info.java`.

## Architecture

The package layout separates state, computation, input, and output:

```text
com.civilonia.gameoflife
├── model   # Immutable board state and coordinates
├── core    # Deterministic Conway transitions and generation evolution
├── input   # Immutable simulation input and validation
└── output  # Binary encoding to caller-provided streams
```

The `core` package must remain free of I/O. The library will not add Spring,
Fabric, JSON libraries, logging frameworks, or other runtime dependencies.

### Board and coordinate contract

The planned board model is immutable: callers can inspect dimensions and cell
states, but cannot modify a constructed board or mutate its retained input.

- Coordinates are zero-based.
- The origin is the top-left cell.
- `x` increases to the right and `y` increases downward.
- Invalid dimensions and invalid coordinates are rejected.
- Positions outside the finite board are dead; no toroidal wrapping is used.

## Simulation semantics

A simulation input consists of an initial immutable board and a non-negative
number of transitions, `N`. A zero transition count is valid.

`N` is the number of state transitions, not the number of encoded boards. A
simulation always includes the initial state:

```text
t=0, t=1, ..., t=N
```

Consequently, it produces exactly `N + 1` board states. For `N = 0`, the output
contains exactly the initial board at `t=0`. Each invocation is an independent,
sequential, deterministic experiment: identical inputs must produce identical
states and identical encoded bytes.

## Planned public API

The public entry point will be `com.civilonia.gameoflife.LifeGameSimulator`.
It will accept validated simulation input and a caller-provided `OutputStream`,
evolve the board synchronously, and encode states from `t=0` through `t=N`.

Illustrative usage after the model and API issues are complete:

```java
Board initialBoard = /* construct an immutable board */;
SimulationInput input = new SimulationInput(initialBoard, 10);

try (OutputStream output = Files.newOutputStream(Path.of("game-of-life.bin"))) {
    new LifeGameSimulator().simulate(input, output);
}
```

The exact constructors and method names other than `LifeGameSimulator` are not
implemented yet and will be documented with the released API. The example
application, rather than the library, owns file creation and closes its file
stream. The library must not close a caller-provided stream; `IOException`s are
propagated and a failed write may leave partial output in that stream.

## Binary output

The planned binary format is deterministic, versioned, and independent of Java
serialization. Its header will identify the format and version and record the
board width, height, and transition count `N`. The payload will contain exactly
`N + 1` states in chronological order, beginning with `t=0`.

The forthcoming specification will define the magic bytes, header layout,
byte order, cell traversal order, bit order, padding for board sizes not
divisible by eight, and the handling of invalid, unsupported, or truncated
data. It will also include manually verifiable byte-level fixtures. Compression
and extra metadata are intentionally out of scope for the initial format.

## Planned standalone example

A small Java `main` application will demonstrate a fixed, reproducible pattern
without Minecraft or a server. It will construct input through the public API,
run `LifeGameSimulator`, and write a binary result file. Execution commands,
output path, and expected result will be documented here when that example is
implemented.

## Publishing and consumption

The intended Maven coordinates are:

```text
com.civilonia:game-of-life
```