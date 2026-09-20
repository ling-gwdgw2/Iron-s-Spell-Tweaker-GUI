package dev.codex.ironspelltweaker.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.codex.ironspelltweaker.IronSpellsTweakerConstants;
import dev.codex.ironspelltweaker.platform.Services;
import io.redspace.ironsspellbooks.api.config.IronConfigParameters;
import io.redspace.ironsspellbooks.api.config.SpellConfigManager;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.Util;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.util.List;

public final class SpellConfigIO {
    public static final String KEY_POWER = "irons_spellbooks:power_multiplier";
    public static final String KEY_MANA = "irons_spellbooks:mana_cost_multiplier";
    public static final String KEY_COOLDOWN = "irons_spellbooks:cooldown_in_seconds";
    public static final String KEY_MAX_LEVEL = "irons_spellbooks:max_level";
    public static final String KEY_MIN_RARITY = "irons_spellbooks:min_rarity";
    public static final String KEY_ENABLED = "irons_spellbooks:enabled";
    public static final String KEY_ALLOW_CRAFTING = "irons_spellbooks:allow_crafting";
    public static final String KEY_SCHOOL = "irons_spellbooks:school";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private SpellConfigIO() {}

    public static File getBaseConfigDir() {
        File dir = Services.PLATFORM.getConfigDir().resolve("irons_spellbooks_spell_config").toFile();
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static File getSpellConfigFile(ResourceLocation spellId) {
        File dir = new File(getBaseConfigDir(), spellId.getNamespace());
        return new File(dir, spellId.getPath() + ".json");
    }

    public static SpellConfigData loadSpellConfig(AbstractSpell spell, ResourceLocation spellId) {
        Component displayName = spell.getDisplayName(null);
        ResourceLocation icon = spell.getSpellIconResource();
        String modId = spellId.getNamespace();
        String modName = formatModName(modId);

        // 1. Fetch authentic default school from spell's DefaultConfig
        SchoolType defaultSchool = null;
        try {
            io.redspace.ironsspellbooks.api.config.DefaultConfig dc = spell.getDefaultConfig();
            if (dc != null && dc.schoolResource != null) {
                defaultSchool = SchoolRegistry.getSchool(dc.schoolResource);
            }
        } catch (Throwable ignored) {}

        if (defaultSchool == null) {
            try {
                defaultSchool = spell.getSchoolType();
            } catch (Throwable ignored) {}
        }
        if (defaultSchool == null) {
            try {
                defaultSchool = SchoolRegistry.EVOCATION.get();
            } catch (Throwable ignored) {}
        }

        SchoolType activeSchool = defaultSchool;

        // 2. Fetch base stats & lore
        CastType castType = CastType.INSTANT;
        float baseCastTime = 0.0f;
        int baseManaCost = 0;
        float baseSpellPower = 0.0f;
        Component lore = Component.empty();

        try {
            castType = spell.getCastType();
            baseCastTime = spell.getCastTime(1) / 20.0f;
            baseManaCost = spell.getManaCost(1);
            baseSpellPower = spell.getSpellPower(1, null);

            String guideKey = spell.getComponentId() + ".guide";
            String descKey = spell.getComponentId() + ".desc";
            String descriptionKey = spell.getComponentId() + ".description";

            if (I18n.exists(guideKey)) {
                lore = Component.translatable(guideKey);
            } else if (I18n.exists(descKey)) {
                lore = Component.translatable(descKey);
            } else if (I18n.exists(descriptionKey)) {
                lore = Component.translatable(descriptionKey);
            }
        } catch (Throwable ignored) {}

        // 3. Fetch authentic default fallback values from DefaultConfig
        double defPower = 1.0;
        double defMana = 1.0;
        double defCooldown = 10.0;
        int defMaxLevel = 10;
        SpellRarity defMinRarity = SpellRarity.COMMON;
        boolean defEnabled = true;
        boolean defCrafting = true;

        try {
            io.redspace.ironsspellbooks.api.config.DefaultConfig dc = spell.getDefaultConfig();
            if (dc != null) {
                if (dc.minRarity != null) {
                    defMinRarity = dc.minRarity;
                }
                if (dc.maxLevel > 0) {
                    defMaxLevel = dc.maxLevel;
                }
                defCooldown = dc.cooldownInSeconds;
                defEnabled = dc.enabled;
                defCrafting = dc.allowCrafting;
            } else {
                defCooldown = spell.getSpellCooldown() / 20.0;
                defMaxLevel = spell.getMaxLevel();
                defEnabled = spell.isEnabled();
                defCrafting = spell.allowCrafting();
            }
        } catch (Throwable ignored) {}

        double power = defPower;
        double mana = defMana;
        double cooldown = defCooldown;
        int maxLevel = defMaxLevel;
        SpellRarity minRarity = defMinRarity;
        boolean enabled = defEnabled;
        boolean crafting = defCrafting;
        boolean hasCustomConfigFile = false;

        // 4. Check if custom JSON file exists
        File file = getSpellConfigFile(spellId);
        if (file.exists() && file.isFile()) {
            hasCustomConfigFile = true;
            try (FileReader reader = new FileReader(file, StandardCharsets.UTF_8)) {
                JsonObject obj = JsonParser.parseReader(reader).getAsJsonObject();
                if (obj.has(KEY_POWER)) power = obj.get(KEY_POWER).getAsDouble();
                if (obj.has(KEY_MANA)) mana = obj.get(KEY_MANA).getAsDouble();
                if (obj.has(KEY_COOLDOWN)) cooldown = obj.get(KEY_COOLDOWN).getAsDouble();
                if (obj.has(KEY_MAX_LEVEL)) maxLevel = obj.get(KEY_MAX_LEVEL).getAsInt();
                if (obj.has(KEY_MIN_RARITY)) {
                    String rarStr = obj.get(KEY_MIN_RARITY).getAsString().toUpperCase();
                    try {
                        minRarity = SpellRarity.valueOf(rarStr);
                    } catch (Exception e) {
                        minRarity = SpellRarity.COMMON;
                    }
                }
                if (obj.has(KEY_ENABLED)) enabled = obj.get(KEY_ENABLED).getAsBoolean();
                if (obj.has(KEY_ALLOW_CRAFTING)) crafting = obj.get(KEY_ALLOW_CRAFTING).getAsBoolean();
                if (obj.has(KEY_SCHOOL)) {
                    String schStr = obj.get(KEY_SCHOOL).getAsString();
                    SchoolType sch = SchoolRegistry.getSchool(new ResourceLocation(schStr));
                    if (sch != null) {
                        activeSchool = sch;
                    }
                }
            } catch (Exception e) {
                IronSpellsTweakerConstants.LOGGER.error("Failed to read spell config for " + spellId, e);
            }
        }

        return new SpellConfigData(
                spell, spellId, displayName, activeSchool, defaultSchool, icon, modId, modName,
                castType, baseCastTime, baseManaCost, baseSpellPower, lore,
                power, mana, cooldown, maxLevel, minRarity, enabled, crafting,
                defPower, defMana, defCooldown, defMaxLevel, defMinRarity, defEnabled, defCrafting,
                hasCustomConfigFile
        );
    }

    public static boolean saveSpellConfig(SpellConfigData data) {
        File file = getSpellConfigFile(data.getSpellId());
        try {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            JsonObject obj = new JsonObject();
            obj.addProperty(KEY_POWER, data.getPowerMultiplier());
            obj.addProperty(KEY_MANA, data.getManaMultiplier());
            obj.addProperty(KEY_COOLDOWN, data.getCooldownInSeconds());
            obj.addProperty(KEY_MAX_LEVEL, data.getMaxLevel());
            obj.addProperty(KEY_MIN_RARITY, data.getMinRarity().name().toLowerCase());
            obj.addProperty(KEY_ENABLED, data.isEnabled());
            obj.addProperty(KEY_ALLOW_CRAFTING, data.isAllowCrafting());
            if (data.getSchool() != null) {
                obj.addProperty(KEY_SCHOOL, data.getSchool().getId().toString());
            }

            try (FileWriter writer = new FileWriter(file, StandardCharsets.UTF_8)) {
                GSON.toJson(obj, writer);
            }

            data.setHasCustomConfigFile(true);

            // Reload Iron's Spells registry
            try {
                SpellRegistry.onConfigReload();
            } catch (Throwable t) {
                IronSpellsTweakerConstants.LOGGER.warn("Could not invoke SpellRegistry.onConfigReload(): " + t.getMessage());
            }

            return true;
        } catch (Exception e) {
            IronSpellsTweakerConstants.LOGGER.error("Failed to save spell config for " + data.getSpellId(), e);
            return false;
        }
    }

    public static boolean deleteSpellConfigFile(SpellConfigData data) {
        File file = getSpellConfigFile(data.getSpellId());
        boolean deleted = false;
        if (file.exists()) {
            deleted = file.delete();
        }
        data.setHasCustomConfigFile(false);
        data.resetToDefaults();

        try {
            SpellRegistry.onConfigReload();
        } catch (Throwable ignored) {}

        return deleted;
    }

    public static int resetAllSpellConfigs(List<SpellConfigData> allSpells) {
        int count = 0;
        for (SpellConfigData spell : allSpells) {
            File file = getSpellConfigFile(spell.getSpellId());
            if (file.exists()) {
                if (file.delete()) {
                    count++;
                }
            }
            spell.setHasCustomConfigFile(false);
            spell.resetToDefaults();
        }

        // Also clean up any remaining JSON files and empty namespace subdirectories
        try {
            File baseDir = getBaseConfigDir();
            if (baseDir.exists() && baseDir.isDirectory()) {
                File[] subDirs = baseDir.listFiles(File::isDirectory);
                if (subDirs != null) {
                    for (File subDir : subDirs) {
                        File[] jsonFiles = subDir.listFiles((dir, name) -> name.endsWith(".json"));
                        if (jsonFiles != null) {
                            for (File jf : jsonFiles) {
                                if (jf.delete()) {
                                    count++;
                                }
                            }
                        }
                        subDir.delete(); // Delete if directory became empty
                    }
                }
            }
        } catch (Throwable t) {
            IronSpellsTweakerConstants.LOGGER.warn("Failed to clean empty config subdirectories: " + t.getMessage());
        }

        try {
            SpellRegistry.onConfigReload();
        } catch (Throwable ignored) {}

        return count;
    }

    public static int batchTweakSchool(SchoolType targetSchool, double powerFactor, double manaFactor, double cooldownFactor, Boolean enabledState, List<SpellConfigData> allSpells) {
        int count = 0;
        for (SpellConfigData spell : allSpells) {
            if (targetSchool == null || targetSchool.equals(spell.getSchool())) {
                boolean changed = false;
                if (powerFactor > 0 && Math.abs(powerFactor - 1.0) > 0.001) {
                    spell.setPowerMultiplier(spell.getPowerMultiplier() * powerFactor);
                    changed = true;
                }
                if (manaFactor > 0 && Math.abs(manaFactor - 1.0) > 0.001) {
                    spell.setManaMultiplier(spell.getManaMultiplier() * manaFactor);
                    changed = true;
                }
                if (cooldownFactor > 0 && Math.abs(cooldownFactor - 1.0) > 0.001) {
                    spell.setCooldownInSeconds(spell.getCooldownInSeconds() * cooldownFactor);
                    changed = true;
                }
                if (enabledState != null) {
                    spell.setEnabled(enabledState);
                    changed = true;
                }
                if (changed) {
                    saveSpellConfig(spell);
                    count++;
                }
            }
        }
        if (count > 0) {
            try {
                SpellRegistry.onConfigReload();
            } catch (Throwable ignored) {}
        }
        return count;
    }

    public static void openConfigFolder() {
        Util.getPlatform().openFile(getBaseConfigDir());
    }

    private static String formatModName(String modId) {
        if ("irons_spellbooks".equals(modId)) {
            return "Iron's Spells";
        }
        String[] parts = modId.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }
}
