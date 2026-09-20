package dev.codex.ironspelltweaker.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import dev.codex.ironspelltweaker.config.SpellConfigData;
import dev.codex.ironspelltweaker.config.SpellConfigIO;
import dev.codex.ironspelltweaker.service.SpellDiscoveryService;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.*;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;

public class SpellTweakerScreen extends Screen {
    private final Screen parent;

    // Filter states
    private EditBox searchBox;
    private String currentSearch = "";
    private SchoolType selectedSchoolFilter = null; // null = All
    private String selectedModFilter = "all"; // "all" = All mods
    private boolean onlyModifiedFilter = false;

    private final List<SpellConfigData> filteredSpells = new ArrayList<>();
    private SpellConfigData selectedSpell = null;

    // Scrolling for left list
    private double scrollOffset = 0;
    private static final int ENTRY_HEIGHT = 32;

    // Layout constants
    private int listTop;
    private int listBottom;
    private int listHeight;
    private int leftWidth;
    private int rightX;
    private int rightWidth;

    // Batch mode state
    private boolean batchMode = false;

    // Notification / Toast
    private String statusMessage = null;
    private int statusTimer = 0;
    private boolean statusSuccess = true;

    // Tweak widgets (right side)
    private CustomSlider powerSlider;
    private CustomSlider manaSlider;
    private CustomSlider cooldownSlider;
    private CustomSlider maxLevelSlider;
    private CycleButton<SpellRarity> rarityButton;
    private CycleButton<SchoolType> schoolReassignButton;
    private CycleButton<Boolean> enabledButton;
    private CycleButton<Boolean> craftingButton;
    private Button saveButton;
    private Button resetButton;
    private Button deleteButton;

    // Batch mode state & widgets
    private SchoolType batchTargetSchool = null; // null = All Schools
    private double batchPowerPercent = 20.0;
    private double batchCooldownPercent = -20.0;
    private double batchManaPercent = 0.0;

    private CycleButton<SchoolFilterOption> batchSchoolBtn;
    private CustomSlider batchPowerSlider;
    private CustomSlider batchCooldownSlider;
    private CustomSlider batchManaSlider;
    private Button batchPresetP20;
    private Button batchPresetP50;
    private Button batchPresetCd20;
    private Button batchPresetCd50;
    private Button batchEnableBtn;
    private Button batchDisableBtn;
    private Button batchApplyBtn;
    private Button batchResetBtn;
    private Button closeBatchBtn;
    private final List<SchoolFilterOption> batchSchoolOptions = new ArrayList<>();

    // Confirmation Modal state & widgets
    private boolean showConfirmModal = false;
    private Component modalTitle = CommonComponents.EMPTY;
    private Component modalMessage = CommonComponents.EMPTY;
    private Component modalSubMessage = CommonComponents.EMPTY;
    private int modalAccentColor = 0xFFFF5555;
    private Runnable onConfirmAction = null;
    private Button modalConfirmBtn;
    private Button modalCancelBtn;
    private Button resetAllButton;

    public SpellTweakerScreen(Screen parent) {
        super(Component.translatable("iron_spell_tweaker.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        SpellDiscoveryService.refresh();
        updateFilter();

        if (selectedSpell == null && !filteredSpells.isEmpty()) {
            selectedSpell = filteredSpells.get(0);
        }

        this.leftWidth = (int) (this.width * 0.44);
        this.rightX = leftWidth + 24;
        this.rightWidth = this.width - rightX - 16;
        this.listTop = 68;
        this.listBottom = this.height - 32;
        this.listHeight = listBottom - listTop;

        int halfLeftBtnW = (leftWidth - 4) / 2;

        // Row 1 (y=20): Search Bar + School Filter CycleButton
        this.searchBox = new EditBox(this.font, 16, 20, halfLeftBtnW, 18, Component.translatable("iron_spell_tweaker.search.placeholder"));
        this.searchBox.setHint(Component.translatable("iron_spell_tweaker.search.placeholder").withStyle(ChatFormatting.GRAY));
        this.searchBox.setValue(currentSearch);
        this.searchBox.setResponder(text -> {
            this.currentSearch = text.trim().toLowerCase(Locale.ROOT);
            this.scrollOffset = 0;
            updateFilter();
        });
        this.addRenderableWidget(this.searchBox);

        List<SchoolFilterOption> schoolOptions = new ArrayList<>();
        schoolOptions.add(SchoolFilterOption.ALL);
        SchoolFilterOption initialSchool = SchoolFilterOption.ALL;
        for (SchoolType s : SpellDiscoveryService.getAllSchools()) {
            if (s != null) {
                SchoolFilterOption opt = new SchoolFilterOption(s, Component.literal(s.getDisplayName().getString()));
                schoolOptions.add(opt);
                if (s.equals(this.selectedSchoolFilter)) {
                    initialSchool = opt;
                }
            }
        }

        CycleButton<SchoolFilterOption> schoolFilterBtn = CycleButton.<SchoolFilterOption>builder(SchoolFilterOption::name)
                .withValues(schoolOptions)
                .withInitialValue(initialSchool)
                .create(16 + halfLeftBtnW + 4, 20, halfLeftBtnW, 18, Component.translatable("iron_spell_tweaker.filter.school"), (btn, val) -> {
                    this.selectedSchoolFilter = val.school();
                    this.scrollOffset = 0;
                    updateFilter();
                });
        this.addRenderableWidget(schoolFilterBtn);

        // Row 2 (y=42): Mod Filter CycleButton + "Only Modified" Toggle Button
        List<String> modOptions = new ArrayList<>();
        modOptions.add("all");
        modOptions.addAll(SpellDiscoveryService.getAllModIds());

        CycleButton<String> modFilterBtn = CycleButton.<String>builder(m -> "all".equals(m)
                ? Component.translatable("iron_spell_tweaker.filter.all_mods")
                : Component.literal(formatModName(m)))
                .withValues(modOptions)
                .withInitialValue(selectedModFilter)
                .create(16, 42, halfLeftBtnW, 18, Component.translatable("iron_spell_tweaker.filter.mod"), (btn, val) -> {
                    this.selectedModFilter = val;
                    this.scrollOffset = 0;
                    updateFilter();
                });
        this.addRenderableWidget(modFilterBtn);

        Button modifiedOnlyBtn = Button.builder(
                this.onlyModifiedFilter ? Component.translatable("iron_spell_tweaker.filter.only_modified").withStyle(ChatFormatting.GOLD)
                                        : Component.translatable("iron_spell_tweaker.filter.all_status"),
                btn -> {
                    this.onlyModifiedFilter = !this.onlyModifiedFilter;
                    btn.setMessage(this.onlyModifiedFilter ? Component.translatable("iron_spell_tweaker.filter.only_modified").withStyle(ChatFormatting.GOLD)
                                                          : Component.translatable("iron_spell_tweaker.filter.all_status"));
                    this.scrollOffset = 0;
                    updateFilter();
                }
        ).bounds(16 + halfLeftBtnW + 4, 42, halfLeftBtnW, 18).build();
        this.addRenderableWidget(modifiedOnlyBtn);

        // Top Right: Batch Tweak Mode Toggle Button
        this.addRenderableWidget(Button.builder(Component.translatable("iron_spell_tweaker.button.batch_tweak").withStyle(ChatFormatting.AQUA), b -> {
            this.batchMode = !this.batchMode;
            if (this.batchMode) {
                this.batchTargetSchool = selectedSchoolFilter != null ? selectedSchoolFilter : (selectedSpell != null ? selectedSpell.getSchool() : null);
                if (this.batchSchoolBtn != null) {
                    for (SchoolFilterOption opt : this.batchSchoolOptions) {
                        if (Objects.equals(opt.school(), this.batchTargetSchool)) {
                            this.batchSchoolBtn.setValue(opt);
                            break;
                        }
                    }
                }
            }
            updateWidgetStates();
        }).bounds(rightX + rightWidth - 110, 18, 110, 18).build());

        // Build Right Panels (Normal Tweaks & Batch Mode)
        buildTweakPanel();
        buildBatchPanel();

        // Bottom Bar buttons
        int bottomY = this.height - 24;
        this.addRenderableWidget(Button.builder(Component.translatable("iron_spell_tweaker.button.open_folder"), b -> {
            SpellConfigIO.openConfigFolder();
        }).bounds(rightX, bottomY, 120, 20).build());

        this.resetAllButton = Button.builder(Component.translatable("iron_spell_tweaker.button.reset_all").withStyle(ChatFormatting.RED), b -> {
            openConfirmModal(
                    Component.translatable("iron_spell_tweaker.modal.reset_all_title").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                    Component.translatable("iron_spell_tweaker.modal.reset_all_msg"),
                    Component.translatable("iron_spell_tweaker.modal.reset_all_sub").withStyle(ChatFormatting.GOLD),
                    0xFFFF3333,
                    Component.translatable("iron_spell_tweaker.button.reset_all").withStyle(ChatFormatting.RED),
                    () -> {
                        int count = SpellConfigIO.resetAllSpellConfigs(SpellDiscoveryService.getAllSpells());
                        refreshTweakWidgets();
                        updateFilter();
                        showStatus(String.format(Component.translatable("iron_spell_tweaker.toast.reset_all_applied").getString(), count), true);
                    }
            );
        }).bounds(rightX + 124, bottomY, 100, 20).build();
        this.addRenderableWidget(this.resetAllButton);

        this.addRenderableWidget(Button.builder(CommonComponents.GUI_BACK, b -> {
            this.onClose();
        }).bounds(this.width - 96, bottomY, 80, 20).build());

        // Initialize Modal Buttons (managed explicitly to avoid tab/click bleed-through)
        int modalW = 360;
        int modalH = 140;
        int mX = (this.width - modalW) / 2;
        int mY = (this.height - modalH) / 2;
        int btnW = 110;
        int btnY = mY + modalH - 28;

        this.modalConfirmBtn = Button.builder(Component.translatable("iron_spell_tweaker.modal.confirm"), b -> executeModalConfirm())
                .bounds(mX + (modalW / 2) - btnW - 6, btnY, btnW, 20).build();
        this.modalConfirmBtn.visible = false;
        this.modalConfirmBtn.active = false;

        this.modalCancelBtn = Button.builder(Component.translatable("iron_spell_tweaker.modal.cancel"), b -> closeConfirmModal())
                .bounds(mX + (modalW / 2) + 6, btnY, btnW, 20).build();
        this.modalCancelBtn.visible = false;
        this.modalCancelBtn.active = false;
    }

    private void buildTweakPanel() {
        int controlX = rightX + 12;
        int controlWidth = rightWidth - 24;
        int halfWidth = (controlWidth - 6) / 2;

        // Sliders start below header and base stats lore box
        int tweakY = listTop + 72;

        // 1. Power Multiplier
        double currentPower = selectedSpell != null ? selectedSpell.getPowerMultiplier() : 1.0;
        this.powerSlider = new CustomSlider(controlX, tweakY, controlWidth, 18, currentPower, 0.1, 10.0, 0.1,
                this::getPowerText,
                val -> { if (selectedSpell != null) selectedSpell.setPowerMultiplier(val); }
        );
        this.addRenderableWidget(this.powerSlider);
        tweakY += 21;

        // 2. Mana Multiplier
        double currentMana = selectedSpell != null ? selectedSpell.getManaMultiplier() : 1.0;
        this.manaSlider = new CustomSlider(controlX, tweakY, controlWidth, 18, currentMana, 0.1, 10.0, 0.1,
                this::getManaText,
                val -> { if (selectedSpell != null) selectedSpell.setManaMultiplier(val); }
        );
        this.addRenderableWidget(this.manaSlider);
        tweakY += 21;

        // 3. Cooldown
        double currentCd = selectedSpell != null ? selectedSpell.getCooldownInSeconds() : 10.0;
        this.cooldownSlider = new CustomSlider(controlX, tweakY, controlWidth, 18, currentCd, 0.0, 120.0, 0.5,
                this::getCooldownText,
                val -> { if (selectedSpell != null) selectedSpell.setCooldownInSeconds(val); }
        );
        this.addRenderableWidget(this.cooldownSlider);
        tweakY += 21;

        // 4. Max Level
        int currentLvl = selectedSpell != null ? selectedSpell.getMaxLevel() : 10;
        this.maxLevelSlider = new CustomSlider(controlX, tweakY, controlWidth, 18, currentLvl, 1, 10, 1,
                val -> getMaxLevelText(val.intValue()),
                val -> { if (selectedSpell != null) selectedSpell.setMaxLevel(val.intValue()); }
        );
        this.addRenderableWidget(this.maxLevelSlider);
        tweakY += 21;

        // 5. Min Rarity & School Reassignment (Side by Side)
        SpellRarity currentRar = selectedSpell != null ? selectedSpell.getMinRarity() : SpellRarity.COMMON;
        this.rarityButton = CycleButton.<SpellRarity>builder(r -> Component.literal(r.name()).withStyle(getRarityFormatting(r)))
                .withValues(SpellRarity.values())
                .withInitialValue(currentRar)
                .create(controlX, tweakY, halfWidth, 18, Component.translatable("iron_spell_tweaker.min_rarity"), (btn, val) -> {
                    if (selectedSpell != null) {
                        selectedSpell.setMinRarity(val);
                    }
                });
        this.addRenderableWidget(this.rarityButton);

        List<SchoolType> schools = SpellDiscoveryService.getAllSchools();
        SchoolType currentSchool = selectedSpell != null && selectedSpell.getSchool() != null ? selectedSpell.getSchool() : (!schools.isEmpty() ? schools.get(0) : null);
        this.schoolReassignButton = CycleButton.<SchoolType>builder(s -> Component.literal(s.getDisplayName().getString()))
                .withValues(schools)
                .withInitialValue(currentSchool)
                .create(controlX + halfWidth + 6, tweakY, halfWidth, 18, Component.translatable("iron_spell_tweaker.school_reassign"), (btn, val) -> {
                    if (selectedSpell != null) {
                        selectedSpell.setSchool(val);
                    }
                });
        this.addRenderableWidget(this.schoolReassignButton);
        tweakY += 21;

        // 6. Enabled & Allow Crafting Toggles (Side by Side)
        boolean currentEn = selectedSpell == null || selectedSpell.isEnabled();
        this.enabledButton = CycleButton.onOffBuilder(currentEn)
                .create(controlX, tweakY, halfWidth, 18, Component.translatable("iron_spell_tweaker.enabled"), (btn, val) -> {
                    if (selectedSpell != null) {
                        selectedSpell.setEnabled(val);
                    }
                });
        this.addRenderableWidget(this.enabledButton);

        boolean currentCr = selectedSpell == null || selectedSpell.isAllowCrafting();
        this.craftingButton = CycleButton.onOffBuilder(currentCr)
                .create(controlX + halfWidth + 6, tweakY, halfWidth, 18, Component.translatable("iron_spell_tweaker.allow_crafting"), (btn, val) -> {
                    if (selectedSpell != null) {
                        selectedSpell.setAllowCrafting(val);
                    }
                });
        this.addRenderableWidget(this.craftingButton);
        tweakY += 24;

        // 7. Action Buttons (Save & Apply / Reset / Delete JSON)
        int saveW = (int) (controlWidth * 0.44);
        int auxW = (controlWidth - saveW - 8) / 2;

        this.saveButton = Button.builder(Component.translatable("iron_spell_tweaker.button.save").withStyle(ChatFormatting.GREEN), b -> {
            if (selectedSpell != null) {
                boolean ok = SpellConfigIO.saveSpellConfig(selectedSpell);
                showStatus(ok ? Component.translatable("iron_spell_tweaker.toast.saved").getString() : "Save failed!", ok);
                updateFilter();
            }
        }).bounds(controlX, tweakY, saveW, 20).build();
        this.addRenderableWidget(this.saveButton);

        this.resetButton = Button.builder(Component.translatable("iron_spell_tweaker.button.reset").withStyle(ChatFormatting.YELLOW), b -> {
            if (selectedSpell != null) {
                selectedSpell.resetToDefaults();
                refreshTweakWidgets();
                showStatus(Component.translatable("iron_spell_tweaker.button.reset").getString(), true);
            }
        }).bounds(controlX + saveW + 4, tweakY, auxW, 20).build();
        this.addRenderableWidget(this.resetButton);

        this.deleteButton = Button.builder(Component.translatable("iron_spell_tweaker.button.delete_config").withStyle(ChatFormatting.RED), b -> {
            if (selectedSpell != null) {
                String spellName = selectedSpell.getDisplayName().getString();
                openConfirmModal(
                        Component.translatable("iron_spell_tweaker.modal.delete_title").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                        Component.translatable("iron_spell_tweaker.modal.delete_msg", spellName),
                        Component.translatable("iron_spell_tweaker.modal.delete_sub").withStyle(ChatFormatting.GRAY),
                        0xFFFF5555,
                        Component.translatable("iron_spell_tweaker.button.delete_config").withStyle(ChatFormatting.RED),
                        () -> {
                            boolean ok = SpellConfigIO.deleteSpellConfigFile(selectedSpell);
                            refreshTweakWidgets();
                            showStatus(Component.translatable("iron_spell_tweaker.toast.deleted").getString(), ok);
                            updateFilter();
                        }
                );
            }
        }).bounds(controlX + saveW + auxW + 8, tweakY, auxW, 20).build();
        this.addRenderableWidget(this.deleteButton);
    }

    private void buildBatchPanel() {
        int controlX = rightX + 16;
        int controlWidth = rightWidth - 32;
        int halfWidth = (controlWidth - 6) / 2;
        int quarterWidth = (controlWidth - 9) / 4;
        int bY = listTop + 30;

        // 1. Target School Selector
        this.batchSchoolOptions.clear();
        this.batchSchoolOptions.add(SchoolFilterOption.ALL);
        SchoolFilterOption initialSchool = SchoolFilterOption.ALL;
        for (SchoolType s : SpellDiscoveryService.getAllSchools()) {
            if (s != null) {
                SchoolFilterOption opt = new SchoolFilterOption(s, Component.literal(s.getDisplayName().getString()));
                this.batchSchoolOptions.add(opt);
                if (Objects.equals(s, this.batchTargetSchool)) {
                    initialSchool = opt;
                }
            }
        }

        this.batchSchoolBtn = CycleButton.<SchoolFilterOption>builder(SchoolFilterOption::name)
                .withValues(this.batchSchoolOptions)
                .withInitialValue(initialSchool)
                .create(controlX, bY, controlWidth, 18, Component.translatable("iron_spell_tweaker.batch.target_school"), (btn, val) -> {
                    this.batchTargetSchool = val.school();
                });
        this.addRenderableWidget(this.batchSchoolBtn);
        bY += 21;

        // 2. Power Adjustment Slider (-50% to +200%, step 5%)
        this.batchPowerSlider = new CustomSlider(controlX, bY, controlWidth, 18, this.batchPowerPercent, -50.0, 200.0, 5.0,
                this::getBatchPowerText,
                val -> this.batchPowerPercent = val
        );
        this.addRenderableWidget(this.batchPowerSlider);
        bY += 21;

        // 3. Cooldown Adjustment Slider (-80% to +100%, step 5%)
        this.batchCooldownSlider = new CustomSlider(controlX, bY, controlWidth, 18, this.batchCooldownPercent, -80.0, 100.0, 5.0,
                this::getBatchCooldownText,
                val -> this.batchCooldownPercent = val
        );
        this.addRenderableWidget(this.batchCooldownSlider);
        bY += 21;

        // 4. Mana Cost Adjustment Slider (-80% to +100%, step 5%)
        this.batchManaSlider = new CustomSlider(controlX, bY, controlWidth, 18, this.batchManaPercent, -80.0, 100.0, 5.0,
                this::getBatchManaText,
                val -> this.batchManaPercent = val
        );
        this.addRenderableWidget(this.batchManaSlider);
        bY += 21;

        // 5. Quick Preset Shortcut Chips (P+20%, P+50%, CD-20%, CD-50%)
        this.batchPresetP20 = Button.builder(Component.literal("+20% Pwr"), b -> {
            this.batchPowerPercent = 20.0;
            if (this.batchPowerSlider != null) this.batchPowerSlider.setSliderValue(20.0);
        }).bounds(controlX, bY, quarterWidth, 18).build();
        this.addRenderableWidget(this.batchPresetP20);

        this.batchPresetP50 = Button.builder(Component.literal("+50% Pwr"), b -> {
            this.batchPowerPercent = 50.0;
            if (this.batchPowerSlider != null) this.batchPowerSlider.setSliderValue(50.0);
        }).bounds(controlX + quarterWidth + 3, bY, quarterWidth, 18).build();
        this.addRenderableWidget(this.batchPresetP50);

        this.batchPresetCd20 = Button.builder(Component.literal("-20% CD"), b -> {
            this.batchCooldownPercent = -20.0;
            if (this.batchCooldownSlider != null) this.batchCooldownSlider.setSliderValue(-20.0);
        }).bounds(controlX + (quarterWidth + 3) * 2, bY, quarterWidth, 18).build();
        this.addRenderableWidget(this.batchPresetCd20);

        this.batchPresetCd50 = Button.builder(Component.literal("-50% CD"), b -> {
            this.batchCooldownPercent = -50.0;
            if (this.batchCooldownSlider != null) this.batchCooldownSlider.setSliderValue(-50.0);
        }).bounds(controlX + (quarterWidth + 3) * 3, bY, quarterWidth, 18).build();
        this.addRenderableWidget(this.batchPresetCd50);
        bY += 21;

        // 6. Enable / Disable All State Toggles
        this.batchEnableBtn = Button.builder(Component.translatable("iron_spell_tweaker.batch.enable_all").withStyle(ChatFormatting.GREEN), b -> {
            int count = SpellConfigIO.batchTweakSchool(this.batchTargetSchool, 0, 0, 0, true, SpellDiscoveryService.getAllSpells());
            showStatus(String.format(Component.translatable("iron_spell_tweaker.toast.batch_applied").getString(), count), true);
            refreshTweakWidgets();
            updateFilter();
        }).bounds(controlX, bY, halfWidth, 18).build();
        this.addRenderableWidget(this.batchEnableBtn);

        this.batchDisableBtn = Button.builder(Component.translatable("iron_spell_tweaker.batch.disable_all").withStyle(ChatFormatting.RED), b -> {
            SchoolType target = this.batchTargetSchool;
            String schoolName = target != null ? target.getDisplayName().getString() : Component.translatable("iron_spell_tweaker.school.all").getString();
            long affectedCount = SpellDiscoveryService.getAllSpells().stream()
                    .filter(s -> target == null || target.equals(s.getSchool()))
                    .count();

            openConfirmModal(
                    Component.translatable("iron_spell_tweaker.modal.disable_all_title").withStyle(ChatFormatting.RED, ChatFormatting.BOLD),
                    Component.translatable("iron_spell_tweaker.modal.disable_all_msg", affectedCount, schoolName),
                    Component.translatable("iron_spell_tweaker.modal.disable_all_sub").withStyle(ChatFormatting.GOLD),
                    0xFFFF5555,
                    Component.translatable("iron_spell_tweaker.batch.disable_all").withStyle(ChatFormatting.RED),
                    () -> {
                        int count = SpellConfigIO.batchTweakSchool(this.batchTargetSchool, 0, 0, 0, false, SpellDiscoveryService.getAllSpells());
                        showStatus(String.format(Component.translatable("iron_spell_tweaker.toast.batch_applied").getString(), count), true);
                        refreshTweakWidgets();
                        updateFilter();
                    }
            );
        }).bounds(controlX + halfWidth + 6, bY, halfWidth, 18).build();
        this.addRenderableWidget(this.batchDisableBtn);
        bY += 24;

        // 7. Apply Adjustments Button (Big & Prominent)
        this.batchApplyBtn = Button.builder(Component.translatable("iron_spell_tweaker.batch.apply").withStyle(ChatFormatting.AQUA), b -> {
            SchoolType target = this.batchTargetSchool;
            String schoolName = target != null ? target.getDisplayName().getString() : Component.translatable("iron_spell_tweaker.school.all").getString();
            long affectedCount = SpellDiscoveryService.getAllSpells().stream()
                    .filter(s -> target == null || target.equals(s.getSchool()))
                    .count();

            openConfirmModal(
                    Component.translatable("iron_spell_tweaker.modal.batch_title").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD),
                    Component.translatable("iron_spell_tweaker.modal.batch_msg", affectedCount, schoolName),
                    Component.translatable("iron_spell_tweaker.modal.batch_sub").withStyle(ChatFormatting.GRAY),
                    0xFF33CCFF,
                    Component.translatable("iron_spell_tweaker.modal.confirm").withStyle(ChatFormatting.AQUA),
                    () -> {
                        double pFactor = 1.0 + (this.batchPowerPercent / 100.0);
                        double mFactor = 1.0 + (this.batchManaPercent / 100.0);
                        double cdFactor = 1.0 + (this.batchCooldownPercent / 100.0);

                        int count = SpellConfigIO.batchTweakSchool(this.batchTargetSchool, pFactor, mFactor, cdFactor, null, SpellDiscoveryService.getAllSpells());
                        showStatus(String.format(Component.translatable("iron_spell_tweaker.toast.batch_applied").getString(), count), true);
                        refreshTweakWidgets();
                        updateFilter();
                    }
            );
        }).bounds(controlX, bY, controlWidth, 20).build();
        this.addRenderableWidget(this.batchApplyBtn);
        bY += 23;

        // 8. Reset Sliders (0%) & Done Buttons
        this.batchResetBtn = Button.builder(Component.translatable("iron_spell_tweaker.batch.reset_sliders").withStyle(ChatFormatting.YELLOW), b -> {
            this.batchPowerPercent = 0.0;
            this.batchCooldownPercent = 0.0;
            this.batchManaPercent = 0.0;
            if (this.batchPowerSlider != null) this.batchPowerSlider.setSliderValue(0.0);
            if (this.batchCooldownSlider != null) this.batchCooldownSlider.setSliderValue(0.0);
            if (this.batchManaSlider != null) this.batchManaSlider.setSliderValue(0.0);
        }).bounds(controlX, bY, halfWidth, 18).build();
        this.addRenderableWidget(this.batchResetBtn);

        this.closeBatchBtn = Button.builder(CommonComponents.GUI_DONE, b -> {
            this.batchMode = false;
            updateWidgetStates();
        }).bounds(controlX + halfWidth + 6, bY, halfWidth, 18).build();
        this.addRenderableWidget(this.closeBatchBtn);

        updateWidgetStates();
    }

    private void updateWidgetStates() {
        boolean hasSpell = selectedSpell != null;
        boolean showTweaks = !batchMode && !showConfirmModal;
        boolean showBatch = batchMode && !showConfirmModal;

        // Normal tweak widgets visibility
        if (powerSlider != null) { powerSlider.visible = showTweaks; powerSlider.active = hasSpell; }
        if (manaSlider != null) { manaSlider.visible = showTweaks; manaSlider.active = hasSpell; }
        if (cooldownSlider != null) { cooldownSlider.visible = showTweaks; cooldownSlider.active = hasSpell; }
        if (maxLevelSlider != null) { maxLevelSlider.visible = showTweaks; maxLevelSlider.active = hasSpell; }
        if (rarityButton != null) { rarityButton.visible = showTweaks; rarityButton.active = hasSpell; }
        if (schoolReassignButton != null) { schoolReassignButton.visible = showTweaks; schoolReassignButton.active = hasSpell; }
        if (enabledButton != null) { enabledButton.visible = showTweaks; enabledButton.active = hasSpell; }
        if (craftingButton != null) { craftingButton.visible = showTweaks; craftingButton.active = hasSpell; }
        if (saveButton != null) { saveButton.visible = showTweaks; saveButton.active = hasSpell; }
        if (resetButton != null) { resetButton.visible = showTweaks; resetButton.active = hasSpell; }
        if (deleteButton != null) { deleteButton.visible = showTweaks; deleteButton.active = hasSpell && selectedSpell.hasCustomConfigFile(); }

        // Batch widgets visibility
        if (batchSchoolBtn != null) batchSchoolBtn.visible = showBatch;
        if (batchPowerSlider != null) batchPowerSlider.visible = showBatch;
        if (batchCooldownSlider != null) batchCooldownSlider.visible = showBatch;
        if (batchManaSlider != null) batchManaSlider.visible = showBatch;
        if (batchPresetP20 != null) batchPresetP20.visible = showBatch;
        if (batchPresetP50 != null) batchPresetP50.visible = showBatch;
        if (batchPresetCd20 != null) batchPresetCd20.visible = showBatch;
        if (batchPresetCd50 != null) batchPresetCd50.visible = showBatch;
        if (batchEnableBtn != null) batchEnableBtn.visible = showBatch;
        if (batchDisableBtn != null) batchDisableBtn.visible = showBatch;
        if (batchApplyBtn != null) batchApplyBtn.visible = showBatch;
        if (batchResetBtn != null) batchResetBtn.visible = showBatch;
        if (closeBatchBtn != null) closeBatchBtn.visible = showBatch;
    }

    private void refreshTweakWidgets() {
        if (selectedSpell == null) return;
        if (powerSlider != null) powerSlider.setSliderValue(selectedSpell.getPowerMultiplier());
        if (manaSlider != null) manaSlider.setSliderValue(selectedSpell.getManaMultiplier());
        if (cooldownSlider != null) cooldownSlider.setSliderValue(selectedSpell.getCooldownInSeconds());
        if (maxLevelSlider != null) maxLevelSlider.setSliderValue(selectedSpell.getMaxLevel());
        if (rarityButton != null) rarityButton.setValue(selectedSpell.getMinRarity());
        if (schoolReassignButton != null && selectedSpell.getSchool() != null) schoolReassignButton.setValue(selectedSpell.getSchool());
        if (enabledButton != null) enabledButton.setValue(selectedSpell.isEnabled());
        if (craftingButton != null) craftingButton.setValue(selectedSpell.isAllowCrafting());
        if (deleteButton != null) deleteButton.active = selectedSpell.hasCustomConfigFile();
    }

    private void updateFilter() {
        filteredSpells.clear();
        for (SpellConfigData spell : SpellDiscoveryService.getAllSpells()) {
            if (selectedSchoolFilter != null && !selectedSchoolFilter.equals(spell.getSchool())) {
                continue;
            }
            if (!"all".equals(selectedModFilter) && !selectedModFilter.equalsIgnoreCase(spell.getModId())) {
                continue;
            }
            if (onlyModifiedFilter && !spell.hasCustomConfigFile() && !spell.isModified()) {
                continue;
            }
            if (!currentSearch.isEmpty()) {
                String dName = spell.getDisplayName().getString().toLowerCase(Locale.ROOT);
                String sId = spell.getSpellId().toString().toLowerCase(Locale.ROOT);
                String mName = spell.getModName().toLowerCase(Locale.ROOT);
                if (!dName.contains(currentSearch) && !sId.contains(currentSearch) && !mName.contains(currentSearch)) {
                    continue;
                }
            }
            filteredSpells.add(spell);
        }

        if (selectedSpell != null && !filteredSpells.contains(selectedSpell)) {
            selectedSpell = filteredSpells.isEmpty() ? null : filteredSpells.get(0);
            refreshTweakWidgets();
            updateWidgetStates();
        }
    }

    private void showStatus(String message, boolean success) {
        this.statusMessage = message;
        this.statusSuccess = success;
        this.statusTimer = 60;
    }

    @Override
    public void tick() {
        super.tick();
        if (statusTimer > 0) {
            statusTimer--;
            if (statusTimer == 0) {
                statusMessage = null;
            }
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        int effectiveMouseX = showConfirmModal ? -999 : mouseX;
        int effectiveMouseY = showConfirmModal ? -999 : mouseY;
        super.render(guiGraphics, effectiveMouseX, effectiveMouseY, partialTick);

        // Header Screen Title
        guiGraphics.drawString(this.font, this.title, 16, 6, 0xFFFFFF, true);

        // Spell Count Badge
        String countStr = String.format("(%d spells)", filteredSpells.size());
        guiGraphics.drawString(this.font, countStr, 20 + this.font.width(this.title), 6, 0xAAAAAA, false);

        // Draw Left Panel Background
        guiGraphics.fill(16, listTop, 16 + leftWidth, listBottom, 0x55000000);
        guiGraphics.renderOutline(16, listTop, leftWidth, listHeight, 0x44FFFFFF);

        // Draw Right Panel Background
        guiGraphics.fill(rightX, listTop, rightX + rightWidth, listBottom, 0x55000000);
        guiGraphics.renderOutline(rightX, listTop, rightWidth, listHeight, 0x44FFFFFF);

        // Render Left Spell List (with Scissor clipping)
        guiGraphics.enableScissor(16, listTop, 16 + leftWidth, listBottom);

        int totalListHeight = filteredSpells.size() * ENTRY_HEIGHT;
        int maxScroll = Math.max(0, totalListHeight - listHeight);
        scrollOffset = Math.max(0, Math.min(scrollOffset, maxScroll));

        int yOffset = (int) (listTop - scrollOffset);
        for (int i = 0; i < filteredSpells.size(); i++) {
            SpellConfigData item = filteredSpells.get(i);
            int entryY = yOffset + i * ENTRY_HEIGHT;

            if (entryY + ENTRY_HEIGHT >= listTop && entryY <= listBottom) {
                boolean isHovered = mouseX >= 18 && mouseX <= 14 + leftWidth && mouseY >= entryY && mouseY < entryY + ENTRY_HEIGHT;
                boolean isSelected = item == selectedSpell;
                int schoolColor = getSchoolColor(item.getSchool());

                // Entry background with school color tint
                if (isSelected) {
                    guiGraphics.fill(18, entryY + 1, 14 + leftWidth, entryY + ENTRY_HEIGHT - 1, 0x33000000 | (schoolColor & 0x00FFFFFF));
                    guiGraphics.renderOutline(18, entryY + 1, leftWidth - 4, ENTRY_HEIGHT - 2, 0xFF000000 | schoolColor);
                } else if (isHovered) {
                    guiGraphics.fill(18, entryY + 1, 14 + leftWidth, entryY + ENTRY_HEIGHT - 1, 0x22FFFFFF);
                    guiGraphics.renderOutline(18, entryY + 1, leftWidth - 4, ENTRY_HEIGHT - 2, 0xAA000000 | (schoolColor & 0x00FFFFFF));
                } else {
                    guiGraphics.renderOutline(18, entryY + 1, leftWidth - 4, ENTRY_HEIGHT - 2, 0x55000000 | (schoolColor & 0x00FFFFFF));
                }

                // 2px solid accent stripe on the left of each card
                guiGraphics.fill(18, entryY + 1, 21, entryY + ENTRY_HEIGHT - 1, 0xFF000000 | schoolColor);

                // Render Spell Icon
                renderSpellIcon(guiGraphics, item.getIconResource(), 24, entryY + 6, 20);

                // Render Spell Name
                int textColor = isSelected ? 0xFFFFAA : 0xFFFFFF;
                String dName = item.getDisplayName().getString();
                guiGraphics.drawString(this.font, dName, 48, entryY + 6, textColor, false);

                // Render Mod & School (School badge colored!)
                String modStr = String.format("[%s] ", item.getModName());
                int modW = this.font.width(modStr);
                guiGraphics.drawString(this.font, modStr, 48, entryY + 18, 0x888888, false);

                if (item.getSchool() != null) {
                    guiGraphics.drawString(this.font, item.getSchool().getDisplayName().getString(), 48 + modW, entryY + 18, schoolColor, false);
                }

                // Badges
                if (!item.isEnabled()) {
                    guiGraphics.drawString(this.font, "[DISABLED]", 14 + leftWidth - 58, entryY + 12, 0xFF5555, false);
                } else if (item.hasCustomConfigFile()) {
                    guiGraphics.drawString(this.font, "[EDITED]", 14 + leftWidth - 48, entryY + 12, 0xFFAA00, false);
                } else if (item.isModified()) {
                    guiGraphics.drawString(this.font, "[UNSAVED]", 14 + leftWidth - 54, entryY + 12, 0xFFFF55, false);
                }
            }
        }

        if (filteredSpells.isEmpty()) {
            String emptyFilterMsg = Component.translatable("iron_spell_tweaker.filter.empty").getString();
            guiGraphics.drawCenteredString(this.font, emptyFilterMsg, 16 + leftWidth / 2, listTop + listHeight / 2 - 4, 0x888888);
        }

        guiGraphics.disableScissor();

        // Draw Left Scrollbar
        if (maxScroll > 0) {
            int barHeight = Math.max(16, (int) ((float) listHeight / totalListHeight * listHeight));
            int barY = listTop + (int) ((float) scrollOffset / maxScroll * (listHeight - barHeight));
            guiGraphics.fill(14 + leftWidth - 3, barY, 14 + leftWidth, barY + barHeight, 0x88FFFFFF);
        }

        // Render Right Panel Content
        if (batchMode) {
            // Batch Mode Header
            SchoolType target = batchTargetSchool;
            String schoolName = target != null ? target.getDisplayName().getString() : Component.translatable("iron_spell_tweaker.school.all").getString();
            int sColor = getSchoolColor(target);

            long affectedCount = SpellDiscoveryService.getAllSpells().stream()
                    .filter(s -> target == null || target.equals(s.getSchool()))
                    .count();

            guiGraphics.drawString(this.font, "⚡ " + Component.translatable("iron_spell_tweaker.batch.title").getString() + ": " + schoolName, rightX + 16, listTop + 6, sColor, true);
            String countLabel = String.format(Locale.ROOT, "(%d %s)", affectedCount, Component.translatable("iron_spell_tweaker.batch.spells_count").getString());
            guiGraphics.drawString(this.font, countLabel, rightX + 16, listTop + 17, 0xAAAAAA, false);
        } else if (selectedSpell != null) {
            // Normal Tweak Mode Header
            int headerY = listTop + 6;
            renderSpellIcon(guiGraphics, selectedSpell.getIconResource(), rightX + 12, headerY + 2, 28);

            String titleStr = selectedSpell.getDisplayName().getString();
            guiGraphics.drawString(this.font, titleStr, rightX + 46, headerY + 2, 0xFFFFAA, true);

            String idStr = selectedSpell.getSpellId().toString();
            guiGraphics.drawString(this.font, idStr, rightX + 46, headerY + 14, 0x888888, false);

            // Base Stats Row (Badges)
            int statsY = headerY + 28;
            String typeStr = "Type: " + formatCastType(selectedSpell.getCastType());
            String timeStr = String.format(Locale.ROOT, "Time: %.1fs", selectedSpell.getBaseCastTime());
            String manaStr = String.format(Locale.ROOT, "Mana: %d", selectedSpell.getBaseManaCost());
            String statsLine;
            if (selectedSpell.getBaseSpellPower() > 0) {
                String powerStr = String.format(Locale.ROOT, "Power: %.1f", selectedSpell.getBaseSpellPower());
                statsLine = String.format("%s  •  %s  •  %s  •  %s", typeStr, timeStr, manaStr, powerStr);
            } else {
                statsLine = String.format("%s  •  %s  •  %s", typeStr, timeStr, manaStr);
            }
            guiGraphics.drawString(this.font, statsLine, rightX + 12, statsY, 0x55FFFF, false);

            // Guide Lore Description (multi-line word wrapped)
            int loreY = statsY + 12;
            if (!selectedSpell.getDescriptionLore().getString().isEmpty()) {
                List<FormattedCharSequence> lines = this.font.split(selectedSpell.getDescriptionLore(), rightWidth - 28);
                int maxLines = Math.min(3, lines.size());
                for (int lineIdx = 0; lineIdx < maxLines; lineIdx++) {
                    guiGraphics.drawString(this.font, lines.get(lineIdx), rightX + 12, loreY + (lineIdx * 10), 0xCCCCCC, false);
                }
            }

            // Divider line separating header from sliders (colored by school!)
            int sColor = getSchoolColor(selectedSpell.getSchool());
            guiGraphics.fill(rightX + 10, listTop + 65, rightX + rightWidth - 10, listTop + 66, 0x66000000 | (sColor & 0x00FFFFFF));
        } else {
            String emptyMsg = Component.translatable("iron_spell_tweaker.panel.no_spell_selected").getString();
            guiGraphics.drawCenteredString(this.font, emptyMsg, rightX + rightWidth / 2, listTop + listHeight / 2, 0x888888);
        }

        // Status Toast Notification
        if (statusMessage != null) {
            int toastColor = statusSuccess ? 0xAA008800 : 0xAA880000;
            int textW = this.font.width(statusMessage);
            int tX = this.width / 2 - textW / 2 - 10;
            int tY = this.height - 48;
            guiGraphics.fill(tX, tY, tX + textW + 20, tY + 18, toastColor);
            guiGraphics.renderOutline(tX, tY, textW + 20, 18, 0xFFFFFFFF);
            guiGraphics.drawString(this.font, statusMessage, tX + 10, tY + 5, 0xFFFFFF, true);
        }

        // Confirmation Modal Dialog (rendered on top of everything including toasts)
        if (showConfirmModal) {
            // Flush all previous rendering (including background widget text) so it is behind the modal
            guiGraphics.flush();

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0, 0, 400.0f);

            // 1. Darken full screen scrim
            guiGraphics.fill(0, 0, this.width, this.height, 0xCC000000);

            // 2. Modal Box dimensions
            int modalW = 360;
            int modalH = 140;
            int mX = (this.width - modalW) / 2;
            int mY = (this.height - modalH) / 2;

            // Modal Background (100% Solid dark panel with layered borders)
            guiGraphics.fill(mX, mY, mX + modalW, mY + modalH, 0xFF121216);
            guiGraphics.renderOutline(mX, mY, modalW, modalH, 0xFF666677);
            guiGraphics.renderOutline(mX + 1, mY + 1, modalW - 2, modalH - 2, 0xFF222228);

            // Top accent bar
            guiGraphics.fill(mX + 2, mY + 2, mX + modalW - 2, mY + 5, modalAccentColor);

            // Modal Title (centered)
            guiGraphics.drawCenteredString(this.font, this.modalTitle, mX + modalW / 2, mY + 14, 0xFFFFFF);

            // Divider line under title
            guiGraphics.fill(mX + 16, mY + 28, mX + modalW - 16, mY + 29, 0x44FFFFFF);

            // Message text (word wrapped)
            List<FormattedCharSequence> msgLines = this.font.split(this.modalMessage, modalW - 32);
            int textY = mY + 36;
            for (int i = 0; i < Math.min(3, msgLines.size()); i++) {
                int lineW = this.font.width(msgLines.get(i));
                guiGraphics.drawString(this.font, msgLines.get(i), mX + (modalW - lineW) / 2, textY + (i * 12), 0xEEEEEE, false);
            }

            // Sub-message text (warning / guidance, word wrapped)
            if (!this.modalSubMessage.getString().isEmpty()) {
                List<FormattedCharSequence> subLines = this.font.split(this.modalSubMessage, modalW - 32);
                int subY = textY + (Math.min(3, msgLines.size()) * 12) + 6;
                for (int i = 0; i < Math.min(2, subLines.size()); i++) {
                    int lineW = this.font.width(subLines.get(i));
                    guiGraphics.drawString(this.font, subLines.get(i), mX + (modalW - lineW) / 2, subY + (i * 11), 0xAAAAAA, false);
                }
            }

            // Render Modal Buttons with active mouse coordinates
            if (modalConfirmBtn != null) {
                modalConfirmBtn.render(guiGraphics, mouseX, mouseY, partialTick);
            }
            if (modalCancelBtn != null) {
                modalCancelBtn.render(guiGraphics, mouseX, mouseY, partialTick);
            }

            // Flush modal rendering and pop pose
            guiGraphics.flush();
            guiGraphics.pose().popPose();
        }
    }

    private void renderSpellIcon(GuiGraphics guiGraphics, ResourceLocation icon, int x, int y, int size) {
        if (icon != null) {
            try {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                guiGraphics.blit(icon, x, y, 0, 0, size, size, size, size);
                return;
            } catch (Throwable ignored) {}
        }
        guiGraphics.fill(x, y, x + size, y + size, 0xAA442266);
        guiGraphics.renderOutline(x, y, size, size, 0xFFAAAAFF);
    }

    public void openConfirmModal(Component title, Component message, Component subMessage, int accentColor, Component confirmText, Runnable onConfirm) {
        this.modalTitle = title;
        this.modalMessage = message;
        this.modalSubMessage = subMessage;
        this.modalAccentColor = accentColor;
        this.onConfirmAction = onConfirm;
        this.showConfirmModal = true;
        updateWidgetStates();

        int modalW = 360;
        int modalH = 140;
        int mX = (this.width - modalW) / 2;
        int mY = (this.height - modalH) / 2;
        int btnW = 110;
        int btnY = mY + modalH - 28;

        if (this.modalConfirmBtn != null) {
            this.modalConfirmBtn.setX(mX + (modalW / 2) - btnW - 6);
            this.modalConfirmBtn.setY(btnY);
            this.modalConfirmBtn.setMessage(confirmText != null ? confirmText : Component.translatable("iron_spell_tweaker.modal.confirm"));
            this.modalConfirmBtn.visible = true;
            this.modalConfirmBtn.active = true;
        }
        if (this.modalCancelBtn != null) {
            this.modalCancelBtn.setX(mX + (modalW / 2) + 6);
            this.modalCancelBtn.setY(btnY);
            this.modalCancelBtn.visible = true;
            this.modalCancelBtn.active = true;
        }
    }

    public void closeConfirmModal() {
        this.showConfirmModal = false;
        this.onConfirmAction = null;
        if (this.modalConfirmBtn != null) {
            this.modalConfirmBtn.visible = false;
            this.modalConfirmBtn.active = false;
        }
        if (this.modalCancelBtn != null) {
            this.modalCancelBtn.visible = false;
            this.modalCancelBtn.active = false;
        }
        updateWidgetStates();
    }

    private void executeModalConfirm() {
        Runnable action = this.onConfirmAction;
        closeConfirmModal();
        if (action != null) {
            action.run();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (showConfirmModal) {
            if (modalConfirmBtn != null && modalConfirmBtn.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            if (modalCancelBtn != null && modalCancelBtn.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
            return true;
        }

        if (mouseX >= 16 && mouseX <= 16 + leftWidth && mouseY >= listTop && mouseY <= listBottom) {
            int clickedIndex = (int) ((mouseY - listTop + scrollOffset) / ENTRY_HEIGHT);
            if (clickedIndex >= 0 && clickedIndex < filteredSpells.size()) {
                this.selectedSpell = filteredSpells.get(clickedIndex);
                refreshTweakWidgets();
                updateWidgetStates();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (showConfirmModal) {
            return true;
        }

        if (mouseX >= 16 && mouseX <= 16 + leftWidth && mouseY >= listTop && mouseY <= listBottom) {
            this.scrollOffset -= delta * 16;
            return true;
        }

        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (showConfirmModal) {
            if (keyCode == 256) { // GLFW.GLFW_KEY_ESCAPE
                closeConfirmModal();
                return true;
            }
            if (keyCode == 257 || keyCode == 335) { // GLFW.GLFW_KEY_ENTER or GLFW.GLFW_KEY_KP_ENTER
                executeModalConfirm();
                return true;
            }
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        if (this.minecraft != null) {
            this.minecraft.setScreen(this.parent);
        }
    }

    private Component getPowerText(double val) {
        return Component.translatable("iron_spell_tweaker.power_multiplier", String.format(Locale.ROOT, "%.1f", val));
    }

    private Component getManaText(double val) {
        return Component.translatable("iron_spell_tweaker.mana_multiplier", String.format(Locale.ROOT, "%.1f", val));
    }

    private Component getCooldownText(double val) {
        return Component.translatable("iron_spell_tweaker.cooldown", String.format(Locale.ROOT, "%.1f", val));
    }

    private Component getBatchPowerText(double val) {
        int percent = (int) Math.round(val);
        if (percent == 0) {
            return Component.translatable("iron_spell_tweaker.batch.power_keep");
        }
        double factor = 1.0 + (percent / 100.0);
        String sign = percent > 0 ? "+" : "";
        return Component.translatable("iron_spell_tweaker.batch.power_format", sign + percent, String.format(Locale.ROOT, "%.2f", factor));
    }

    private Component getBatchCooldownText(double val) {
        int percent = (int) Math.round(val);
        if (percent == 0) {
            return Component.translatable("iron_spell_tweaker.batch.cooldown_keep");
        }
        double factor = 1.0 + (percent / 100.0);
        String sign = percent > 0 ? "+" : "";
        return Component.translatable("iron_spell_tweaker.batch.cooldown_format", sign + percent, String.format(Locale.ROOT, "%.2f", factor));
    }

    private Component getBatchManaText(double val) {
        int percent = (int) Math.round(val);
        if (percent == 0) {
            return Component.translatable("iron_spell_tweaker.batch.mana_keep");
        }
        double factor = 1.0 + (percent / 100.0);
        String sign = percent > 0 ? "+" : "";
        return Component.translatable("iron_spell_tweaker.batch.mana_format", sign + percent, String.format(Locale.ROOT, "%.2f", factor));
    }

    private Component getMaxLevelText(int val) {
        return Component.translatable("iron_spell_tweaker.max_level", String.valueOf(val));
    }

    private ChatFormatting getRarityFormatting(SpellRarity rarity) {
        if (rarity == null) return ChatFormatting.WHITE;
        return switch (rarity) {
            case COMMON -> ChatFormatting.WHITE;
            case UNCOMMON -> ChatFormatting.GREEN;
            case RARE -> ChatFormatting.AQUA;
            case EPIC -> ChatFormatting.DARK_PURPLE;
            case LEGENDARY -> ChatFormatting.GOLD;
        };
    }

    private static String formatCastType(io.redspace.ironsspellbooks.api.spells.CastType type) {
        if (type == null) return "Instant";
        return switch (type) {
            case INSTANT -> "Instant";
            case LONG -> "Charge";
            case CONTINUOUS -> "Continuous";
            case NONE -> "Passive";
        };
    }

    public static int getSchoolColor(SchoolType school) {
        if (school == null) return 0xFF888888;
        try {
            if (school.getDisplayName() != null && school.getDisplayName().getStyle() != null && school.getDisplayName().getStyle().getColor() != null) {
                return 0xFF000000 | school.getDisplayName().getStyle().getColor().getValue();
            }
        } catch (Throwable ignored) {}
        String id = school.getId().getPath().toLowerCase(Locale.ROOT);
        return switch (id) {
            case "fire" -> 0xFFFF6622;
            case "ice" -> 0xFF55CCFF;
            case "lightning" -> 0xFF33FFFF;
            case "holy" -> 0xFFFFDD44;
            case "ender" -> 0xFFAA44FF;
            case "blood" -> 0xFFCC2233;
            case "evocation" -> 0xFFB066FF;
            case "nature" -> 0xFF44CC44;
            case "eldritch" -> 0xFF119988;
            default -> 0xFF88AAFF;
        };
    }

    private static String formatModName(String modId) {
        if ("irons_spellbooks".equals(modId)) return "Iron's Spells";
        String[] parts = modId.split("_");
        StringBuilder sb = new StringBuilder();
        for (String part : parts) {
            if (!part.isEmpty()) {
                sb.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1)).append(" ");
            }
        }
        return sb.toString().trim();
    }

    public static class CustomSlider extends AbstractSliderButton {
        private final Consumer<Double> onApply;
        private final Function<Double, Component> messageProvider;
        private final double min;
        private final double max;
        private final double step;

        public CustomSlider(int x, int y, int width, int height, double initial, double min, double max, double step, Function<Double, Component> messageProvider, Consumer<Double> onApply) {
            super(x, y, width, height, CommonComponents.EMPTY, toSlider(initial, min, max));
            this.min = min;
            this.max = max;
            this.step = step;
            this.messageProvider = messageProvider;
            this.onApply = onApply;
            this.updateMessage();
        }

        public void setSliderValue(double realValue) {
            this.value = toSlider(realValue, min, max);
            this.updateMessage();
        }

        public double getRealValue() {
            double raw = min + this.value * (max - min);
            return Math.round(raw / step) * step;
        }

        @Override
        protected void updateMessage() {
            this.setMessage(messageProvider.apply(getRealValue()));
        }

        @Override
        protected void applyValue() {
            onApply.accept(getRealValue());
        }

        private static double toSlider(double value, double min, double max) {
            return (Math.max(min, Math.min(max, value)) - min) / (max - min);
        }
    }

    public record SchoolFilterOption(SchoolType school, Component name) {
        public static final SchoolFilterOption ALL = new SchoolFilterOption(null, Component.translatable("iron_spell_tweaker.school.all"));
    }
}
