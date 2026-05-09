package net.filipes.rituals.component;

import com.mojang.serialization.Codec;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.core.Registry;
import net.minecraft.world.item.component.CustomModelData;

import java.util.List;

public class ModDataComponents {

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
                        .persistent(Codec.intRange(1, 7))
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


    public static int getStage(net.minecraft.world.item.ItemStack stack) {
        Integer stage = stack.get(UPGRADE_STAGE);
        return stage != null ? stage : 1;
    }

    public static int getKillCount(net.minecraft.world.item.ItemStack stack) {
        Integer kills = stack.get(KILL_COUNT);
        return kills != null ? kills : 0;
    }

    public static net.minecraft.world.item.ItemStack withStage(net.minecraft.world.item.ItemStack stack, int stage) {
        net.minecraft.world.item.ItemStack copy = stack.copy();
        copy.set(UPGRADE_STAGE, stage);
        copy.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(
                List.of((float) stage),
                List.of(),
                List.of(),
                List.of()
        ));
        return copy;
    }

    public static net.minecraft.world.item.ItemStack withKillCount(net.minecraft.world.item.ItemStack stack, int kills) {
        net.minecraft.world.item.ItemStack copy = stack.copy();
        copy.set(KILL_COUNT, kills);
        return copy;
    }
}