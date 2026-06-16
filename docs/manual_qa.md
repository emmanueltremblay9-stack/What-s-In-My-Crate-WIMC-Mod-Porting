# Manual QA Report

Target: Minecraft 1.21.1, NeoForge 21.1.x, Java 21.

## Verified By Build

- Compile client tooltip component APIs.
- Compile common config and extraction code without an Immersive Engineering compile dependency.
- Keep client rendering classes referenced only from the client-only `@Mod(..., dist = Dist.CLIENT)` entrypoint.
- `./gradlew.bat build --no-daemon` completed successfully on Java 21.
- Dedicated server smoke launch reached `Done (...)! For help, type "help"` with the mod list limited to `wimc`, `minecraft`, and `neoforge`.

## Bugfix Pass - June 16, 2026

- Fixed `requireShiftForPreview=true` so the Shift hint is added only when the hovered stack can actually produce a crate preview.
- Fixed `showEmptySlots=false` so trimmed empty tail slots are not reported as hidden/truncated slots.
- Added resource pack `supported_formats` metadata and repository line-ending normalization.
- Re-ran `./gradlew.bat build --no-daemon`; build completed successfully.
- Re-ran a dedicated server smoke launch; server reached `Done (...)! For help, type "help"`.

## Bugfix Pass 2 - June 16, 2026

- Switched `DataComponents.CONTAINER` extraction to `ItemContainerContents.copyInto(...)`, matching the vanilla/NeoForge component materialization path.
- Added a lightweight `qaPreviewModel` verification task wired into `check` to prevent false hidden-slot reports for trimmed empty previews.
- Re-ran `./gradlew.bat build --no-daemon`; build completed successfully with the QA task.

## Bugfix Pass 3 - June 16, 2026

- Fixed loot-table precedence so empty `DataComponents.CONTAINER` data does not hide unresolved `DataComponents.CONTAINER_LOOT`.
- Added fallback detection for block-entity loot table keys without item data.
- Bounded item capability fallback scans to 256 slots to avoid pathological hover-time allocations from malformed handlers.
- Wrapped per-stack preview rendering so a broken item icon/decorator skips that slot instead of crashing the tooltip.
- Re-ran `./gradlew.bat build --no-daemon`; build completed successfully with the QA task.

## In-Game Manual Scenarios To Run With Immersive Engineering Installed

- Empty crate: hover an IE crate with no `minecraft:container` contents. With `showEmptySlots=true`, expect a 9-wide empty grid. With it disabled, expect no grid.
- Mixed items: seal a crate containing varied stacks. Hover the resulting item and expect icons plus vanilla stack decorations.
- Reinforced crate: repeat mixed-item test with `immersiveengineering:reinforced_crate`.
- Malformed or missing inventory data: use a crate stack without `minecraft:container`, or with unrelated custom data. Expect no crash and either an empty preview or no preview depending on config.
- Sealed crate: IE sealed crates should preview because IE stores sealed drops in `DataComponents.CONTAINER`.
- Loot-table crate: a supported crate item with `DataComponents.CONTAINER_LOOT` but no generated `DataComponents.CONTAINER` should show the unresolved-loot message instead of attempting to generate or request server data.
- GUI scale: test scale 1, 2, 3, and Auto. The vanilla tooltip positioner should clamp the custom component to screen bounds.
- Dedicated server launch: run the NeoForge server configuration. The common mod should load config only, and client GUI classes should not be loaded.
