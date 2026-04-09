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

    public static void register() {
        UPGRADE_STAGE = Registry.register(
                BuiltInRegistries.DATA_COMPONENT_TYPE,
                Identifier.fromNamespaceAndPath("rituals", "upgrade_stage"),
                DataComponentType.<Integer>builder()
                        .persistent(Codec.intRange(1, 5))
                        .networkSynchronized(ByteBufCodecs.VAR_INT)
                        .build()
        );
    }

    public static int getStage(net.minecraft.world.item.ItemStack stack) {
        Integer stage = stack.get(UPGRADE_STAGE);
        return stage != null ? stage : 1;
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
}