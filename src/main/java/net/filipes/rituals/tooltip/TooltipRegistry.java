package net.filipes.rituals.tooltip;

import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.filipes.rituals.component.ModDataComponents;
import net.filipes.rituals.upgrade.KillUpgradeRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;

import java.util.*;

public class TooltipRegistry {

    private static final Map<Item, List<TooltipLine>> REGISTRY = new LinkedHashMap<>();
    private static final Map<Item, List<StageAbility>> STAGE_ABILITIES = new LinkedHashMap<>();
    private static final Map<Item, Integer> LOCKED_COLOR = new HashMap<>();
    public static int unlockedColor = 0x55FF55;
    public static int defaultLockedColor = 0xBB99CC;
    public static int cooldownColor = 0xAAAAAA;
    public static String clockGlyph = "\u231A";
    public static TooltipLine.TooltipFont clockGlyphFont = TooltipLine.TooltipFont.DEFAULT;
    public static TooltipLine.TooltipFont headerFont = TooltipLine.TooltipFont.MINECRAFT_FIVE;
    public static TooltipLine.TooltipFont cooldownFont = TooltipLine.TooltipFont.DEFAULT;
    public static boolean blankLineBeforeAbility = true;
    public static boolean showKillRequirement = true;
    public static java.util.function.IntFunction<String> killRequirementLabel =
            kills -> " [" + kills + " kill" + (kills == 1 ? "" : "s") + "]";
    public static boolean showScrollHint = true;

    public static TooltipLine scrollHintLine = TooltipLine.builder()
            .literal("[Scroll to see full tooltip]", 0x7a7a7a)
            .minecraftFive()
            .build();

    private static final Set<Item> SCROLL_HINT_DISABLED = new HashSet<>();

    public static void disableScrollHint(Item item) {
        SCROLL_HINT_DISABLED.add(item);
    }

    public static void register(Item item, TooltipLine... lines) {
        REGISTRY.computeIfAbsent(item, k -> new ArrayList<>())
                .addAll(Arrays.asList(lines));
    }

    public static void register(Item item, TooltipLine line) {
        REGISTRY.computeIfAbsent(item, k -> new ArrayList<>()).add(line);
    }

    public static void registerStages(Item item, int lockedColor, StageAbility... abilities) {
        LOCKED_COLOR.put(item, lockedColor);
        STAGE_ABILITIES.computeIfAbsent(item, k -> new ArrayList<>()).addAll(Arrays.asList(abilities));
    }

    public static java.util.function.IntFunction<TooltipLine> stageLineStyle =
            stage -> TooltipLine.builder()
                    .literal("Stage - " + stage, 0xBB99CC)
                    .build();

    public static java.util.function.IntFunction<TooltipLine> killLineStyle =
            kills -> TooltipLine.builder()
                    .literal("Kills: " + kills, 0xBB99CC)
                    .build();

    public static void init() {
        ItemTooltipCallback.EVENT.register((stack, tooltipContext, tooltipType, lines) -> {
            int insertAt = 1;

            if (ModDataComponents.getMaxStage(stack.getItem()) > 1) {
                int stage = ModDataComponents.getStage(stack);
                lines.add(insertAt++, stageLineStyle.apply(stage).toComponent());
            }

            if (KillUpgradeRegistry.isKillUpgradeable(stack.getItem())) {
                int currentKills = ModDataComponents.getKillCount(stack);
                lines.add(insertAt++, killLineStyle.apply(currentKills).toComponent());
            }

            List<TooltipLine> tooltips = REGISTRY.get(stack.getItem());
            if (tooltips != null) {
                for (TooltipLine line : tooltips) {
                    lines.add(insertAt++, line.toComponent());
                }
            }

            List<StageAbility> abilities = STAGE_ABILITIES.get(stack.getItem());
            if (abilities != null) {
                int currentStage = ModDataComponents.getStage(stack);
                int lockedColor = LOCKED_COLOR.getOrDefault(stack.getItem(), defaultLockedColor);
                boolean killUpgradeable = KillUpgradeRegistry.isKillUpgradeable(stack.getItem());
                if (showScrollHint && !SCROLL_HINT_DISABLED.contains(stack.getItem())) {
                    lines.add(insertAt++, scrollHintLine.toComponent());
                }

                for (StageAbility ability : abilities) {
                    if (blankLineBeforeAbility) {
                        lines.add(insertAt++, Component.empty());
                    }

                    int headerColor;
                    if (ability.isPassive()) {
                        headerColor = ability.headerColorOverride() != null ? ability.headerColorOverride() : lockedColor;
                    } else {
                        boolean unlocked = currentStage >= ability.stage();
                        headerColor = unlocked
                                ? unlockedColor
                                : (ability.headerColorOverride() != null ? ability.headerColorOverride() : lockedColor);
                    }

                    String headerText = ability.resolvedHeaderText();
                    if (showKillRequirement && !ability.isPassive() && killUpgradeable) {
                        Optional<Integer> kills = KillUpgradeRegistry.getKillsRequiredForStage(stack.getItem(), ability.stage());
                        if (kills.isPresent()) {
                            headerText += killRequirementLabel.apply(kills.get());
                        }
                    }

                    lines.add(insertAt++, TooltipLine.builder()
                            .literal(headerText, headerColor)
                            .font(headerFont)
                            .build()
                            .toComponent());

                    for (TooltipLine descLine : ability.description()) {
                        lines.add(insertAt++, descLine.toComponent());
                    }

                    Long cooldownMs = ability.resolvedCooldownMs();
                    String actionLabel = ability.resolvedActionLabel();
                    String actionText = actionLabel != null ? "[" + actionLabel + "]" : null;

                    if (cooldownMs != null || actionText != null) {
                        TooltipLine.Builder cooldownLine = TooltipLine.builder();

                        if (cooldownMs != null) {
                            long seconds = cooldownMs / 1000;
                            cooldownLine.literal(clockGlyph + " ", cooldownColor).font(clockGlyphFont)
                                    .literal(seconds + "s cooldown", cooldownColor).font(cooldownFont);
                            if (actionText != null) {
                                cooldownLine.literal(" " + actionText, cooldownColor).font(cooldownFont);
                            }
                        } else {
                            cooldownLine.literal(actionText, cooldownColor).font(cooldownFont);
                        }

                        lines.add(insertAt++, cooldownLine.build().toComponent());
                    }
                }


            }

            net.filipes.rituals.client.tooltip.TooltipScrollHandler.updateContext(
                    stack.getItem(), ModDataComponents.getStage(stack));
        });
    }
}