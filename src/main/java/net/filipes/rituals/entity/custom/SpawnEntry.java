package net.filipes.rituals.entity.custom;

import net.minecraft.world.entity.EntityType;

public record SpawnEntry(
        EntityType<?> type,
        int count,
        double yOffset,
        float scale,
        int delayTicks
) {}