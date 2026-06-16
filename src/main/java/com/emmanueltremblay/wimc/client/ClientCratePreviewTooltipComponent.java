package com.emmanueltremblay.wimc.client;

import com.emmanueltremblay.wimc.crate.CratePreviewContents;
import com.emmanueltremblay.wimc.crate.CratePreviewStatus;
import com.emmanueltremblay.wimc.tooltip.CratePreviewTooltipComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

public final class ClientCratePreviewTooltipComponent implements ClientTooltipComponent {
    private static final int SLOT_SIZE = CratePreviewContents.SLOT_SIZE;
    private static final int ITEM_OFFSET = 1;
    private static final int MESSAGE_TOP_PADDING = 3;
    private static final int MESSAGE_HEIGHT = 10;
    private static final int SLOT_BORDER = 0xFF8A8A8A;
    private static final int SLOT_FILL = 0xFF202124;
    private static final int EMPTY_SLOT_FILL = 0x90202024;
    private static final int TEXT_COLOR = 0xFFBFC7C9;

    private final CratePreviewContents contents;

    public ClientCratePreviewTooltipComponent(CratePreviewTooltipComponent component) {
        this.contents = component.contents();
    }

    @Override
    public int getHeight() {
        int gridHeight = contents.hasRenderableGrid() ? contents.rows() * SLOT_SIZE : 0;
        return gridHeight + (hasMessage() ? MESSAGE_TOP_PADDING + MESSAGE_HEIGHT : 0);
    }

    @Override
    public int getWidth(Font font) {
        int gridWidth = contents.hasRenderableGrid() ? contents.columns() * SLOT_SIZE : 0;
        return Math.max(gridWidth, font.width(message()));
    }

    @Override
    public void renderImage(Font font, int x, int y, GuiGraphics graphics) {
        int messageY = y;
        if (contents.hasRenderableGrid()) {
            renderGrid(font, x, y, graphics);
            messageY += contents.rows() * SLOT_SIZE;
        }

        if (hasMessage()) {
            graphics.drawString(font, message(), x, messageY + MESSAGE_TOP_PADDING, TEXT_COLOR, false);
        }
    }

    private void renderGrid(Font font, int x, int y, GuiGraphics graphics) {
        for (int slot = 0; slot < contents.slots().size(); slot++) {
            int slotX = x + (slot % contents.columns()) * SLOT_SIZE;
            int slotY = y + (slot / contents.columns()) * SLOT_SIZE;
            ItemStack stack = contents.slots().get(slot);

            if (contents.showEmptySlots() || !stack.isEmpty()) {
                renderSlotBackground(graphics, slotX, slotY, stack.isEmpty());
            }

            if (!stack.isEmpty()) {
                renderStackSafely(font, graphics, stack, slotX + ITEM_OFFSET, slotY + ITEM_OFFSET);
            }
        }
    }

    private static void renderStackSafely(Font font, GuiGraphics graphics, ItemStack stack, int x, int y) {
        try {
            graphics.renderItem(stack, x, y);
            graphics.renderItemDecorations(font, stack, x, y);
        } catch (RuntimeException exception) {
            // Malformed component data or a broken item renderer should not crash the whole tooltip.
        }
    }

    private static void renderSlotBackground(GuiGraphics graphics, int x, int y, boolean empty) {
        graphics.fill(x, y, x + SLOT_SIZE, y + SLOT_SIZE, SLOT_BORDER);
        graphics.fill(x + 1, y + 1, x + SLOT_SIZE - 1, y + SLOT_SIZE - 1, empty ? EMPTY_SLOT_FILL : SLOT_FILL);
    }

    private boolean hasMessage() {
        return contents.status() != CratePreviewStatus.READY || contents.hiddenSlots() > 0;
    }

    private Component message() {
        if (contents.hiddenSlots() > 0) {
            return Component.translatable("wimc.tooltip.crate_preview.truncated", contents.hiddenSlots());
        }
        if (contents.status() == CratePreviewStatus.LOOT_UNRESOLVED) {
            return Component.translatable("wimc.tooltip.crate_preview.loot");
        }
        if (contents.status() == CratePreviewStatus.EMPTY) {
            return Component.translatable("wimc.tooltip.crate_preview.empty");
        }
        return Component.empty();
    }
}
