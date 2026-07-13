package net.filipes.rituals.command;

import com.mojang.brigadier.CommandDispatcher;
import net.filipes.rituals.pedestal.PedestalSavedData;
import net.filipes.rituals.entity.custom.ElectricBoltEntity;
import net.filipes.rituals.entity.custom.ScreenShakeEntity;
import net.filipes.rituals.pedestal.PedestalTypes;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.util.Util;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.Vec3;

import java.util.Map;

public class RitualCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("rituals")
                        .requires(src -> src.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER))
                        .then(Commands.literal("find")
                                .executes(ctx -> {
                                    CommandSourceStack src = ctx.getSource();
                                    ServerLevel overworld = src.getServer().getLevel(Level.OVERWORLD);
                                    if (overworld == null) {
                                        src.sendFailure(Component.literal("Overworld not found."));
                                        return 0;
                                    }

                                    PedestalSavedData data = PedestalSavedData.getOrCreate(overworld);
                                    int totalTypes = PedestalTypes.REGISTRY.size();

                                    if (data.getPlaced().size() < totalTypes) {
                                        int found = PedestalScanner.scanForPedestals(overworld);
                                        if (found > 0) {
                                            src.sendSuccess(() -> Component.literal("§7Found " + found + " new pedestal(s)."), false);
                                        }
                                    }

                                    Map<String, BlockPos> placed = data.getPlaced();
                                    if (placed.isEmpty()) {
                                        src.sendSuccess(() -> Component.literal("§eNo ritual pedestals have been generated yet."), false);
                                        return 1;
                                    }

                                    src.sendSuccess(() -> Component.literal("§6--- Ritual Pedestal Locations ("
                                            + placed.size() + "/" + totalTypes + ") ---"), false);

                                    placed.forEach((typeId, pos) -> {
                                        String coords = pos.getX() + " " + pos.getY() + " " + pos.getZ();
                                        String tpCmd  = "/tp @s " + coords;
                                        Component msg = Component.literal("§e" + typeId + "§r: §b" + coords + " §7[click to tp]")
                                                .withStyle(style -> style
                                                        .withClickEvent(new ClickEvent.RunCommand(tpCmd))
                                                        .withUnderlined(true));
                                        src.sendSuccess(() -> msg, false);
                                    });

                                    return 1;
                                })
                        )
                        .then(Commands.literal("screenshake")
                                .executes(ctx -> {
                                    CommandSourceStack src = ctx.getSource();
                                    ServerLevel level = src.getLevel();
                                    Vec3 position = src.getPosition();

                                    level.addFreshEntity(new ScreenShakeEntity(
                                            level,
                                            position,
                                            12.0f,
                                            1.0f,
                                            80
                                    ));

                                    src.sendSuccess(() -> Component.literal("Spawned a screen shake source."), false);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("bolt")
                                .executes(ctx -> {
                                    CommandSourceStack src = ctx.getSource();
                                    ServerLevel level = src.getLevel();

                                    Vec3 start = src.getEntity() instanceof LivingEntity living
                                            ? living.getEyePosition()
                                            : src.getPosition().add(0.0, 1.0, 0.0);
                                    Vec3 direction = src.getEntity() != null
                                            ? src.getEntity().getLookAngle()
                                            : new Vec3(0.0, 0.0, 1.0);
                                    Vec3 end = start.add(direction.scale(12.0));

                                    ElectricBoltEntity.spawn(level, start, end, 1.8f, 0.14f, 0x98E8FF);

                                    src.sendSuccess(() -> Component.literal("Spawned an electric bolt."), false);
                                    return 1;
                                })
                        )
                        .then(Commands.literal("debug")
                                .executes(ctx -> {
                                    CommandSourceStack src = ctx.getSource();
                                    ServerLevel level = src.getServer().getLevel(Level.OVERWORLD);
                                    if (level == null) {
                                        src.sendFailure(Component.literal("Overworld not found."));
                                        return 0;
                                    }

                                    var structureSetRegistry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET);
                                    System.out.println("[Rituals] Registered structure sets: " + structureSetRegistry.keySet());

                                    var structureRegistry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
                                    System.out.println("[Rituals] Registered structures: " + structureRegistry.keySet());

                                    ResourceKey<Structure> pedestalKey = ResourceKey.create(Registries.STRUCTURE,
                                            Identifier.fromNamespaceAndPath("rituals", "pedestal_structure"));

                                    var structureHolderOpt = structureRegistry.get(pedestalKey);
                                    if (structureHolderOpt.isEmpty()) {
                                        System.out.println("[Rituals] pedestal_structure NOT FOUND in structure registry!");
                                        src.sendSuccess(() -> Component.literal("§cpedestal_structure missing from registry, check console"), false);
                                        return 1;
                                    }

                                    Holder<Structure> holder = structureHolderOpt.get();
                                    var chunkSource = level.getChunkSource();
                                    if (chunkSource instanceof ServerChunkCache serverChunkCache) {
                                        var structureState = serverChunkCache.getGeneratorState();
                                        var placements = structureState.getPlacementsForStructure(holder);
                                        System.out.println("[Rituals] Placements for pedestal_structure: " + placements);
                                        src.sendSuccess(() -> Component.literal("§7Found " + placements.size() + " placement(s), check console for details"), false);
                                    } else {
                                        System.out.println("[Rituals] ChunkSource is not a ServerChunkCache: " + chunkSource.getClass());
                                    }

                                    return 1;
                                })
                        )

        );
    }
}
