package net.filipes.rituals.tooltip;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;

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

    public static Builder builder() {
        return new Builder();
    }

    public MutableComponent toComponent() {
        MutableComponent result = Component.empty();
        for (Segment s : segments) result.append(s.toComponent());
        return result;
    }

    private record Segment(
            String text,
            boolean translatable,
            int color,
            boolean bold,
            boolean italic,
            boolean underline,
            boolean strikethrough,
            boolean obfuscated
    ) {
        MutableComponent toComponent() {
            MutableComponent c = translatable
                    ? Component.translatable(text)
                    : Component.literal(text);
            return c.withStyle(Style.EMPTY
                    .withColor(TextColor.fromRgb(color))
                    .withBold(bold)
                    .withItalic(italic)
                    .withUnderlined(underline)
                    .withStrikethrough(strikethrough)
                    .withObfuscated(obfuscated));
        }

        Segment withBold()          { return new Segment(text, translatable, color, true,  italic,  underline, strikethrough, obfuscated); }
        Segment withItalic()        { return new Segment(text, translatable, color, bold,  true,    underline, strikethrough, obfuscated); }
        Segment withUnderline()     { return new Segment(text, translatable, color, bold,  italic,  true,      strikethrough, obfuscated); }
        Segment withStrikethrough() { return new Segment(text, translatable, color, bold,  italic,  underline, true,          obfuscated); }
        Segment withObfuscated()    { return new Segment(text, translatable, color, bold,  italic,  underline, strikethrough, true);       }
    }

    public static class Builder {

        private final List<Segment> segments = new ArrayList<>();

        public Builder literal(String text, int hexColor) {
            segments.add(new Segment(text, false, hexColor, false, false, false, false, false));
            return this;
        }

        public Builder translated(String key, int hexColor) {
            segments.add(new Segment(key, true, hexColor, false, false, false, false, false));
            return this;
        }

        public Builder bold()          { replaceLast(last().withBold());          return this; }
        public Builder italic()        { replaceLast(last().withItalic());        return this; }
        public Builder underline()     { replaceLast(last().withUnderline());     return this; }
        public Builder strikethrough() { replaceLast(last().withStrikethrough()); return this; }
        public Builder obfuscated()    { replaceLast(last().withObfuscated());    return this; }

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