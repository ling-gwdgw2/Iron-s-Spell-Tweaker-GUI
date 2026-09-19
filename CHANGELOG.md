# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

---

## [1.0.0] - 2026-09-20

### Added
- **Interactive Spell Browser**:
  - Real-time search bar filtering by spell name, ID, and mod identifier.
  - Multi-option school filter dropdown.
  - Addon mod discovery filter (`irons_spellbooks`, addon mods, etc.).
  - "Edited Only" toggle to inspect only modified/customized spells.
  - Custom spell card rendering with 2px school accent stripe and glowing outlines.
- **Real-Time Stat Tweaking**:
  - Live sliders for Power Multiplier ($0.1\times$ to $10.0\times$), Mana Cost Multiplier ($0.1\times$ to $10.0\times$), Cooldown ($0.0\text{s}$ to $120.0\text{s}$), and Max Level ($1$ to $10$).
  - Dropdown selector for Minimum Rarity (Common to Legendary).
  - Magic School reassignment selector.
  - Toggle buttons for In-Game Enabling and Inscription Table Crafting.
- **Base Stats & Description Lore Preview**:
  - Displays original base cast type, base cast time, base mana cost, and base power.
  - Multi-line word-wrapped guide/lore descriptions extracted from translation keys.
- **Batch Tweak Panel**:
  - Target school selector (specific school or All Schools).
  - Custom percentage adjustment sliders for Power ($-50\%$ to $+200\%$), Cooldown ($-80\%$ to $+100\%$), and Mana ($-80\%$ to $+100\%$).
  - Quick preset chips (`+20% Pwr`, `+50% Pwr`, `-20% CD`, `-50% CD`).
  - Mass Enable All and Disable All toggles.
- **Safety Confirmation Modal Dialog**:
  - In-screen overlay dialog box styled with Minecraft UI aesthetics.
  - Intercepts clicks and scroll events to prevent accidental modifications.
  - Protects `Delete JSON`, `Batch Apply`, `Disable All`, and `Reset All`.
  - Full keyboard accessibility (`ESC` to cancel, `ENTER` to confirm).
- **Factory Reset All**:
  - 1-click button to purge all custom config files in `.minecraft/config/irons_spellbooks_spell_config/` and revert all spells to defaults.
- **Performance**:
  - 0% CPU/memory overhead during world play (zero tick events, zero background threads).
- **Localization**:
  - Full English (`en_us`) and Thai (`th_th`) localization.
