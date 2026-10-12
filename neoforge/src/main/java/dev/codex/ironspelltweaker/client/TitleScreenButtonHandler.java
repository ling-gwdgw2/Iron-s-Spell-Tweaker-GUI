package dev.codex.ironspelltweaker.client;

import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

public class TitleScreenButtonHandler {

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof TitleScreen || event.getScreen() instanceof PauseScreen) {
            Button btn = MenuButtonHelper.createMenuButton(event.getScreen());
            if (btn != null) {
                event.addListener(btn);
            }
        }
    }
}
