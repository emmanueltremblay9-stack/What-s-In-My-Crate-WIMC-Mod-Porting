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

## Bugfix Pass 4 - June 16, 2026

- Fixed block-entity loot-table detection when a supported crate has a loot key plus an empty or malformed `Items` tag.
- Fixed false-empty precedence so a non-empty block-entity or item-capability inventory can override an empty container data component.
- Re-ran `./gradlew.bat build --no-daemon`; build completed successfully with the QA task.
- Re-ran a dedicated server smoke launch; server reached `Done (...)! For help, type "help"`.

## Engineer's Manual Layout Pass - June 17, 2026

- Added a small Immersive Engineering manual autoload hook under `assets/immersiveengineering/manual/autoload.json`.
- Added a nested `Factory Inventory Inspector` section under IE's existing Storage & Transport category.
- Added three short entries: overview, supported crates, and preview settings.
- Added anchored crate icons through `item_display` special elements.
- Added explicit IE recipe references for `immersiveengineering:crafting/crate` and `immersiveengineering:crafting/reinforced_crate`.
- Added the required category localization key: `manual.wimc.factory_inventory_inspector`.
- Kept pages short with explicit `<np>` page breaks to avoid fixed-height manual overflow.
- Verified manual JSON parses, required text files exist, category localization exists, and referenced IE 1.21.1 crate recipes exist in the inspected IE source.
- Re-ran `./gradlew.bat build --no-daemon`; build completed successfully with the QA task.
- Re-ran a dedicated server smoke launch; server reached `Done (...)! For help, type "help"`.

## Bugfix Pass 5 - June 17, 2026

- Removed Markdown-style backticks from Engineer's Manual player-facing text because IE manual pages render them literally.
- Added `qaManualResources` to validate WIMC manual autoload hierarchy, entry text shape, anchors, links, recipe references, item-display IDs, and category localization during Gradle `check`.
- Re-ran `./gradlew.bat build --no-daemon`; build completed successfully with `qaPreviewModel` and `qaManualResources`.
- Re-ran a dedicated server smoke launch; server reached `Done (...)! For help, type "help"`.

## In-Game Tooltip Scenarios To Run With Immersive Engineering Installed

- Empty crate: hover an IE crate with no `minecraft:container` contents. With `showEmptySlots=true`, expect a 9-wide empty grid. With it disabled, expect no grid.
- Mixed items: seal a crate containing varied stacks. Hover the resulting item and expect icons plus vanilla stack decorations.
- Reinforced crate: repeat mixed-item test with `immersiveengineering:reinforced_crate`.
- Malformed or missing inventory data: use a crate stack without `minecraft:container`, or with unrelated custom data. Expect no crash and either an empty preview or no preview depending on config.
- Sealed crate: IE sealed crates should preview because IE stores sealed drops in `DataComponents.CONTAINER`.
- Loot-table crate: a supported crate item with `DataComponents.CONTAINER_LOOT` but no generated `DataComponents.CONTAINER` should show the unresolved-loot message instead of attempting to generate or request server data.
- GUI scale: test scale 1, 2, 3, and Auto. The vanilla tooltip positioner should clamp the custom component to screen bounds.
- Dedicated server launch: run the NeoForge server configuration. The common mod should load config only, and client GUI classes should not be loaded.

## In-Game Manual Scenarios To Run With Immersive Engineering Installed

- Open the Engineer's Manual and confirm Storage & Transport contains a `Factory Inventory Inspector` subcategory.
- Open `Engineer's Crate Preview`; expect crate and reinforced crate icons, short readable pages, and links to the other two WIMC entries.
- Open `Supported Crates`; expect the wooden crate and reinforced crate crafting recipe panels to render.
- Use the link from `Supported Crates` to IE's existing `Storage Crates` entry.
- Open `Preview Settings`; expect player-facing config explanations without oversized tables or clipped text.
- Change IE manual GUI scale and bad-eyesight settings; re-open all WIMC entries and confirm no text is cut off.
- Launch without Immersive Engineering; WIMC should still build and dedicated server startup should remain safe.
