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
- [ ] Add `PneumaticRoutePolicy` registration.
- [ ] Add `PneumaticRouteContext`.
- [ ] Add `PneumaticRouteDecision`.
- [ ] Integrate policy evaluation without making the whole topology graph
      payload-specific.
- [ ] Add a test proving a longer allowed path beats a shorter blocked path.
- [ ] Confirm behaviour is identical when no policies are registered.

## Milestone 2 — ProjectRed Logistics integration

- [ ] Register a colour-routing policy.
- [ ] Add paint state through the agreed generic extension.
- [ ] Dye / repaint / clear-paint interactions.
- [ ] Render painted tubes.
- [ ] Assign and preserve payload colours.
- [ ] Validate routing across junctions and loops.

## Milestone 3 — Sorting Machine

- [ ] Block/entity/model.
- [ ] RP2-style GUI.
- [ ] Filter columns.
- [ ] Colour assignment.
- [ ] Sorting modes.
- [ ] Default route behaviour.
- [ ] Stack handling.
- [ ] Automated tests.

## Milestone 4 — Manager

- [ ] Inventory template.
- [ ] Stock-level evaluation.
- [ ] Requests.
- [ ] Priorities.
- [ ] Colour-aware transport.
- [ ] Manager-to-Manager behaviour.
- [ ] Large-network tests.
