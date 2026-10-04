# ProjectRed Logistics

ProjectRed Logistics is a NeoForge addon for ProjectRed that brings
RedPower 2-inspired pneumatic logistics back to modern Minecraft.

The project extends ProjectRed's pneumatic tube system with routing,
inventory-management machines, tube metadata, and optional low-load power
transport while keeping ProjectRed-specific API changes generic enough for
other addons to use.

## Current features

### Pneumatic tubes

- Namespaced metadata on pneumatic payloads and tubes.
- Route-aware coloured tube networks.
- Dyeable pneumatic tubes with physical colour-routing separation.
- Coloured payload routing.
- Optional Electrotine Alloy conductor installed by right-clicking a tube.
- Electrotine-lined tubes carry ProjectRed low-load power.
- Powered tubes connect to Low Load Power Line and Framed Low Load Power Line.
- RP2-inspired internal conductor rendering for routing colour and power.

### Restriction Tube

- RP2-compatible weighted routing: Restriction Tubes add a route cost of 1,000,000
  instead of blocking transport.
- Items prefer ordinary pneumatic routes whenever one is available, but can
  still use Restriction Tubes as a fallback.
- Uses the normal ProjectRed pneumatic-tube geometry with a dark
  graphite/blackened-steel outer frame so it fits the modern ProjectRed visual style.
- Supports the same routing-colour, red-alloy, and Electrotine tube upgrades
  as ordinary pneumatic tubes.

### Filter

- RP2-style 3x3 filter inventory.
- Quantity-sensitive extraction.
- Whole-stack extraction when unconfigured.
- Optional routing colour assignment.
- Incoming payload filtering and recolouring.
- Internal Automatic mode so the Filter can pulse without external redstone.

### Sorting Machine

- RP2-inspired 5x8 sorting grid.
- Eight independently coloured filter columns.
- Seven RedPower 2 sorting modes.
- Default-route behaviour.
- Configured-batch and whole-stack handling.
- Single Step, Automatic, and Single Sweep operation.
- ProjectRed low-load power support with the RP2-style operating threshold.
- RP2-inspired GUI, controls, and machine artwork.

### Manager

- 24-slot stock template.
- Stock and Excess modes.
- Priority-based Manager-to-Manager requests.
- Colour-aware transport.
- Automatic restocking and excess export.
- Requested-payload return handling for unused backstuff.
- Stock-satisfied redstone output.
- ProjectRed low-load power support.
- RP2-inspired GUI, controls, and machine artwork.

## Target

- Minecraft 1.21.1
- NeoForge 21.1.72
- Java 21
- ProjectRed 1.21.1 development branch

The current development setup uses a locally patched ProjectRed build published
as:

```
1.21.1-5.0-logistics-dev
```

The public ProjectRed 4.23.0 artifacts do not yet contain the pneumatic
extension points required by ProjectRed Logistics.

## Architecture

ProjectRed itself should only gain generic extension points. ProjectRed
Logistics owns the feature-specific behaviour.

Examples:

- ProjectRed exposes namespaced pneumatic payload/tube metadata.
- ProjectRed exposes generic routing, entry, connection, and power-extension
  hooks.
- ProjectRed Logistics decides that dye represents routing colour.
- ProjectRed Logistics implements Filter, Sorting Machine, and Manager logic.
- ProjectRed Logistics decides that Electrotine Alloy enables low-load power
  transport through a pneumatic tube.

This keeps the upstream ProjectRed changes useful to other addons instead of
hard-coding ProjectRed Logistics concepts into ProjectRed itself.

See:

- [`docs/API_DESIGN.md`](docs/API_DESIGN.md) for the generic pneumatic API
  design.
- [`docs/ROADMAP.md`](docs/ROADMAP.md) for development milestones.
- [`upstream/README.md`](upstream/README.md) for the ProjectRed patch workflow.

## Development status

ProjectRed Logistics is under active development.

The Filter, Sorting Machine, Manager, Restriction Tube, coloured routing, and
Electrotine-powered tube systems are functional and being tested and refined. Current work is
focused on RP2 behaviour parity, visual polish, integration testing, recipes,
and preparing the generic ProjectRed pneumatic changes for possible upstream
submission.

## Upstream ProjectRed patches

The repository currently carries a series of development patches against
ProjectRed's 1.21.1 source:

1. Generic pneumatic payload metadata.
2. Generic pneumatic route policies.
3. Generic pneumatic tube metadata.
4. Generic pneumatic entry policies.
5. Generic pneumatic tube connection policies.
6. Generic optional low-load power support for pneumatic tubes.

These patches are intentionally generic. The RP2-specific logistics semantics
remain entirely in ProjectRed Logistics.

## Building

With the patched ProjectRed development artifacts already published to Maven
Local:

```bash
./gradlew compileJava
./gradlew test --console=plain
./gradlew runClient
```

See [`upstream/README.md`](upstream/README.md) for instructions on applying
and publishing the required ProjectRed development patches.

## License

MIT.
