package net.filipes.rituals.component;

import com.mojang.serialization.Codec;
import net.filipes.rituals.upgrade.KillUpgradeRegistry;
import net.filipes.rituals.upgrade.UpgradeRecipeRegistry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Registry;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;

import java.util.List;

public class ModDataComponents {

    public static final int STAGE_CODEC_MAX = 10;

    public static final int DEFAULT_MAX_STAGE = 1;

    public static DataComponentType<Integer> UPGRADE_STAGE;
    public static DataComponentType<Integer> KILL_COUNT;
    public static DataComponentType<Integer> LIGHTNING_RAPIER_CHARGE;

    public static final DataComponentType<Boolean> MINING_ENABLED = Registry.register(
            BuiltInRegistries.DATA_COMPONENT_TYPE,
            Identifier.fromNamespaceAndPath("rituals", "mining_enabled"),
            DataComponentType.<Boolean>builder()
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL)
                    .build()
    );

    public static void register() {
        UPGRADE_STAGE = Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath("rituals", "upgrade_stage"),
                DataComponentType.<Integer>builder()
                        .persistent(Codec.intRange(1, STAGE_CODEC_MAX))
                        .networkSynchronized(ByteBufCodecs.VAR_INT)
                        .build()
        );

        KILL_COUNT = Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath("rituals", "kill_count"),
                DataComponentType.<Integer>builder()
                        .persistent(Codec.intRange(0, Integer.MAX_VALUE))
                        .networkSynchronized(ByteBufCodecs.VAR_INT)
                        .build()
        );

        LIGHTNING_RAPIER_CHARGE = Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath("rituals", "lightning_rapier_charge"),
                DataComponentType.<Integer>builder()
                        .persistent(Codec.intRange(0, 6))
                        .networkSynchronized(ByteBufCodecs.VAR_INT)
                        .build()
        );
    }

    public static int getMaxStage(Item item) {
        int fromRecipe = UpgradeRecipeRegistry.getMaxStage(item);
        int fromKill   = KillUpgradeRegistry.getMaxStage(item);
        int resolved   = Math.max(fromRecipe, fromKill);
        return resolved > 0 ? resolved : DEFAULT_MAX_STAGE;
    }

    public static int getMaxStage(ItemStack stack) {
        return getMaxStage(stack.getItem());
    }

    public static int getStage(ItemStack stack) {
        Integer stage = stack.get(UPGRADE_STAGE);
        return stage != null ? stage : 1;
    }

    public static int getKillCount(ItemStack stack) {
        Integer kills = stack.get(KILL_COUNT);
        return kills != null ? kills : 0;
    }

    public static void setStage(ItemStack stack, int stage) {
        int clamped = Math.clamp(stage, 1, getMaxStage(stack));
        stack.set(UPGRADE_STAGE, clamped);
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(
                List.of((float) clamped),
                List.of(),
                List.of(),
                List.of()
        ));
    }

    public static void setStageMax(ItemStack stack) {
        setStage(stack, getMaxStage(stack));
    }

    public static void setKillCount(ItemStack stack, int kills) {
        stack.set(KILL_COUNT, Math.max(0, kills));
    }

    public static ItemStack withStage(ItemStack stack, int stage) {
        ItemStack copy = stack.copy();
        setStage(copy, stage);
        return copy;
    }

    public static ItemStack withStageMax(ItemStack stack) {
        return withStage(stack, getMaxStage(stack));
    }

    public static ItemStack withKillCount(ItemStack stack, int kills) {
        ItemStack copy = stack.copy();
        copy.set(KILL_COUNT, kills);
        return copy;
    }
}