package net.filipes.rituals.tooltip;

import net.minecraft.network.chat.*;
import net.minecraft.resources.Identifier;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class TooltipLine {

    private final List<Segment> segments;

    private TooltipLine(List<Segment> segments) {
        this.segments = segments;
    }

    public static TooltipLine translated(String key, int hexColor) {
        return builder().translated(key, hexColor).build();
    }

    public static TooltipLine literal(String text, int hexColor) {
        return builder().literal(text, hexColor).build();
    }

    /** Convenience for a single glyph rendered through the ICONS font (e.g. the clock icon). */
    public static TooltipLine icon(String glyph, int hexColor) {
        return builder().literal(glyph, hexColor).font(TooltipFont.ICONS).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public MutableComponent toComponent() {
        MutableComponent result = Component.empty();
        for (Segment s : segments) result.append(s.toComponent());
        return result;
    }

    public enum TooltipFont {
        DEFAULT(null),
        MINECRAFT_FIVE(Identifier.fromNamespaceAndPath("rituals", "minecraftfive")),
        // Maps private-use codepoints (e.g. TooltipRegistry.clockGlyph) to icon textures.
        // Requires assets/rituals/font/icons.json - see the icons.json example provided alongside this file.
        ICONS(Identifier.fromNamespaceAndPath("rituals", "icons"));

        @Nullable
        private final Identifier id;

        TooltipFont(@Nullable Identifier id) {
            this.id = id;
        }

        @Nullable
        public FontDescription.Resource getDescription() {
            return id != null ? new FontDescription.Resource(id) : null;
        }
    }

    private record Segment(
            String text,
            boolean translatable,
            int color,
            boolean bold,
            boolean italic,
            boolean underline,
            boolean strikethrough,
            boolean obfuscated,
            TooltipFont font
    ) {
        MutableComponent toComponent() {
            MutableComponent c = translatable
                    ? Component.translatable(text)
                    : Component.literal(text);

            Style style = Style.EMPTY
                    .withColor(TextColor.fromRgb(color))
                    .withBold(bold)
                    .withItalic(italic)
                    .withUnderlined(underline)
                    .withStrikethrough(strikethrough)
                    .withObfuscated(obfuscated);

            if (font != TooltipFont.DEFAULT && font.getDescription() != null) {
                style = style.withFont(font.getDescription());
            }

            return c.withStyle(style);
        }

        Segment withBold()          { return new Segment(text, translatable, color, true,  italic,  underline, strikethrough, obfuscated, font); }
        Segment withItalic()        { return new Segment(text, translatable, color, bold,  true,    underline, strikethrough, obfuscated, font); }
        Segment withUnderline()     { return new Segment(text, translatable, color, bold,  italic,  true,      strikethrough, obfuscated, font); }
        Segment withStrikethrough() { return new Segment(text, translatable, color, bold,  italic,  underline, true,          obfuscated, font); }
        Segment withObfuscated()    { return new Segment(text, translatable, color, bold,  italic,  underline, strikethrough, true,       font); }
        Segment withFont(TooltipFont newFont) { return new Segment(text, translatable, color, bold,  italic,  underline, strikethrough, obfuscated, newFont); }
    }

    public static class Builder {

        private final List<Segment> segments = new ArrayList<>();

        public Builder literal(String text, int hexColor) {
            segments.add(new Segment(text, false, hexColor, false, false, false, false, false, TooltipFont.DEFAULT));
            return this;
        }

        public Builder translated(String key, int hexColor) {
            segments.add(new Segment(key, true, hexColor, false, false, false, false, false, TooltipFont.DEFAULT));
            return this;
        }

        public Builder bold()          { replaceLast(last().withBold());          return this; }
        public Builder italic()        { replaceLast(last().withItalic());        return this; }
        public Builder underline()     { replaceLast(last().withUnderline());     return this; }
        public Builder strikethrough() { replaceLast(last().withStrikethrough()); return this; }
        public Builder obfuscated()    { replaceLast(last().withObfuscated());    return this; }

        public Builder font(TooltipFont font) { replaceLast(last().withFont(font)); return this; }
        public Builder minecraftFive()        { replaceLast(last().withFont(TooltipFont.MINECRAFT_FIVE)); return this; }

        public TooltipLine build() {
            return new TooltipLine(new ArrayList<>(segments));
        }

        private Segment last() {
            return segments.get(segments.size() - 1);
        }

        private void replaceLast(Segment s) {
            segments.set(segments.size() - 1, s);
        }
    }
}