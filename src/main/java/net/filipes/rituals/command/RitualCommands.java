package net.filipes.rituals.command;

import com.mojang.brigadier.CommandDispatcher;
import net.filipes.rituals.pedestal.PedestalSavedData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.world.level.Level;

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
                                    Map<String, BlockPos> placed = data.getPlaced();

                                    if (placed.isEmpty()) {
                                        src.sendSuccess(() ->
                                                Component.literal("§eNo ritual pedestals have been generated yet."), false);
                                        return 1;
                                    }

                                    src.sendSuccess(() -> Component.literal("§6--- Ritual Pedestal Locations ---"), false);
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
        );
    }
}