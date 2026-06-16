package com.emmanueltremblay.wimc.crate;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.component.ItemContainerContents;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class CrateInventoryExtractor {
    private CrateInventoryExtractor() {
    }

    public static Optional<CratePreviewContents> extract(
            ItemStack stack,
            @Nullable HolderLookup.Provider registries,
            int maxRows,
            int columns,
            boolean showEmptySlots
    ) {
        if (!CratePreviewDetector.isSupportedCrate(stack)) {
            return Optional.empty();
        }

        int expectedSlots = CratePreviewDetector.expectedSlotCount(stack);

        Optional<CratePreviewContents> dataComponentPreview = fromContainerComponent(stack, expectedSlots, maxRows, columns, showEmptySlots);
        if (dataComponentPreview.isPresent()) {
            return dataComponentPreview;
        }

        if (stack.get(DataComponents.CONTAINER_LOOT) != null) {
            return Optional.of(CratePreviewContents.create(
                    List.of(),
                    0,
                    maxRows,
                    columns,
                    false,
                    CratePreviewStatus.LOOT_UNRESOLVED
            ));
        }

        Optional<CratePreviewContents> blockEntityPreview = fromBlockEntityData(stack, registries, expectedSlots, maxRows, columns, showEmptySlots);
        if (blockEntityPreview.isPresent()) {
            return blockEntityPreview;
        }

        Optional<CratePreviewContents> capabilityPreview = fromItemCapability(stack, maxRows, columns, showEmptySlots);
        if (capabilityPreview.isPresent()) {
            return capabilityPreview;
        }

        if (showEmptySlots && expectedSlots > 0) {
            return Optional.of(CratePreviewContents.create(
                    List.of(),
                    expectedSlots,
                    maxRows,
                    columns,
                    true,
                    CratePreviewStatus.EMPTY
            ));
        }

        return Optional.empty();
    }

    private static Optional<CratePreviewContents> fromContainerComponent(
            ItemStack stack,
            int expectedSlots,
            int maxRows,
            int columns,
            boolean showEmptySlots
    ) {
        ItemContainerContents contents = stack.get(DataComponents.CONTAINER);
        if (contents == null) {
            return Optional.empty();
        }

        try {
            int componentSlots = contents.getSlots();
            int totalSlots = Math.max(expectedSlots, componentSlots);
            NonNullList<ItemStack> slots = NonNullList.withSize(totalSlots, ItemStack.EMPTY);
            contents.copyInto(slots);

            CratePreviewStatus status = hasAnyItem(slots) ? CratePreviewStatus.READY : CratePreviewStatus.EMPTY;
            return Optional.of(CratePreviewContents.create(slots, totalSlots, maxRows, columns, showEmptySlots, status));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private static Optional<CratePreviewContents> fromBlockEntityData(
            ItemStack stack,
            @Nullable HolderLookup.Provider registries,
            int expectedSlots,
            int maxRows,
            int columns,
            boolean showEmptySlots
    ) {
        if (registries == null || expectedSlots <= 0) {
            return Optional.empty();
        }

        CustomData blockEntityData = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (blockEntityData == null || !blockEntityData.contains("Items")) {
            return Optional.empty();
        }

        try {
            CompoundTag tag = blockEntityData.copyTag();
            if (!tag.contains("Items", Tag.TAG_LIST)) {
                return Optional.empty();
            }

            NonNullList<ItemStack> slots = NonNullList.withSize(expectedSlots, ItemStack.EMPTY);
            ContainerHelper.loadAllItems(tag, slots, registries);
            CratePreviewStatus status = hasAnyItem(slots) ? CratePreviewStatus.READY : CratePreviewStatus.EMPTY;
            return Optional.of(CratePreviewContents.create(slots, expectedSlots, maxRows, columns, showEmptySlots, status));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private static Optional<CratePreviewContents> fromItemCapability(
            ItemStack stack,
            int maxRows,
            int columns,
            boolean showEmptySlots
    ) {
        try {
            IItemHandler handler = stack.getCapability(Capabilities.ItemHandler.ITEM, null);
            if (handler == null || handler.getSlots() <= 0) {
                return Optional.empty();
            }

            List<ItemStack> slots = new ArrayList<>(handler.getSlots());
            for (int slot = 0; slot < handler.getSlots(); slot++) {
                slots.add(handler.getStackInSlot(slot).copy());
            }

            CratePreviewStatus status = hasAnyItem(slots) ? CratePreviewStatus.READY : CratePreviewStatus.EMPTY;
            return Optional.of(CratePreviewContents.create(slots, handler.getSlots(), maxRows, columns, showEmptySlots, status));
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    private static boolean hasAnyItem(List<ItemStack> slots) {
        for (ItemStack slot : slots) {
            if (!slot.isEmpty()) {
                return true;
            }
        }
        return false;
    }
}
