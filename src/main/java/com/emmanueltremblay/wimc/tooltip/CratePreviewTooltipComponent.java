package com.emmanueltremblay.wimc.tooltip;

import com.emmanueltremblay.wimc.crate.CratePreviewContents;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

public record CratePreviewTooltipComponent(CratePreviewContents contents) implements TooltipComponent {
}
