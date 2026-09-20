package dev.codex.ironspelltweaker.client;

import com.terraformersmc.modmenu.api.ConfigScreenFactory;
import com.terraformersmc.modmenu.api.ModMenuApi;
import dev.codex.ironspelltweaker.client.gui.SpellTweakerScreen;

public class IronSpellsTweakerModMenu implements ModMenuApi {
    @Override
    public ConfigScreenFactory<?> getModConfigScreenFactory() {
        return SpellTweakerScreen::new;
    }
}
