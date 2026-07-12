package net.filipes.rituals.tooltip;

import net.filipes.rituals.client.cooldown.CooldownManager;

import java.util.ArrayList;
import java.util.List;

public class StageAbility {

    public enum ActionSlot {
        ONE("Action One"),
        TWO("Action Two"),
        THREE("Action Three"),
        NONE(null);

        private final String label;

        ActionSlot(String label) {
            this.label = label;
        }

        public String label() {
            return label;
        }
    }

    private final boolean passive;
    private final int stage;
    private final String manualName;
    private final String abilityId;
    private final List<TooltipLine> description;
    private final Long manualCooldownMs;
    private final boolean cooldownFromAbility;
    private final Integer headerColorOverride;
    private final ActionSlot actionSlot;
    private final boolean shift;

    private StageAbility(Builder b) {
        this.passive = b.passive;
        this.stage = b.stage;
        this.manualName = b.manualName;
        this.abilityId = b.abilityId;
        this.description = List.copyOf(b.description);
        this.manualCooldownMs = b.manualCooldownMs;
        this.cooldownFromAbility = b.cooldownFromAbility;
        this.headerColorOverride = b.headerColorOverride;
        this.actionSlot = b.actionSlot;
        this.shift = b.shift;
    }

    public boolean isPassive() { return passive; }

    public int stage() { return stage; }

    public Integer headerColorOverride() { return headerColorOverride; }

    public List<TooltipLine> description() { return description; }

    public ActionSlot actionSlot() { return actionSlot; }

    public boolean shift() { return shift; }

    public String resolvedHeaderText() {
        String name = resolvedNameOrNull();
        if (passive) {
            return name != null ? "Passive - " + name : "Passive";
        }
        return name != null ? "Stage " + stage + " - " + name : "Stage " + stage;
    }

    public String resolvedActionLabel() {
        if (actionSlot == ActionSlot.NONE) return null;
        return shift ? "Shift + " + actionSlot.label() : actionSlot.label();
    }

    private String resolvedNameOrNull() {
        if (manualName != null) return manualName;
        if (abilityId != null) {
            CooldownManager.AbilityDefinition def = CooldownManager.getDefinitions().get(abilityId);
            if (def != null) return def.displayName();
        }
        return null;
    }

    public Long resolvedCooldownMs() {
        if (manualCooldownMs != null) return manualCooldownMs;
        if (cooldownFromAbility && abilityId != null) {
            CooldownManager.AbilityDefinition def = CooldownManager.getDefinitions().get(abilityId);
            if (def != null) return def.durationMs();
        }
        return null;
    }

    public static Builder passive() {
        return new Builder(true, 0);
    }

    public static Builder builder(int stage) {
        return new Builder(false, stage);
    }

    public static class Builder {
        private final boolean passive;
        private final int stage;
        private String manualName;
        private String abilityId;
        private final List<TooltipLine> description = new ArrayList<>();
        private Long manualCooldownMs;
        private boolean cooldownFromAbility = false;
        private Integer headerColorOverride;
        private ActionSlot actionSlot = ActionSlot.NONE;
        private boolean shift = false;

        private Builder(boolean passive, int stage) {
            this.passive = passive;
            this.stage = stage;
        }

        public Builder name(String name) {
            this.manualName = name;
            return this;
        }

        public Builder abilityId(String id) {
            this.abilityId = id;
            return this;
        }

        public Builder describe(String text, int hexColor) {
            description.add(TooltipLine.literal(text, hexColor));
            return this;
        }

        public Builder describe(TooltipLine line) {
            description.add(line);
            return this;
        }

        public Builder cooldown(long ms) {
            this.manualCooldownMs = ms;
            return this;
        }

        public Builder cooldownFromAbility() {
            this.cooldownFromAbility = true;
            return this;
        }

        public Builder actionSlot(ActionSlot slot) {
            this.actionSlot = slot;
            return this;
        }

        public Builder holdShift() {
            this.shift = true;
            return this;
        }

        public Builder headerColor(int hexColor) {
            this.headerColorOverride = hexColor;
            return this;
        }

        public StageAbility build() {
            return new StageAbility(this);
        }
    }
}