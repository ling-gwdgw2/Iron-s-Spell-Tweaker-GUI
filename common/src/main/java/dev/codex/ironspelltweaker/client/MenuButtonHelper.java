package dev.codex.ironspelltweaker.client;

import dev.codex.ironspelltweaker.client.gui.SpellTweakerScreen;
import dev.codex.ironspelltweaker.config.ClientConfig;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;

public class MenuButtonHelper {

    public static Button createMenuButton(Screen screen) {
        ClientConfig.load();

        boolean isTitle = screen instanceof TitleScreen;
        boolean isPause = screen instanceof PauseScreen;

        if (isTitle && !ClientConfig.isShowTitleScreenButton()) {
            return null;
        }
        if (isPause && !ClientConfig.isShowPauseScreenButton()) {
            return null;
        }

        boolean isSquare = ClientConfig.getButtonStyle() == ClientConfig.ButtonStyle.SQUARE;

        int buttonWidth;
        int buttonHeight = 20;
        Component buttonText;

        if (isSquare) {
            buttonWidth = 20;
            buttonText = Component.literal("✦").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD);
        } else {
            buttonWidth = 98;
            buttonText = Component.translatable("iron_spell_tweaker.menu_button");
        }

        int x;
        int y;

        if (isTitle) {
            int baseY = screen.height / 4 + 48 + 72 + 12;
            int baseX;
            if (isSquare) {
                baseX = screen.width / 2 + 104;
                if (baseX + buttonWidth > screen.width - 4) {
                    baseX = screen.width - buttonWidth - 4;
                }
            } else {
                baseX = Math.min(screen.width - buttonWidth - 8, screen.width / 2 + 104);
            }
            x = baseX + ClientConfig.getTitleScreenXOffset();
            y = baseY + ClientConfig.getTitleScreenYOffset();
        } else if (isPause) {
            int baseY = screen.height / 4 + 72 - 16;
            int baseX = screen.width / 2 + 104;
            if (baseX + buttonWidth > screen.width - 4) {
                baseX = screen.width - buttonWidth - 4;
            }
            x = baseX + ClientConfig.getPauseScreenXOffset();
            y = baseY + ClientConfig.getPauseScreenYOffset();
        } else {
            return null;
        }

        Tooltip tooltip = Tooltip.create(
                Component.translatable("iron_spell_tweaker.menu_button")
                        .withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)
                        .append(Component.literal("\n"))
                        .append(Component.translatable("iron_spell_tweaker.menu_button.tooltip").withStyle(ChatFormatting.GRAY))
        );

        return Button.builder(buttonText, btn -> {
            Minecraft.getInstance().setScreen(new SpellTweakerScreen(screen));
        })
        .tooltip(tooltip)
        .bounds(x, y, buttonWidth, buttonHeight)
        .build();
    }
}
