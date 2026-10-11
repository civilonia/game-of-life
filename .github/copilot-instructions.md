# Game of Life review instructions

Review changes as advisory feedback. The repository maintainer retains final
approval and merge authority.

Prioritize the following, in order:

1. **Simulation correctness:** Conway transitions must be synchronous, use the
   eight-cell Moore neighborhood, and treat cells outside the finite board as
   dead. Do not introduce edge wrapping unless the public contract explicitly
   changes.
2. **Determinism and reproducibility:** Identical inputs must produce identical
   board states and output bytes. Avoid unordered iteration, hidden global
   state, time-dependent behavior, and non-deterministic encodings.
3. **Immutability:** Board and simulation input state must not expose mutable
   internals or retain caller-owned mutable data without defensive copying.
4. **Separation of concerns:** Keep model, simulation computation, and I/O
   distinct. Core simulation code must remain free of filesystem, network, and
   framework dependencies. The library must not close caller-provided streams.
5. **Dependencies:** Preserve the dependency-free runtime design. Do not add
   runtime dependencies unless the change has an explicit, compelling need.
6. **Binary format correctness:** Check versioning, byte and bit order, cell
   traversal order, padding, state ordering from `t=0` through `t=N`, and
   validation/error behavior against the format specification.
7. **Tests:** Request focused JUnit 5 coverage for boundary conditions and
   regressions, including empty and small boards, corners and edges, zero
   transitions, invalid inputs, stable patterns, oscillators, and byte-level
   encoding fixtures where applicable.
8. **Simplicity and maintainability:** Prefer small, clear, idiomatic Java 21
   changes with precise names and documentation for public behavior. Flag
   unnecessary abstraction, duplication, or changes that expand the public API
   without need.

Do not block a change solely for style when correctness, compatibility, and
maintainability are unaffected. Keep comments concrete: identify the behavior,
the consequence, and a practical correction.

End every review with an explicit summary in this form:

- **Problems found:** `None` or a concise list of the blocking and
  non-blocking problems identified.
- **Merge recommendation:** `Can merge` when no blocking issue remains, or
  `Do not merge yet` when a correctness, compatibility, security, or required
  test issue must be addressed.

Treat this as an advisory recommendation only; the repository maintainer makes
the final merge decision.
