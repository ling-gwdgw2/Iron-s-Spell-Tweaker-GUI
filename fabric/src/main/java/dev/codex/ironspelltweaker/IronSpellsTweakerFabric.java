package dev.codex.ironspelltweaker;

import dev.codex.ironspelltweaker.client.MenuButtonHelper;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;

public class IronSpellsTweakerFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        IronSpellsTweakerConstants.LOGGER.info("Iron's Spell Tweaker GUI initialized on Fabric!");

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen || screen instanceof PauseScreen) {
                Button btn = MenuButtonHelper.createMenuButton(screen);
                if (btn != null) {
                    Screens.getButtons(screen).add(btn);
                }
            }
        });
    }
}
