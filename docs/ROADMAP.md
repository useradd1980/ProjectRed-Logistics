# Development Roadmap

## Milestone 0 — Repository/bootstrap

- [x] Choose NeoForge 1.21.1.
- [x] Pin Java 21.
- [x] Pin ProjectRed 4.23.0.
- [x] Establish MIT licensing.
- [x] Separate addon design from ProjectRed upstream API work.

## Milestone 1 — Generic pneumatic API prototype

- [x] Prototype public `PneumaticPayload` abstraction in upstream patch 0001.
- [x] Prototype namespaced payload metadata with defensive copies.
- [x] Implement NBT persistence and packet description serialization in patch 0001.
- [x] Add four focused JUnit tests (execution pending in CI).
- [ ] Confirm full dependency-resolved ProjectRed test/build passes.
- [x] Prototype `PneumaticRoutePolicy` registration in upstream patch 0002.
- [x] Prototype stable `PneumaticRouteContext` and routing-node context in patch 0002.
- [x] Prototype `PneumaticRouteDecision` with allow/block and non-negative added cost.
- [x] Prototype payload-aware Dijkstra routing over ProjectRed's cached graph links.
- [x] Add routing-significant-node hook so restricted tubes are not hidden inside compressed links.
- [ ] Add an integration-style graph test proving a longer allowed path beats a shorter blocked path.
- [ ] Confirm behaviour is identical when no policies are registered.

## Milestone 2 — ProjectRed Logistics integration

- [x] Register a colour-routing policy.
- [x] Store tube paint as namespaced metadata through the generic tube API.
- [x] Add dye/repaint interaction plus temporary sneak-empty-hand clearing.
- [x] Add first-pass client rendering for painted tubes.
- [ ] Assign and preserve payload colours.
- [ ] Validate routing across junctions and loops.

## Milestone 3 — Filter

- [x] Register Filter block/entity and first-pass model.
- [x] Add nine real, persistent filter slots.
- [x] Implement empty-filter whole-stack extraction.
- [x] Implement quantity-sensitive configured extraction.
- [x] Assign optional routing colour to outgoing payloads.
- [x] Add functional 3x3 Filter inventory GUI.
- [x] Match RP2 Filter slot layout and add 17-state colour selector.
- [ ] Validate filtering of incoming tube payloads.
- [ ] Add automated tests.

## Milestone 4 — Sorting Machine

- [ ] Block/entity/model.
- [ ] RP2-style GUI.
- [ ] Filter columns.
- [ ] Colour assignment.
- [ ] Sorting modes.
- [ ] Default route behaviour.
- [ ] Stack handling.
- [ ] Automated tests.

## Milestone 5 — Manager

- [ ] Inventory template.
- [ ] Stock-level evaluation.
- [ ] Requests.
- [ ] Priorities.
- [ ] Colour-aware transport.
- [ ] Manager-to-Manager behaviour.
- [ ] Large-network tests.
