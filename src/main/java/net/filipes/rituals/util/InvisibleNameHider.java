package net.filipes.rituals.util;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

public class InvisibleNameHider {

    private static final int GARBLED_LENGTH = 10;
    private static boolean generatingDeathMessage = false;

    public static boolean isGeneratingDeathMessage() {
        return generatingDeathMessage;
    }

    public static Component wrapDeathMessage(java.util.function.Supplier<Component> supplier) {
        generatingDeathMessage = true;
        try {
            return supplier.get();
        } finally {
            generatingDeathMessage = false;
        }
    }

    public static Component garbledName() {
        return Component.literal("z".repeat(GARBLED_LENGTH))
                .withStyle(ChatFormatting.OBFUSCATED, ChatFormatting.WHITE);
    }
}