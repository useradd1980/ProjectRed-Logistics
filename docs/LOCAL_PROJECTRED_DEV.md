# Local patched ProjectRed development

Until the generic pneumatic API is available in a public ProjectRed release,
ProjectRed Logistics compiles and runs against a patched ProjectRed build
published to Maven Local.

## 1. Build and publish patched ProjectRed

From the sibling ProjectRed checkout after applying/committing patches 0001–0003:

```bash
cd ~/Projects/ProjectRed
AUTO_GENERATED_VERSION=5.0-logistics-dev ./gradlew clean publishToMavenLocal
```

This publishes the classifier jars under the development version:

```text
mrtjp:ProjectRed:1.21.1-5.0-logistics-dev
```

## 2. Build ProjectRed Logistics

```bash
cd ~/Projects/ProjectRed-Logistics
git switch feat/pneumatic-payload-metadata
git pull
./gradlew compileJava
```

The repository currently sets `use_local_projectred=true` in
`gradle.properties`.

To return to the public release dependency later, set:

```properties
use_local_projectred=false
```

The public dependency path should only be used after an upstream ProjectRed
release contains the pneumatic API.
