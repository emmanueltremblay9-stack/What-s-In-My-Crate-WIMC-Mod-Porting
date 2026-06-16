package com.emmanueltremblay.wimc.client;

import com.emmanueltremblay.wimc.Wimc;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = Wimc.MOD_ID, dist = Dist.CLIENT)
public final class WimcClient {
    public WimcClient(IEventBus modBus) {
        modBus.addListener(CratePreviewTooltipEvents::registerTooltipComponents);
        NeoForge.EVENT_BUS.addListener(CratePreviewTooltipEvents::gatherTooltipComponents);
    }
}
