package com.emmanueltremblay.wimc.crate;

import com.emmanueltremblay.wimc.Wimc;
import com.emmanueltremblay.wimc.WimcClientConfig;
import com.emmanueltremblay.wimc.WimcTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

public final class CratePreviewDetector {
    private static final String IE_MOD_ID = "immersiveengineering";
    private static final ResourceLocation IE_CRATE = ResourceLocation.fromNamespaceAndPath(IE_MOD_ID, "crate");
    private static final ResourceLocation IE_REINFORCED_CRATE = ResourceLocation.fromNamespaceAndPath(IE_MOD_ID, "reinforced_crate");

    private CratePreviewDetector() {
    }

    public static boolean isSupportedCrate(ItemStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (isImmersiveEngineeringCrate(itemId)) {
            return WimcClientConfig.enableImmersiveEngineeringCrates() && ModList.get().isLoaded(IE_MOD_ID);
        }

        return WimcClientConfig.enableOwnModCrates()
                && (isOwnModCrate(itemId) || stack.is(WimcTags.INDUSTRIAL_CRATES));
    }

    public static int expectedSlotCount(ItemStack stack) {
        ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (isImmersiveEngineeringCrate(itemId) || isOwnModCrate(itemId) || stack.is(WimcTags.INDUSTRIAL_CRATES)) {
            return CratePreviewContents.DEFAULT_CRATE_SLOTS;
        }
        return 0;
    }

    private static boolean isImmersiveEngineeringCrate(ResourceLocation itemId) {
        return IE_CRATE.equals(itemId) || IE_REINFORCED_CRATE.equals(itemId);
    }

    private static boolean isOwnModCrate(ResourceLocation itemId) {
        return Wimc.MOD_ID.equals(itemId.getNamespace()) && itemId.getPath().contains("crate");
    }
}
