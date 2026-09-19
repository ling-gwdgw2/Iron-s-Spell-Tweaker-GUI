package dev.codex.ironspelltweaker.client.gui;

import dev.codex.ironspelltweaker.config.SpellConfigData;
import dev.codex.ironspelltweaker.config.SpellConfigIO;
import dev.codex.ironspelltweaker.service.SpellDiscoveryService;
import dev.isxander.yacl3.api.*;
import dev.isxander.yacl3.api.controller.*;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.*;

public class YaclTweakerScreenFactory {

    public static Screen createScreen(Screen parent) {
        YetAnotherConfigLib.Builder builder = YetAnotherConfigLib.createBuilder()
                .title(Component.translatable("iron_spell_tweaker.title"));

        List<SpellConfigData> allSpells = SpellDiscoveryService.getAllSpells();
        List<SchoolType> schools = SpellDiscoveryService.getAllSchools();

        // Group spells by school
        Map<SchoolType, List<SpellConfigData>> bySchool = new LinkedHashMap<>();
        for (SchoolType school : schools) {
            bySchool.put(school, new ArrayList<>());
        }

        for (SpellConfigData spell : allSpells) {
            SchoolType school = spell.getSchool();
            if (school != null && bySchool.containsKey(school)) {
                bySchool.get(school).add(spell);
            }
        }

        // Add Category for each School
        for (Map.Entry<SchoolType, List<SpellConfigData>> entry : bySchool.entrySet()) {
            SchoolType school = entry.getKey();
            List<SpellConfigData> spellsInSchool = entry.getValue();
            if (spellsInSchool.isEmpty()) continue;

            ConfigCategory.Builder categoryBuilder = ConfigCategory.createBuilder()
                    .name(school.getDisplayName());

            for (SpellConfigData spell : spellsInSchool) {
                OptionGroup.Builder groupBuilder = OptionGroup.createBuilder()
                        .name(Component.literal(spell.getDisplayName().getString() + " [" + spell.getModName() + "]"))
                        .collapsed(true);

                // 1. Power Multiplier
                groupBuilder.option(Option.<Double>createBuilder()
                        .name(Component.translatable("iron_spell_tweaker.power_multiplier", ""))
                        .binding(Binding.generic(
                                spell.getDefaultPowerMultiplier(),
                                spell::getPowerMultiplier,
                                spell::setPowerMultiplier
                        ))
                        .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(0.1, 10.0).step(0.1))
                        .build());

                // 2. Mana Multiplier
                groupBuilder.option(Option.<Double>createBuilder()
                        .name(Component.translatable("iron_spell_tweaker.mana_multiplier", ""))
                        .binding(Binding.generic(
                                spell.getDefaultManaMultiplier(),
                                spell::getManaMultiplier,
                                spell::setManaMultiplier
                        ))
                        .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(0.1, 10.0).step(0.1))
                        .build());

                // 3. Cooldown
                groupBuilder.option(Option.<Double>createBuilder()
                        .name(Component.translatable("iron_spell_tweaker.cooldown", ""))
                        .binding(Binding.generic(
                                spell.getDefaultCooldown(),
                                spell::getCooldownInSeconds,
                                spell::setCooldownInSeconds
                        ))
                        .controller(opt -> DoubleSliderControllerBuilder.create(opt).range(0.0, 120.0).step(0.5))
                        .build());

                // 4. Max Level
                groupBuilder.option(Option.<Integer>createBuilder()
                        .name(Component.translatable("iron_spell_tweaker.max_level", ""))
                        .binding(Binding.generic(
                                spell.getDefaultMaxLevel(),
                                spell::getMaxLevel,
                                spell::setMaxLevel
                        ))
                        .controller(opt -> IntegerSliderControllerBuilder.create(opt).range(1, 10).step(1))
                        .build());

                // 5. Min Rarity
                groupBuilder.option(Option.<SpellRarity>createBuilder()
                        .name(Component.translatable("iron_spell_tweaker.min_rarity", ""))
                        .binding(Binding.generic(
                                spell.getDefaultMinRarity(),
                                spell::getMinRarity,
                                spell::setMinRarity
                        ))
                        .controller(EnumDropdownControllerBuilder::create)
                        .build());

                // 6. Enabled Toggle
                groupBuilder.option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("iron_spell_tweaker.enabled", ""))
                        .binding(Binding.generic(
                                spell.isDefaultEnabled(),
                                spell::isEnabled,
                                spell::setEnabled
                        ))
                        .controller(TickBoxControllerBuilder::create)
                        .build());

                // 7. Allow Crafting Toggle
                groupBuilder.option(Option.<Boolean>createBuilder()
                        .name(Component.translatable("iron_spell_tweaker.allow_crafting", ""))
                        .binding(Binding.generic(
                                spell.isDefaultAllowCrafting(),
                                spell::isAllowCrafting,
                                spell::setAllowCrafting
                        ))
                        .controller(TickBoxControllerBuilder::create)
                        .build());

                categoryBuilder.group(groupBuilder.build());
            }

            builder.category(categoryBuilder.build());
        }

        builder.save(() -> {
            for (SpellConfigData spell : allSpells) {
                if (spell.isModified()) {
                    SpellConfigIO.saveSpellConfig(spell);
                }
            }
        });

        return builder.build().generateScreen(parent);
    }
}
