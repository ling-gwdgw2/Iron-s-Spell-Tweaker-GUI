package dev.codex.ironspelltweaker.client;

import dev.codex.ironspelltweaker.client.gui.SpellTweakerScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TitleScreenButtonHandler {

    @SubscribeEvent
    public static void onScreenInit(ScreenEvent.Init.Post event) {
        if (event.getScreen() instanceof TitleScreen titleScreen) {
            int buttonWidth = 98;
            int buttonHeight = 20;

            int x = Math.min(titleScreen.width - buttonWidth - 8, titleScreen.width / 2 + 104);
            int y = titleScreen.height / 4 + 48 + 72 + 12;

            Button tweakerBtn = Button.builder(Component.translatable("iron_spell_tweaker.menu_button"), b -> {
                Minecraft.getInstance().setScreen(new SpellTweakerScreen(titleScreen));
            })
            .tooltip(Tooltip.create(Component.translatable("iron_spell_tweaker.menu_button.tooltip")))
            .bounds(x, y, buttonWidth, buttonHeight)
            .build();

            event.addListener(tweakerBtn);
        } else if (event.getScreen() instanceof PauseScreen pauseScreen) {
            int buttonWidth = 98;
            int buttonHeight = 20;
            int x = pauseScreen.width / 2 + 104;
            int y = pauseScreen.height / 4 + 72 - 16;

            Button pauseBtn = Button.builder(Component.translatable("iron_spell_tweaker.menu_button"), b -> {
                Minecraft.getInstance().setScreen(new SpellTweakerScreen(pauseScreen));
            })
            .tooltip(Tooltip.create(Component.translatable("iron_spell_tweaker.menu_button.tooltip")))
            .bounds(x, y, buttonWidth, buttonHeight)
            .build();

            event.addListener(pauseBtn);
        }
    }
}
