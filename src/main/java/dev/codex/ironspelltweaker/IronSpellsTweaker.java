package dev.codex.ironspelltweaker;

import dev.codex.ironspelltweaker.client.ClientModEvents;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(IronSpellsTweaker.MOD_ID)
public class IronSpellsTweaker {
    public static final String MOD_ID = "iron_spell_tweaker";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public IronSpellsTweaker(IEventBus modEventBus, ModContainer modContainer) {
        LOGGER.info("Starting Iron's Spell Tweaker GUI mod");

        if (FMLEnvironment.dist == Dist.CLIENT) {
            ClientModEvents.init(modEventBus, modContainer);
        }
    }
}
