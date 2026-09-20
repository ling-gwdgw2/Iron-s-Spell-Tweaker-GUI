package dev.codex.ironspelltweaker.service;

import dev.codex.ironspelltweaker.config.SpellConfigData;
import dev.codex.ironspelltweaker.config.SpellConfigIO;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.resources.ResourceLocation;

import java.util.*;

public class SpellDiscoveryService {
    private static final List<SpellConfigData> CACHED_SPELLS = new ArrayList<>();
    private static final List<SchoolType> CACHED_SCHOOLS = new ArrayList<>();
    private static final List<String> CACHED_MODS = new ArrayList<>();
    private static boolean initialized = false;

    public static synchronized void refresh() {
        CACHED_SPELLS.clear();
        CACHED_SCHOOLS.clear();
        CACHED_MODS.clear();

        Set<SchoolType> schoolSet = new LinkedHashSet<>();
        Set<String> modSet = new LinkedHashSet<>();

        // Ensure all registered schools from SchoolRegistry are included
        if (SchoolRegistry.REGISTRY != null && SchoolRegistry.REGISTRY.get() != null) {
            for (SchoolType school : SchoolRegistry.REGISTRY.get().getValues()) {
                if (school != null) {
                    schoolSet.add(school);
                }
            }
        }

        if (SpellRegistry.REGISTRY != null && SpellRegistry.REGISTRY.get() != null) {
            for (Map.Entry<net.minecraft.resources.ResourceKey<AbstractSpell>, AbstractSpell> entry : SpellRegistry.REGISTRY.get().getEntries()) {
                AbstractSpell spell = entry.getValue();
                ResourceLocation id = entry.getKey().location();

                // Skip placeholder or none spell
                if ("none".equals(id.getPath()) && "irons_spellbooks".equals(id.getNamespace())) {
                    continue;
                }

                SpellConfigData configData = SpellConfigIO.loadSpellConfig(spell, id);
                CACHED_SPELLS.add(configData);

                if (configData.getSchool() != null) {
                    schoolSet.add(configData.getSchool());
                }
                modSet.add(configData.getModId());
            }
        }

        // Sort spells alphabetically by display name
        CACHED_SPELLS.sort(Comparator.comparing(a -> a.getDisplayName().getString().toLowerCase()));

        // Populate schools
        CACHED_SCHOOLS.addAll(schoolSet);
        CACHED_SCHOOLS.sort(Comparator.comparing(s -> s.getDisplayName().getString().toLowerCase()));

        // Populate mods
        CACHED_MODS.addAll(modSet);
        CACHED_MODS.sort(String::compareToIgnoreCase);

        initialized = true;
    }

    public static List<SpellConfigData> getAllSpells() {
        if (!initialized || CACHED_SPELLS.isEmpty()) {
            refresh();
        }
        return Collections.unmodifiableList(CACHED_SPELLS);
    }

    public static List<SchoolType> getAllSchools() {
        if (!initialized || CACHED_SCHOOLS.isEmpty()) {
            refresh();
        }
        return Collections.unmodifiableList(CACHED_SCHOOLS);
    }

    public static List<String> getAllModIds() {
        if (!initialized || CACHED_MODS.isEmpty()) {
            refresh();
        }
        return Collections.unmodifiableList(CACHED_MODS);
    }
}
