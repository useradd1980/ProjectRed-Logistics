# Development Roadmap

ProjectRed Logistics is an actively developed NeoForge addon that restores and
extends RedPower 2-style pneumatic logistics on top of modern ProjectRed.

This roadmap reflects the current implementation rather than the original
prototype plan. Completed items have been verified in code and, where noted,
through in-game testing.

## Milestone 0 — Repository/bootstrap

- [x] Choose Minecraft 1.21.1 / NeoForge 21.1.x.
- [x] Pin Java 21.
- [x] Establish MIT licensing.
- [x] Separate addon behaviour from generic ProjectRed API work.
- [x] Publish a patched ProjectRed development build to Maven Local.
- [x] Keep ProjectRed-specific compatibility patches under `upstream/patches/`.
- [ ] Add CI/status checks for compile and tests on pull requests.

## Milestone 1 — Generic ProjectRed pneumatic extension API

- [x] Add public `PneumaticPayload` abstraction.
- [x] Add namespaced payload metadata with defensive copies.
- [x] Persist payload metadata through NBT and client packet descriptions.
- [x] Add generic pneumatic route-policy registration.
- [x] Add stable route and routing-node contexts.
- [x] Add allow/block decisions and non-negative added route cost.
- [x] Add payload-aware Dijkstra routing over ProjectRed's cached graph links.
- [x] Add routing-significant-node retention for metadata-sensitive tubes.
- [x] Add namespaced pneumatic tube metadata.
- [x] Add first-tube/entry policy admission.
- [x] Add generic physical tube-connection policy hooks.
- [x] Add generic opt-in low-load power support for pneumatic tubes.
- [x] Apply the full patch series to the pinned ProjectRed 1.21.1 source.
- [x] Build and publish the patched ProjectRed development artifacts locally.
- [x] Add an integration graph test proving a longer allowed route beats a
      shorter blocked route.
- [x] Add explicit regression coverage proving vanilla ProjectRed behaviour is
      unchanged when no extension policies are registered.
- [ ] Convert the patch series into clean upstream ProjectRed contribution
      branches/commits.
- [ ] Open upstream pull requests for the generic pneumatic extension points.

## Milestone 2 — Coloured pneumatic routing

- [x] Register ProjectRed Logistics colour-routing policy.
- [x] Store tube routing colour as namespaced tube metadata.
- [x] Store payload routing colour as namespaced payload metadata.
- [x] Add dye/repaint interaction for pneumatic tubes.
- [x] Render routing-colour strips inside pneumatic tubes.
- [x] Preserve payload colour across tube transport.
- [x] Reject incompatible payload/tube colour combinations.
- [x] Disconnect differently painted adjacent tubes at the physical topology
      level.
- [x] Allow routing colour and ProjectRed red-alloy conductors to coexist.
- [x] Allow routing colour and Electrotine power conductors to coexist.
- [ ] Complete dedicated junction/loop regression tests for coloured routing.

## Milestone 3 — Filter

- [x] Register Filter block/entity and model.
- [x] Add nine persistent filter slots.
- [x] Implement empty-filter whole-stack extraction.
- [x] Implement quantity-sensitive configured extraction.
- [x] Assign optional routing colour to outgoing payloads.
- [x] Filter incoming payloads by configured entries.
- [x] Recolour accepted incoming payloads.
- [x] Add RP2-style 3x3 GUI layout.
- [x] Use the original RP2 routing-colour button artwork.
- [x] Add internal Automatic mode so an external redstone pulse is optional.
- [x] Add RP2-derived crafting recipe.
- [x] Add automated tests for matching, recolouring, and colour compatibility.
- [ ] Add broader in-game regression coverage for mixed inventories and
      backstuff behaviour.

## Milestone 4 — Sorting Machine

- [x] Register Sorting Machine block/entity.
- [x] Add RP2-inspired machine artwork and orientation.
- [x] Add functional RP2-positioned 5x8 GUI.
- [x] Add eight filter columns with independent routing colours.
- [x] Use original RP2 routing-colour button artwork.
- [x] Implement inline payload sorting and colour assignment.
- [x] Implement all seven RedPower 2 pr6 sorting modes.
- [x] Implement default-route behaviour for modes 4 and 6.
- [x] Implement configured-batch and whole-stack handling.
- [x] Add Single Step, Automatic, and Single Sweep operation.
- [x] Integrate ProjectRed low-load power.
- [x] Match RP2-style >60 V operating threshold.
- [x] Consume 25 power units per processed item.
- [x] Add Auto Crafter-style recessed voltage/power-flow meters.
- [x] Add RP2-derived crafting recipe.
- [x] Add automated tests for sorting-mode families, default routes, and
      extraction quantities.
- [ ] Add larger-network stress/regression tests.

## Milestone 5 — Manager

- [x] Register Manager block/entity and 24-slot stock template.
- [x] Add Stock and Excess modes.
- [x] Implement stock-level evaluation.
- [x] Implement automatic excess export.
- [x] Implement Manager-to-Manager stock requests.
- [x] Implement priority-based request/supply behaviour.
- [x] Implement colour-aware Manager transport.
- [x] Prevent duplicate outstanding requests from over-pulling stock.
- [x] Tag requested payloads with source and destination metadata.
- [x] Return unused/backstuffed requested items to the supplying Manager.
- [x] Guard Manager route scans against stale/removed tube graph entries.
- [x] Emit RP2-style redstone output when Stock mode is satisfied.
- [x] Integrate ProjectRed low-load power.
- [x] Add RP2 Manager block textures and visual states.
- [x] Add RP2-inspired Manager GUI.
- [x] Use original RP2 Stock/Excess, priority, and routing-colour button
      artwork.
- [ ] Add automated Manager unit/integration tests.
- [ ] Add large-network and competing-priority stress tests.
- [ ] Add the original RP2 Manager crafting recipe after the Regulator exists.

## Milestone 6 — Electrotine-powered pneumatic tubes

- [x] Install an Electrotine Alloy conductor by right-clicking a pneumatic tube.
- [x] Persist powered-tube state as namespaced tube metadata.
- [x] Carry real ProjectRed low-load power through upgraded tubes.
- [x] Connect upgraded tubes to other upgraded pneumatic tubes.
- [x] Connect upgraded tubes to Low Load Power Line.
- [x] Connect upgraded tubes to Framed Low Load Power Line.
- [x] Connect upgraded tubes to ProjectRed low-load machines.
- [x] Connect upgraded tubes to powered Logistics machines.
- [x] Return the Electrotine Alloy ingot when the conductor is removed.
- [x] Remove placement/removal chat messages.
- [x] Render an internal conductor without colliding with routing/red-alloy
      strips.
- [x] Use the same blue/yellow stripe colours as ProjectRed's Low Load Power
      Line.
- [x] Keep conductor thickness uniform at framed-power-line connections.
- [x] Map stripe UVs continuously through straight runs, ends, bends, and
      junctions.
- [ ] Add automated/regression tests for low-load power topology changes.

## Milestone 7 — Restriction Tube

- [x] Confirm original RP2 semantics: a Restriction Tube contributes a routing
      weight of 1,000,000 rather than blocking transport.
- [x] Add generic weighted-routing coverage proving a longer unrestricted route
      beats a shorter heavily weighted route.
- [x] Add generic weighted-routing coverage proving a heavily weighted route
      remains usable when it is the only route.
- [x] Register a Logistics-owned Restriction Tube multipart and placement item.
- [x] Mark Restriction Tubes as routing-significant graph nodes.
- [x] Apply the RP2-style 1,000,000 route cost through the generic route-policy
      API.
- [x] Add the original RP2-style shapeless recipe: pneumatic tube + iron ingot.
- [x] Give the Restriction Tube a modern dark graphite/blackened-steel frame
      derived from ProjectRed's pneumatic-tube texture.
- [x] Validate placement and route preference in-game.
- [x] Validate coloured routing, red-alloy wiring, and Electrotine power
      coexistence on Restriction Tubes.

## Milestone 8 — Recipes and progression

- [x] Add RP2-derived Filter recipe.
- [x] Add RP2-derived Sorting Machine recipe.
- [x] Verify standard shaped recipes are visible through recipe-viewer mods.
- [ ] Implement the Regulator.
- [ ] Add the original RP2 Manager recipe once its Regulator dependency exists.
- [ ] Review the remaining RP2 logistics-machine progression and recipes.

## Milestone 9 — Upstreaming, polish, and release readiness

- [x] Refresh the project README to describe the current feature set.
- [x] Document the full ProjectRed patch workflow.
- [x] Keep addon-specific semantics out of the generic ProjectRed patches.
- [ ] Refresh `docs/API_DESIGN.md` to include patches 0003–0006 and the final
      implemented APIs.
- [ ] Add GitHub Actions compile/test checks.
- [ ] Require successful CI checks before merging to the protected main branch.
- [ ] Split and clean the ProjectRed patch series into reviewable upstream PRs.
- [ ] Resolve upstream feedback or maintain a documented compatibility patch
      set if some hooks are not accepted.
- [ ] Complete gameplay regression testing across Filters, Sorting Machines,
      Managers, coloured routing, red-alloy tubes, and Electrotine power.
- [ ] Define the first public release version and installation instructions.
