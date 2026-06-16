# Manual QA Report

Target: Minecraft 1.21.1, NeoForge 21.1.x, Java 21.

## Verified By Build

- Compile client tooltip component APIs.
- Compile common config and extraction code without an Immersive Engineering compile dependency.
- Keep client rendering classes referenced only from the client-only `@Mod(..., dist = Dist.CLIENT)` entrypoint.
- `./gradlew.bat build --no-daemon` completed successfully on Java 21.
- Dedicated server smoke launch reached `Done (...)! For help, type "help"` with the mod list limited to `wimc`, `minecraft`, and `neoforge`.

## In-Game Manual Scenarios To Run With Immersive Engineering Installed

- Empty crate: hover an IE crate with no `minecraft:container` contents. With `showEmptySlots=true`, expect a 9-wide empty grid. With it disabled, expect no grid.
- Mixed items: seal a crate containing varied stacks. Hover the resulting item and expect icons plus vanilla stack decorations.
- Reinforced crate: repeat mixed-item test with `immersiveengineering:reinforced_crate`.
- Malformed or missing inventory data: use a crate stack without `minecraft:container`, or with unrelated custom data. Expect no crash and either an empty preview or no preview depending on config.
- Sealed crate: IE sealed crates should preview because IE stores sealed drops in `DataComponents.CONTAINER`.
- Loot-table crate: a supported crate item with `DataComponents.CONTAINER_LOOT` but no generated `DataComponents.CONTAINER` should show the unresolved-loot message instead of attempting to generate or request server data.
- GUI scale: test scale 1, 2, 3, and Auto. The vanilla tooltip positioner should clamp the custom component to screen bounds.
- Dedicated server launch: run the NeoForge server configuration. The common mod should load config only, and client GUI classes should not be loaded.
