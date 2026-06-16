package com.emmanueltremblay.wimc.crate;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public record CratePreviewContents(
        List<ItemStack> slots,
        int columns,
        int rows,
        int totalSlots,
        int hiddenSlots,
        boolean showEmptySlots,
        CratePreviewStatus status
) {
    public static final int PREFERRED_COLUMNS = 9;
    public static final int DEFAULT_CRATE_SLOTS = 27;
    public static final int SLOT_SIZE = 18;

    public static CratePreviewContents create(
            List<ItemStack> sourceSlots,
            int expectedSlots,
            int maxRows,
            int columns,
            boolean showEmptySlots,
            CratePreviewStatus status
    ) {
        int safeColumns = clamp(columns, 1, PREFERRED_COLUMNS);
        int safeRows = clamp(maxRows, 1, 12);
        int slotCapacity = safeColumns * safeRows;
        int totalSlots = Math.max(0, Math.max(expectedSlots, sourceSlots.size()));
        int clampedSlots = Math.min(totalSlots, slotCapacity);
        int visibleSlots = clampedSlots;

        if (!showEmptySlots && status == CratePreviewStatus.EMPTY) {
            visibleSlots = 0;
        } else if (!showEmptySlots) {
            visibleSlots = Math.min(visibleSlots, lastNonEmptySlot(sourceSlots, visibleSlots) + 1);
        }

        int rows = visibleSlots == 0 ? 0 : (int)Math.ceil(visibleSlots / (double)safeColumns);
        List<ItemStack> visible = new ArrayList<>(visibleSlots);
        for (int slot = 0; slot < visibleSlots; slot++) {
            ItemStack stack = slot < sourceSlots.size() ? sourceSlots.get(slot) : ItemStack.EMPTY;
            visible.add(stack.copy());
        }

        int hiddenSlots = hiddenSlots(sourceSlots, totalSlots, clampedSlots, showEmptySlots, status);
        return new CratePreviewContents(List.copyOf(visible), safeColumns, rows, totalSlots, hiddenSlots, showEmptySlots, status);
    }

    public boolean hasRenderableGrid() {
        return rows > 0 && (showEmptySlots || hasAnyItems());
    }

    public boolean hasAnyItems() {
        for (ItemStack stack : slots) {
            if (!stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static int lastNonEmptySlot(List<ItemStack> slots, int visibleLimit) {
        int last = -1;
        int limit = Math.min(slots.size(), visibleLimit);
        for (int slot = 0; slot < limit; slot++) {
            if (!slots.get(slot).isEmpty()) {
                last = slot;
            }
        }
        return last;
    }

    private static int hiddenSlots(
            List<ItemStack> sourceSlots,
            int totalSlots,
            int clampedSlots,
            boolean showEmptySlots,
            CratePreviewStatus status
    ) {
        if (totalSlots <= clampedSlots || (!showEmptySlots && status == CratePreviewStatus.EMPTY)) {
            return 0;
        }
        if (showEmptySlots || hasNonEmptySlotsAfter(sourceSlots, clampedSlots)) {
            return totalSlots - clampedSlots;
        }
        return 0;
    }

    private static boolean hasNonEmptySlotsAfter(List<ItemStack> slots, int startSlot) {
        for (int slot = Math.max(0, startSlot); slot < slots.size(); slot++) {
            if (!slots.get(slot).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private static int clamp(int value, int min, int max) {
        return Math.max(min, Math.min(max, value));
    }
}
