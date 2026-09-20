package dev.codex.ironspelltweaker;

import dev.codex.ironspelltweaker.client.gui.SpellTweakerScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

public class IronSpellsTweakerFabric implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        IronSpellsTweakerConstants.LOGGER.info("Iron's Spell Tweaker GUI initialized on Fabric!");

        ScreenEvents.AFTER_INIT.register((client, screen, scaledWidth, scaledHeight) -> {
            if (screen instanceof TitleScreen titleScreen) {
                int buttonWidth = 98;
                int buttonHeight = 20;

                int x = Math.min(titleScreen.width - buttonWidth - 8, titleScreen.width / 2 + 104);
                int y = titleScreen.height / 4 + 48 + 72 + 12;

                Button tweakerBtn = Button.builder(Component.translatable("iron_spell_tweaker.menu_button"), b -> {
                    client.setScreen(new SpellTweakerScreen(titleScreen));
                })
                .tooltip(Tooltip.create(Component.translatable("iron_spell_tweaker.menu_button.tooltip")))
                .bounds(x, y, buttonWidth, buttonHeight)
                .build();

                Screens.getButtons(screen).add(tweakerBtn);
            } else if (screen instanceof PauseScreen pauseScreen) {
                int buttonWidth = 98;
                int buttonHeight = 20;
                int x = pauseScreen.width / 2 + 104;
                int y = pauseScreen.height / 4 + 72 - 16;

                Button pauseBtn = Button.builder(Component.translatable("iron_spell_tweaker.menu_button"), b -> {
                    client.setScreen(new SpellTweakerScreen(pauseScreen));
                })
                .tooltip(Tooltip.create(Component.translatable("iron_spell_tweaker.menu_button.tooltip")))
                .bounds(x, y, buttonWidth, buttonHeight)
                .build();

                Screens.getButtons(screen).add(pauseBtn);
            }
        });
    }
}
