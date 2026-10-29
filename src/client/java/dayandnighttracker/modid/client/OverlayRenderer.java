package dayandnighttracker.modid.client;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;

public final class OverlayRenderer {
    public static final int ICON = 10;
    public static final int PAD = 3;
    public static final int GAP = 4;
    public static final int HEIGHT = 16;

    public enum Phase {
        SUNRISE(new String[] {
            "..........",
            "..........",
            "....oo....",
            ".o......o.",
            "...yyyy...",
            "..yyyyyy..",
            "o.yyyyyy.o",
            "gggggggggg",
            "dddddddddd",
            "dddddddddd"
        }),
        DAY(new String[] {
            "....oo....",
            ".o......o.",
            "...YYYY...",
            "..YYYYYY..",
            "o.YYYYYY.o",
            "o.YYYYYY.o",
            "..YYYYYY..",
            "...YYYY...",
            ".o......o.",
            "....oo...."
        }),
        SUNSET(new String[] {
            "..........",
            "..........",
            "..........",
            "....rr....",
            ".r......r.",
            "...RRRR...",
            "r.RRRRRR.r",
            "GGGGGGGGGG",
            "DDDDDDDDDD",
            "DDDDDDDDDD"
        }),
        NIGHT(new String[] {
            "..........",
            "...mmm..w.",
            "..mmm.....",
            ".mmm..w...",
            ".mmm......",
            ".mmm....w.",
            "..mmm.....",
            "...mmmmm..",
            "....mmm...",
            ".........."
        });

        final String[] grid;

        Phase(String[] grid) {
            this.grid = grid;
        }
    }

    public record Info(Phase phase, String text) {}

    private static String cachedLang;
    private static DateTimeFormatter cachedFormat;

    private OverlayRenderer() {}

    public static Info current() {
        Minecraft mc = Minecraft.getInstance();
        long ticks = mc.level != null ? mc.level.getDefaultClockTime() : 9000L;
        long day = Math.floorDiv(ticks, 24000L) + 1;
        long clock = Math.floorMod(ticks + 6000L, 24000L);
        int hour = (int) (clock / 1000);
        int minute = (int) ((clock % 1000) * 60 / 1000);

        Phase phase;
        if (hour >= 5 && hour < 7) phase = Phase.SUNRISE;
        else if (hour >= 7 && hour < 17) phase = Phase.DAY;
        else if (hour >= 17 && hour < 19) phase = Phase.SUNSET;
        else phase = Phase.NIGHT;

        String word = Component.translatableWithFallback("dnt.day", "Day").getString();
        String text = word + " " + day + " \u2013 " + formatTime(hour, minute);
        return new Info(phase, text);
    }

    private static String formatTime(int hour, int minute) {
        String lang = Minecraft.getInstance().options.languageCode;
        if (cachedFormat == null || !lang.equals(cachedLang)) {
            String[] parts = lang.split("_");
            Locale locale = parts.length >= 2
                ? Locale.of(parts[0], parts[1].toUpperCase(Locale.ROOT))
                : Locale.of(parts[0]);
            cachedFormat = DateTimeFormatter.ofLocalizedTime(FormatStyle.SHORT).withLocale(locale);
            cachedLang = lang;
        }
        return cachedFormat.format(LocalTime.of(hour, minute))
            .replace('\u202f', ' ')
            .replace('\u00a0', ' ');
    }

    public static int width(Font font, Info info) {
        return PAD + ICON + GAP + font.width(info.text()) + PAD;
    }

    public static int posX(int width) {
        int sw = Minecraft.getInstance().getWindow().getGuiScaledWidth();
        return (int) Math.round(Settings.posX() * Math.max(0, sw - width));
    }

    public static int posY() {
        int sh = Minecraft.getInstance().getWindow().getGuiScaledHeight();
        return (int) Math.round(Settings.posY() * Math.max(0, sh - HEIGHT));
    }

    public static void draw(GuiGraphicsExtractor g, Font font, Info info, int x, int y) {
        int w = width(font, info);
        g.fill(x + 1, y, x + w - 1, y + HEIGHT, 0x90000000);
        g.fill(x, y + 1, x + w, y + HEIGHT - 1, 0x90000000);
        drawIcon(g, info.phase(), x + PAD, y + (HEIGHT - ICON) / 2);
        g.text(font, info.text(), x + PAD + ICON + GAP, y + 4, 0xFFFFFFFF, true);
    }

    private static void drawIcon(GuiGraphicsExtractor g, Phase phase, int x, int y) {
        for (int row = 0; row < ICON; row++) {
            String line = phase.grid[row];
            for (int col = 0; col < ICON; col++) {
                int color = color(line.charAt(col));
                if (color != 0) {
                    g.fill(x + col, y + row, x + col + 1, y + row + 1, color);
                }
            }
        }
    }

    private static int color(char c) {
        return switch (c) {
            case 'o' -> 0xFFF28C28;
            case 'y' -> 0xFFFFC62E;
            case 'Y' -> 0xFFFFD43B;
            case 'g' -> 0xFF5E9E3A;
            case 'd' -> 0xFF8B5A2B;
            case 'r' -> 0xFFFFC19A;
            case 'R' -> 0xFFFF5A36;
            case 'G' -> 0xFF3F6F3A;
            case 'D' -> 0xFF6B4423;
            case 'm' -> 0xFFF5E9B8;
            case 'w' -> 0xFFFFFFFF;
            default -> 0;
        };
    }
}
