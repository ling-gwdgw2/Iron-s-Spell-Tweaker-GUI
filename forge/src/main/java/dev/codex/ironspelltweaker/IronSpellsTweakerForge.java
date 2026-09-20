package dev.codex.ironspelltweaker;

import dev.codex.ironspelltweaker.client.TitleScreenButtonHandler;
import dev.codex.ironspelltweaker.client.gui.SpellTweakerScreen;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(IronSpellsTweakerConstants.MOD_ID)
public class IronSpellsTweakerForge {

    public IronSpellsTweakerForge() {
        IronSpellsTweakerConstants.LOGGER.info("Iron's Spell Tweaker GUI initialized on Forge 1.20.1!");

        MinecraftForge.EVENT_BUS.register(TitleScreenButtonHandler.class);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);

        ModLoadingContext.get().registerExtensionPoint(
                ConfigScreenHandler.ConfigScreenFactory.class,
                () -> new ConfigScreenHandler.ConfigScreenFactory(
                        (mc, parent) -> new SpellTweakerScreen(parent)
                )
        );
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        IronSpellsTweakerConstants.LOGGER.info("Iron's Spell Tweaker GUI client setup complete.");
    }
}
