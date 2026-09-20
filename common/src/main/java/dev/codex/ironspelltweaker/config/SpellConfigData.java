package dev.codex.ironspelltweaker.config;

import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class SpellConfigData {
    private final AbstractSpell spell;
    private final ResourceLocation spellId;
    private final Component displayName;
    private SchoolType school;
    private final SchoolType defaultSchool;
    private final ResourceLocation iconResource;
    private final String modId;
    private final String modName;

    // Base spell lore and stats
    private final CastType castType;
    private final float baseCastTime;
    private final int baseManaCost;
    private final float baseSpellPower;
    private final Component descriptionLore;

    // Default values (from mod/game)
    private final double defaultPowerMultiplier;
    private final double defaultManaMultiplier;
    private final double defaultCooldown;
    private final int defaultMaxLevel;
    private final SpellRarity defaultMinRarity;
    private final boolean defaultEnabled;
    private final boolean defaultAllowCrafting;

    // Current edited values
    private double powerMultiplier;
    private double manaMultiplier;
    private double cooldownInSeconds;
    private int maxLevel;
    private SpellRarity minRarity;
    private boolean enabled;
    private boolean allowCrafting;

    private boolean modified = false;
    private boolean hasCustomConfigFile = false;

    public SpellConfigData(
            AbstractSpell spell,
            ResourceLocation spellId,
            Component displayName,
            SchoolType school,
            SchoolType defaultSchool,
            ResourceLocation iconResource,
            String modId,
            String modName,
            CastType castType,
            float baseCastTime,
            int baseManaCost,
            float baseSpellPower,
            Component descriptionLore,
            double powerMultiplier,
            double manaMultiplier,
            double cooldownInSeconds,
            int maxLevel,
            SpellRarity minRarity,
            boolean enabled,
            boolean allowCrafting,
            double defaultPowerMultiplier,
            double defaultManaMultiplier,
            double defaultCooldown,
            int defaultMaxLevel,
            SpellRarity defaultMinRarity,
            boolean defaultEnabled,
            boolean defaultAllowCrafting,
            boolean hasCustomConfigFile
    ) {
        this.spell = spell;
        this.spellId = spellId;
        this.displayName = displayName;
        this.school = school != null ? school : defaultSchool;
        this.defaultSchool = defaultSchool;
        this.iconResource = iconResource;
        this.modId = modId;
        this.modName = modName;

        this.castType = castType;
        this.baseCastTime = baseCastTime;
        this.baseManaCost = baseManaCost;
        this.baseSpellPower = baseSpellPower;
        this.descriptionLore = descriptionLore;

        this.powerMultiplier = powerMultiplier;
        this.manaMultiplier = manaMultiplier;
        this.cooldownInSeconds = cooldownInSeconds;
        this.maxLevel = Math.max(1, Math.min(10, maxLevel));
        this.minRarity = minRarity != null ? minRarity : SpellRarity.COMMON;
        this.enabled = enabled;
        this.allowCrafting = allowCrafting;

        this.defaultPowerMultiplier = defaultPowerMultiplier;
        this.defaultManaMultiplier = defaultManaMultiplier;
        this.defaultCooldown = defaultCooldown;
        this.defaultMaxLevel = defaultMaxLevel;
        this.defaultMinRarity = defaultMinRarity != null ? defaultMinRarity : SpellRarity.COMMON;
        this.defaultEnabled = defaultEnabled;
        this.defaultAllowCrafting = defaultAllowCrafting;

        this.hasCustomConfigFile = hasCustomConfigFile;
    }

    public AbstractSpell getSpell() {
        return spell;
    }

    public ResourceLocation getSpellId() {
        return spellId;
    }

    public Component getDisplayName() {
        return displayName;
    }

    public SchoolType getSchool() {
        return school;
    }

    public void setSchool(SchoolType school) {
        if (school != null && !school.equals(this.school)) {
            this.school = school;
            this.modified = true;
        }
    }

    public SchoolType getDefaultSchool() {
        return defaultSchool;
    }

    public ResourceLocation getIconResource() {
        return iconResource;
    }

    public String getModId() {
        return modId;
    }

    public String getModName() {
        return modName;
    }

    public CastType getCastType() {
        return castType;
    }

    public float getBaseCastTime() {
        return baseCastTime;
    }

    public int getBaseManaCost() {
        return baseManaCost;
    }

    public float getBaseSpellPower() {
        return baseSpellPower;
    }

    public Component getDescriptionLore() {
        return descriptionLore;
    }

    public double getPowerMultiplier() {
        return powerMultiplier;
    }

    public void setPowerMultiplier(double powerMultiplier) {
        this.powerMultiplier = Math.round(powerMultiplier * 10.0) / 10.0;
        this.modified = true;
    }

    public double getManaMultiplier() {
        return manaMultiplier;
    }

    public void setManaMultiplier(double manaMultiplier) {
        this.manaMultiplier = Math.round(manaMultiplier * 10.0) / 10.0;
        this.modified = true;
    }

    public double getCooldownInSeconds() {
        return cooldownInSeconds;
    }

    public void setCooldownInSeconds(double cooldownInSeconds) {
        this.cooldownInSeconds = Math.max(0.0, Math.round(cooldownInSeconds * 10.0) / 10.0);
        this.modified = true;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = Math.max(1, Math.min(10, maxLevel));
        this.modified = true;
    }

    public SpellRarity getMinRarity() {
        return minRarity;
    }

    public void setMinRarity(SpellRarity minRarity) {
        this.minRarity = minRarity;
        this.modified = true;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.modified = true;
    }

    public boolean isAllowCrafting() {
        return allowCrafting;
    }

    public void setAllowCrafting(boolean allowCrafting) {
        this.allowCrafting = allowCrafting;
        this.modified = true;
    }

    public boolean isModified() {
        return modified;
    }

    public boolean hasCustomConfigFile() {
        return hasCustomConfigFile;
    }

    public void setHasCustomConfigFile(boolean hasCustomConfigFile) {
        this.hasCustomConfigFile = hasCustomConfigFile;
    }

    public void resetToDefaults() {
        this.school = defaultSchool;
        this.powerMultiplier = defaultPowerMultiplier;
        this.manaMultiplier = defaultManaMultiplier;
        this.cooldownInSeconds = defaultCooldown;
        this.maxLevel = defaultMaxLevel;
        this.minRarity = defaultMinRarity;
        this.enabled = defaultEnabled;
        this.allowCrafting = defaultAllowCrafting;
        this.modified = true;
    }

    public double getDefaultPowerMultiplier() {
        return defaultPowerMultiplier;
    }

    public double getDefaultManaMultiplier() {
        return defaultManaMultiplier;
    }

    public double getDefaultCooldown() {
        return defaultCooldown;
    }

    public int getDefaultMaxLevel() {
        return defaultMaxLevel;
    }

    public SpellRarity getDefaultMinRarity() {
        return defaultMinRarity;
    }

    public boolean isDefaultEnabled() {
        return defaultEnabled;
    }

    public boolean isDefaultAllowCrafting() {
        return defaultAllowCrafting;
    }
}
