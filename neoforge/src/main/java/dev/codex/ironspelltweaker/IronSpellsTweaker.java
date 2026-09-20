package dev.codex.ironspelltweaker;

import dev.codex.ironspelltweaker.client.ClientModEvents;
import dev.codex.ironspelltweaker.platform.Services;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(IronSpellsTweakerConstants.MOD_ID)
public class IronSpellsTweaker {
    public static final String MOD_ID = IronSpellsTweakerConstants.MOD_ID;

    public IronSpellsTweaker(IEventBus modEventBus, ModContainer modContainer) {
        IronSpellsTweakerConstants.LOGGER.info("Starting Iron's Spell Tweaker GUI on {}", Services.PLATFORM.getPlatformName());

        if (Services.PLATFORM.isClient()) {
            ClientModEvents.init(modEventBus, modContainer);
        }
    }
}
