# ProjectRed upstream API prototypes

Only generic ProjectRed changes belong here; painted tubes, the Sorting Machine,
and Manager are implemented separately in the ProjectRed Logistics addon.

Source baseline: `MrTJP/ProjectRed` 1.21.1 branch at
`e631f983c9b31c79f04deffa92ae5e68468326a0`.

## Patch series

Apply in order:

1. `patches/0001-generic-pneumatic-payload-metadata.patch`
2. `patches/0002-generic-pneumatic-route-policy.patch`
3. `patches/0003-generic-pneumatic-tube-metadata.patch`
4. `patches/0004-generic-pneumatic-entry-policy.patch`
5. `patches/0005-generic-pneumatic-tube-connection-policy.patch`
6. `patches/0006-generic-pneumatic-low-load-power.patch`

Suggested workflow:

```bash
git clone https://github.com/MrTJP/ProjectRed.git
cd ProjectRed
git checkout e631f983c9b31c79f04deffa92ae5e68468326a0
git switch -c feature/generic-pneumatic-api

git apply --check /path/to/ProjectRed-Logistics/upstream/patches/0001-generic-pneumatic-payload-metadata.patch
git apply /path/to/ProjectRed-Logistics/upstream/patches/0001-generic-pneumatic-payload-metadata.patch

git apply --check /path/to/ProjectRed-Logistics/upstream/patches/0002-generic-pneumatic-route-policy.patch
git apply /path/to/ProjectRed-Logistics/upstream/patches/0002-generic-pneumatic-route-policy.patch

git apply --check /path/to/ProjectRed-Logistics/upstream/patches/0003-generic-pneumatic-tube-metadata.patch
git apply /path/to/ProjectRed-Logistics/upstream/patches/0003-generic-pneumatic-tube-metadata.patch

git apply --check /path/to/ProjectRed-Logistics/upstream/patches/0004-generic-pneumatic-entry-policy.patch
git apply /path/to/ProjectRed-Logistics/upstream/patches/0004-generic-pneumatic-entry-policy.patch

git apply --check /path/to/ProjectRed-Logistics/upstream/patches/0005-generic-pneumatic-tube-connection-policy.patch
git apply /path/to/ProjectRed-Logistics/upstream/patches/0005-generic-pneumatic-tube-connection-policy.patch

git apply --check /path/to/ProjectRed-Logistics/upstream/patches/0006-generic-pneumatic-low-load-power.patch
git apply /path/to/ProjectRed-Logistics/upstream/patches/0006-generic-pneumatic-low-load-power.patch
```

## Patch 0001 — payload metadata

Adds:

- `PneumaticPayload`
- `PneumaticPayloadData`
- namespaced payload-only NBT
- save/load support
- client packet synchronization
- focused unit tests

ProjectRed already transfers the same logical payload across adjacent tubes and
serializes it for handoff packets, so no separate transport object is required.

## Patch 0002 — routing policy

Adds:

- `PneumaticRouteDecision`
- `PneumaticRouteContext`
- `PneumaticRouteNodeContext`
- `PneumaticRoutePolicy`
- policy registration through `IExpansionAPI`
- an internal policy registry
- payload-aware Dijkstra routing over cached graph links
- routing-significant-node retention

When no policy is registered, ProjectRed keeps its current route-table fast
path.

The routing-significant-node callback is required because ProjectRed normally
compresses redundant physical tube runs into larger graph links. A painted or
otherwise restricted tube must remain visible to payload-aware routing.

## Patch 0004 — first-tube policy admission

Ensures registered pneumatic route policies are also evaluated when a payload
is first inserted from a machine/device into a pneumatic tube. Without this
boundary check, restrictions attached to the first tube can be bypassed before
normal graph routing begins.

The additional route cost is irrelevant at this fixed admission boundary; the
policy's allow/block decision is enforced before the tube accepts the payload.

## Patch 0003 — tube metadata

Adds:

- `PneumaticTube`
- `PneumaticTubeData`
- public tube lookup through `IExpansionAPI`
- namespaced data persisted on the pneumatic multipart
- server-to-client metadata synchronization
- automatic graph invalidation when tube metadata changes

This is the generic mechanism ProjectRed Logistics will use for paint. ProjectRed
itself remains unaware of colour semantics.

## Intended addon use

ProjectRed Logistics will store:

- payload colour under `projectred_logistics:route_colour`
- tube paint under `projectred_logistics:route_colour`

The addon policy will mark painted tubes as routing-significant and reject
incompatible payload/tube colour combinations.

See `ROUTING_TEST_PLAN.md` for the next validation cases.

Status: prototype. Full dependency-resolved ProjectRed compilation and in-game
validation are still required. No upstream PR has been opened.


## Patch 0005 — physical tube connection policy

Adds a generic, payload-independent hook that lets addons veto physical
tube-to-tube connections while ProjectRed rebuilds connection masks.

This is intentionally separate from payload route policies: a connection policy
changes the actual topology and therefore also affects tube geometry. Tube
metadata changes force local and neighbouring tube masks to be recalculated.

## Patch 0006 — optional low-load power conduction

Adds a generic opt-in hook that lets addons mark pneumatic tubes as low-load
power conductors without teaching ProjectRed about addon-specific materials.

ProjectRed owns the electrical simulation and exposes the tube as a normal
low-load power line only while at least one registered predicate enables it.
ProjectRed Logistics uses this for Electrotine Alloy-lined pneumatic tubes.

Enabled tubes can connect to:

- other enabled pneumatic tubes
- Low Load Power Line and Framed Low Load Power Line
- ProjectRed low-load machines, including the powered Logistics machines

Plain pneumatic tubes remain electrically inert.
