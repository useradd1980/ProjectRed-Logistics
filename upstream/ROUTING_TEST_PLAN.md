# Pneumatic routing integration test plan

## Primary regression case

Build a topology with two paths from one source junction to the same reachable
inventory.

- Path A is shorter.
- Path A contains a routing-significant node rejected by the registered policy.
- Path B is longer.
- Path B is fully accepted.

Expected result: the payload chooses Path B.

This proves payload-aware routing is not constrained by the payload-agnostic
route table's shorter-path pruning.

## Baseline case

With no policies registered, the same network should use ProjectRed's existing
route-table path and preserve current behaviour.

## Added-cost case

Two allowed paths reach valid inventories.

- Path A base cost: 4
- Path B base cost: 6
- Policy adds cost 5 to Path A

Expected result: Path B wins with effective cost 6 versus 9.

## Routing-significant node case

Start with a straight run of redundant tubes.

1. Confirm the middle tube is normally compressible.
2. Add namespaced tube metadata that causes the policy to return
   `requiresRoutingNode = true` for that position.
3. Metadata mutation should invalidate graph links.
4. Confirm the middle tube becomes represented as an active graph node.
5. Remove the metadata and confirm the topology can collapse again.

## Metadata continuity case

A coloured payload crosses several tube handoffs and a chunk save/load.

Expected result: its namespaced metadata remains unchanged.

## Client synchronization case

Change namespaced tube metadata on the server.

Expected result: the client receives updated metadata and rerenders the
multipart without requiring a chunk reload.


## First-tube admission

- A coloured payload inserted directly from a pneumatic device into an
  unpainted tube is accepted.
- A coloured payload inserted directly into a matching painted tube is
  accepted.
- A coloured payload inserted directly into a differently painted tube is
  rejected before entering the tube.
- An uncoloured payload remains accepted by painted tubes.
