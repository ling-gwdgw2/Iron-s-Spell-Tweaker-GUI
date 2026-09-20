# 🪄 Iron's Spell Tweaker GUI (Forge 1.20.1)

[![Minecraft 1.20.1](https://img.shields.io/badge/Minecraft-1.20.1-brightgreen.svg)](https://minecraft.net/)
[![Forge](https://img.shields.io/badge/ModLoader-Forge%2047.1.0+-orange.svg)](https://files.minecraftforge.net/net/minecraftforge/forge/index_1.20.1.html)
[![Java 17](https://img.shields.io/badge/Java-17-blue.svg)](https://adoptium.net/)
[![Main Branch](https://img.shields.io/badge/1.21.1-NeoForge%20%26%20Fabric%20(main)-brightgreen.svg)](https://github.com/ling-gwdgw2/Iron-s-Spell-Tweaker-GUI/tree/main)

An intuitive, modern in-game GUI mod for **Minecraft Forge 1.20.1** that allows players and modpack creators to freely tweak, balance, and customize all spells from [Iron's Spells 'n Spellbooks](https://www.curseforge.com/minecraft/mc-mods/irons-spells-n-spellbooks) and its addons—directly from the title screen or in-game pause menu with **zero restart required**!

> **Looking for 1.21.1 (NeoForge & Fabric)?** Check out the [`main`](https://github.com/ling-gwdgw2/Iron-s-Spell-Tweaker-GUI/tree/main) branch!

---

## ✨ Features

### 🔍 Interactive Spell Browser
- **Instant Search**: Type to search spells in real time by display name, spell ID, or mod ID.
- **School Filter**: Filter by Fire, Ice, Lightning, Holy, Ender, Blood, Evocation, Nature, Eldritch, or any custom school.
- **Addon/Mod Filter**: Automatically discovers all installed spell addons and allows filtering per mod.
- **"Edited Only" Filter**: Quickly toggle to view only spells that have custom configs or unsaved tweaks.
- **Dynamic Theming**: Each spell card features authentic school color accents and borders.

### ⚡ Live Stat Tweaks
- **Power Multiplier**: Tune damage and spell potency ($0.1\times$ to $10.0\times$).
- **Mana Cost Multiplier**: Adjust mana consumption ($0.1\times$ to $10.0\times$).
- **Cooldown**: Set cooldowns precisely from $0.0\text{s}$ to $120.0\text{s}$.
- **Max Level**: Adjust upgrade cap from level 1 to 10.
- **Min Rarity**: Cycle through Common, Uncommon, Rare, Epic, and Legendary.
- **School Reassignment**: Freely reassign spells to different magic schools.
- **Enable / Disable Toggle**: Disables spell casting and scroll generation.
- **Crafting Toggle**: Toggle whether spell scrolls can be crafted at the Inscription Table.

### 📖 Base Stats & Lore Preview
- **Base Stats Row**: Inspect original Cast Type (Instant, Charge, Continuous, Passive), Base Cast Time (seconds), Base Mana Cost, and Base Spell Power before applying multipliers.
- **Description Lore**: Multi-line word-wrapped guide/lore extracted directly from language definitions.

### ⚡ Batch Tweak Mode (Mass Adjustment)
- **Target School Selector**: Apply adjustments to all spells in a specific school or across all schools.
- **Percentage Sliders**:
  - Power: $-50\%$ to $+200\%$ ($5\%$ step).
  - Cooldown: $-80\%$ to $+100\%$ ($5\%$ step).
  - Mana Cost: $-80\%$ to $+100\%$ ($5\%$ step).
- **Quick Preset Chips**: $+20\%$ Power, $+50\%$ Power, $-20\%$ Cooldown, $-50\%$ Cooldown.
- **Mass Toggle**: Enable All / Disable All for an entire school in one click.

### 🛡️ Safety Confirmation Modals & Factory Reset
- **In-Screen Modal Dialog**: Prevents accidental clicks on destructive actions (`Delete JSON`, `Batch Apply`, `Disable All`, `Reset All`).
  - Full keyboard support: `ESC` to cancel, `ENTER` to confirm.
  - Complete click interception to prevent background click-through.
- **🗑️ Factory Reset All**: 1-click button to purge all custom config files in `.minecraft/config/irons_spellbooks_spell_config/` and revert all spells to mod defaults.

### 🚀 Zero Gameplay Performance Overhead
- **Audited Architecture**: **0% background CPU / memory overhead** during world gameplay.
- **No Tick Handlers & No Background Threads**: The mod only executes when the GUI is explicitly open.

### 🌐 Bilingual Support
- English (`en_us`)
- ภาษาไทย (`th_th`)

---

## 📦 Requirements & Dependencies
 
| Dependency | Version | Required? |
|---|---|---|
| **Minecraft** | `1.20.1` | Required |
| **Forge** | `47.1.0` or newer | Required |
| **Iron's Spells 'n Spellbooks** | `1.20.1-3.0.0+` | Required |

---

## 🛠️ Installation

1. Download the latest release `.jar` from the [Releases](https://github.com/ling-gwdgw2/Iron-s-Spell-Tweaker-GUI/releases) page.
2. Ensure you have **Forge 1.20.1** and **Iron's Spells 'n Spellbooks** installed.
3. Place `Iron's Spell Tweaker GUI-Forge-1.20.1-1.0.0.jar` into your `.minecraft/mods` directory.
4. Launch the game! A new **"Spell Tweaker"** button will appear on the title screen and pause menu.

---

## 💻 Building from Source

### Prerequisites
- [JDK 17](https://adoptium.net/) or higher installed and set on your `JAVA_HOME` / `PATH`.
- Git installed.

### Steps
```bash
# 1. Clone the repository and switch to the Forge 1.20.1 branch
git clone -b 1.20.1-forge https://github.com/ling-gwdgw2/Iron-s-Spell-Tweaker-GUI.git
cd Iron-s-Spell-Tweaker-GUI

# 2. Build with Gradle
.\gradlew.bat :forge:jar

# Or build with Python script:
python build_forge.py
```

The compiled mod JAR will be located in:
`forge/build/libs/Iron's Spell Tweaker GUI-Forge-1.20.1-1.0.0.jar`

---

## 🏗️ Architecture

```
Iron's Spell Tweaker GUI/
├── common/                  # Shared UI Screens, Sliders, JSON I/O, Discovery Service
│   ├── src/main/java/       # 1.20.1 compatible UI and configuration logic
│   └── src/main/resources/  # Assets (lang/en_us.json, lang/th_th.json, icon.png)
├── forge/                   # Forge 1.20.1 adapter & entrypoints
│   ├── src/main/java/       # @Mod, TitleScreenButtonHandler, ForgePlatformHelper
│   └── src/main/resources/  # META-INF/mods.toml, ServiceLoader SPI
├── libs_forge/              # Forge 1.20.1 & Iron's Spells 1.20.1 compile libraries
└── build_forge.py           # Standalone automated build script
```

---

## 📄 How It Works

Iron's Spells 'n Spellbooks natively reads individual JSON configuration files located at:
```
.minecraft/config/irons_spellbooks_spell_config/<namespace>/<spell_path>.json
```

**Iron's Spell Tweaker GUI** acts as a visual interface for this system:
1. **Reads authentic defaults** directly from each spell's `DefaultConfig` in memory.
2. **Generates and saves** standard Iron's Spells JSON config files formatted identically to manual configs.
3. **Calls `SpellRegistry.onConfigReload()`** upon saving or deleting configs, allowing real-time stat updates without restarting the game or server.

---

## 📜 License

This project is licensed under the [MIT License](LICENSE) - feel free to use, modify, and distribute this mod in your modpacks!
