# Generic ProjectRed Pneumatic API Proposal

## Purpose

ProjectRed's pneumatic tube implementation currently owns payload transport,
route discovery, graph caching, endpoint selection, and tube handoff. Addons
should be able to influence routing without replacing those systems.

The API must remain generic. It must not contain RP2-specific concepts such as
"red route", "sorting-machine column", or "Manager priority".

## Design principles

1. Existing ProjectRed behaviour is unchanged when no policy is registered.
2. Addons must not depend on internal graph classes.
3. Payload metadata is namespaced and survives save/load and tube handoff.
4. Route policies may reject a route element or add routing cost.
5. Policies must be consulted early enough that a shorter invalid route does
   not suppress a longer valid route.
6. The API should permit ProjectRed to change its internal graph
   implementation later.

## Proposed public API

Suggested package:

`mrtjp.projectred.api.pneumatics`

### PneumaticPayload

```java
public interface PneumaticPayload {
    ItemStack getItemStack();

    boolean hasData(ResourceLocation key);
    @Nullable CompoundTag getData(ResourceLocation key);
    void setData(ResourceLocation key, CompoundTag value);
    void removeData(ResourceLocation key);
}
```

ProjectRed's internal `PneumaticTubePayload` implements this interface. The
first prototype patch introduces `PneumaticPayloadData` as the reusable
storage helper. Its compound-valued keys are `ResourceLocation` namespaced
identifiers, and all set/get/save/load methods defensively copy mutable NBT.

The patch stores the entire metadata compound under
`projectred_payload_data` on the payload itself (not on the ItemStack).
Missing data in an existing/older saved payload becomes an empty container.
For client updates it uses CodeChickenLib's `writeCompoundNBT` and
`readCompoundNBT` helpers in the payload's existing description codec.

Patch location:
`upstream/patches/0001-generic-pneumatic-payload-metadata.patch`.

ProjectRed's published 4.23.0 artifacts do **not** contain this proposed API;
the standalone addon does not yet call it.

Metadata must survive:

- NBT persistence
- server/client payload descriptions
- tube-to-tube handoff

### PneumaticRouteDecision

```java
public record PneumaticRouteDecision(boolean allowed, int additionalCost) {
    public static final PneumaticRouteDecision PASS =
        new PneumaticRouteDecision(true, 0);

    public static final PneumaticRouteDecision BLOCK =
        new PneumaticRouteDecision(false, 0);
}
```

`additionalCost` should be non-negative in the first API revision.

### PneumaticRouteContext

The API context should expose stable Minecraft concepts rather than internal
`GraphNode` / `GraphRoute` objects.

Candidate information:

- `Level`
- current/next `BlockPos` when available
- movement `Direction`
- base traversal cost
- route phase
- whether the transition is entering a tube, traversing a tube, or exiting to
  an endpoint

### PneumaticRoutePolicy

```java
public interface PneumaticRoutePolicy {
    PneumaticRouteDecision evaluate(
        PneumaticPayload payload,
        PneumaticRouteContext context
    );

    default boolean requiresRoutingNode(PneumaticRouteNodeContext context) {
        return false;
    }
}
```

The second callback is important because ProjectRed compresses physically
redundant tube runs into graph links. A tube carrying routing-significant state
(such as paint, access control, or a one-way rule) must remain represented as a
graph node or the payload-aware search would never see it.

Registration should follow ProjectRed's existing Expansion API pattern:

```java
ProjectRedAPI.expansionAPI.registerPneumaticRoutePolicy(policy);
```

## Important graph constraint

ProjectRed currently caches payload-independent routes and removes alternative
routes to a destination when an equal or shorter route has already been found.

Therefore filtering only the completed route table is insufficient.

Example:

- a shorter path contains a BLUE-only segment
- a longer path contains a RED-compatible segment
- the payload is RED

If the shorter path is cached first and the longer route discarded, a late
colour filter cannot recover the valid route.

## Recommended prototype strategy

Do not make the entire physical graph payload-specific.

Patch 0002 takes this approach:

1. Preserve ProjectRed's existing topology/link cache.
2. Keep the current route-table fast path when no pneumatic policies exist.
3. When policies are registered, run a payload-aware Dijkstra search over the
   already-cached graph links.
4. Allow policies to reject transitions or add non-negative traversal cost.
5. Allow policies to mark physical tube locations as routing-significant so
   those locations are retained as graph nodes instead of disappearing inside
   compressed links.
6. Do not expose internal GraphNode, GraphLink, or GraphRoute classes through
   the public API.

This solves the "short blocked path hides longer valid path" problem without
rediscovering the entire physical tube network for every payload.

## ProjectRed Logistics use

The addon initially registers `LogisticsColourRoutePolicy`.

Payload metadata key:

`projectred_logistics:route_colour`

Semantics:

- unpainted tubes are neutral
- a coloured payload may traverse an unpainted tube
- a coloured payload may traverse a tube painted the same colour
- a coloured payload may not traverse a tube painted another colour

The treatment of uncoloured payloads entering painted tubes should be verified
against original RP2 behaviour before implementation.

## Out of scope for upstream ProjectRed

These remain entirely in ProjectRed Logistics:

- dye interaction
- tube paint rendering
- Sorting Machine GUI/logic
- Sorting Machine sorting modes
- Manager GUI/logic
- stock templates
- Manager-to-Manager requests
- RP2-compatible colour semantics


## Prototype patch 0002

Location:
`upstream/patches/0002-generic-pneumatic-route-policy.patch`

The prototype adds:

- `PneumaticRouteDecision`
- `PneumaticRouteContext`
- `PneumaticRouteNodeContext`
- `PneumaticRoutePolicy`
- route-policy registration through `IExpansionAPI`
- an internal policy registry
- a policy-aware Dijkstra search over cached graph links
- routing-significant-node retention
- unit coverage for route-decision cost validation

The next validation target is an integration-style topology test where a
shorter route is blocked by a policy and a longer route to the same reachable
inventory remains selectable.
