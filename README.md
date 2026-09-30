# ProjectRed Logistics

ProjectRed Logistics is a NeoForge addon for ProjectRed focused on advanced
pneumatic-tube logistics inspired by the capabilities of RedPower 2.

Initial goals:

- Paintable pneumatic tubes with route-aware colour semantics.
- A Sorting Machine with RP2-style colour assignment and sorting modes.
- A Manager-style distributed inventory control system.
- Clean integration through a generic ProjectRed pneumatic routing API rather
  than invasive patches or mixins.

## Target

- Minecraft 1.21.1
- NeoForge 21.1.72
- Java 21
- ProjectRed 4.23.0

## Architecture

ProjectRed itself should only gain generic extension points. ProjectRed
Logistics owns every feature-specific behaviour such as colour routing,
painted-tube state, sorting logic, and Manager logic.

See `docs/API_DESIGN.md` for the current upstream API proposal and
`docs/ROADMAP.md` for development milestones.

## Status

Early development. The first milestone is validating and upstreaming a generic
pneumatic payload/routing API before implementing the Sorting Machine or
Manager.

## License

MIT.
