package net.filipes.rituals.tooltip;

import net.minecraft.network.chat.FontDescription;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;


public enum TooltipFont {
    DEFAULT(null),
    MINECRAFT_FIVE(Identifier.fromNamespaceAndPath("rituals", "minecraftfive"));

    @Nullable
    private final Identifier id;

    TooltipFont(@Nullable Identifier id) {
        this.id = id;
    }

    public FontDescription.@Nullable Resource getDescription() {
        return id != null ? new FontDescription.Resource(id) : null;
    }
}
