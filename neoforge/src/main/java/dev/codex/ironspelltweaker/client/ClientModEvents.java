package dev.codex.ironspelltweaker.client;

import dev.codex.ironspelltweaker.IronSpellsTweaker;
import dev.codex.ironspelltweaker.IronSpellsTweakerConstants;
import dev.codex.ironspelltweaker.client.gui.SpellTweakerScreen;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

public final class ClientModEvents {
    private ClientModEvents() {}

    public static void init(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(ClientModEvents::clientSetup);

        // Register NeoForge Mod List Config button
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, (container, parent) -> new SpellTweakerScreen(parent));
    }

    private static void clientSetup(final FMLClientSetupEvent event) {
        IronSpellsTweakerConstants.LOGGER.info("Iron's Spell Tweaker GUI initialized on Client");

        // Register main menu and pause screen button hooks
        NeoForge.EVENT_BUS.register(TitleScreenButtonHandler.class);
    }
}
