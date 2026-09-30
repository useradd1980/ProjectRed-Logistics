# ProjectRed upstream API prototypes

Only generic ProjectRed changes belong here; painted tubes, the Sorting Machine,
and Manager are implemented separately in the ProjectRed Logistics addon.

## First patch: payload-only namespaced metadata

Source: [MrTJP/ProjectRed e631f983c9b31c79f04deffa92ae5e68468326a0](https://github.com/MrTJP/ProjectRed/commit/e631f983c9b31c79f04deffa92ae5e68468326a0)
on the 1.21.1 branch.

Apply in a separate ProjectRed checkout:

```bash
git clone https://github.com/MrTJP/ProjectRed.git
cd ProjectRed
git checkout e631f983c9b31c79f04deffa92ae5e68468326a0
git switch -c feature/generic-pneumatic-api
git apply --check /path/to/ProjectRed-Logistics/upstream/patches/0001-generic-pneumatic-payload-metadata.patch
git apply /path/to/ProjectRed-Logistics/upstream/patches/0001-generic-pneumatic-payload-metadata.patch
./gradlew :api:test :expansion:compileJava
```

The patch adds `PneumaticPayload`, `PneumaticPayloadData`, NBT persistence,
client packet synchronization, and focused API unit tests.

ProjectRed already transfers the same payload object between adjacent tubes.
Its handoff packet calls `writeDesc` and `readDesc`, so this patch needs no
changes to `PneumaticTransport` or the tube-routing graph.

Status: prototype. A full dependency-resolved Gradle build and in-game tube
handoff tests are still required. No upstream PR has been opened.
