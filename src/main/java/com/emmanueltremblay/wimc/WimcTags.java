package com.emmanueltremblay.wimc;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public final class WimcTags {
    public static final TagKey<Item> INDUSTRIAL_CRATES = TagKey.create(
            Registries.ITEM,
            ResourceLocation.fromNamespaceAndPath(Wimc.MOD_ID, "industrial_crates")
    );

    private WimcTags() {
    }
}
