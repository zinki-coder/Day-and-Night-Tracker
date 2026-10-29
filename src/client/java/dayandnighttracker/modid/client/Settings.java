package dayandnighttracker.modid.client;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import net.fabricmc.loader.api.FabricLoader;

public final class Settings {
    public static final double DEFAULT_X = 0.99;
    public static final double DEFAULT_Y = 0.02;

    private static double posX = DEFAULT_X;
    private static double posY = DEFAULT_Y;

    private Settings() {}

    private static Path file() {
        return FabricLoader.getInstance().getConfigDir().resolve("day-and-night-tracker.properties");
    }

    public static double posX() {
        return posX;
    }

    public static double posY() {
        return posY;
    }

    public static void setPos(double x, double y) {
        posX = clamp(x);
        posY = clamp(y);
    }

    public static void reset() {
        posX = DEFAULT_X;
        posY = DEFAULT_Y;
        save();
    }

    public static void load() {
        Path f = file();
        if (!Files.exists(f)) return;
        Properties p = new Properties();
        try (Reader r = Files.newBufferedReader(f)) {
            p.load(r);
            posX = clamp(Double.parseDouble(p.getProperty("posX", String.valueOf(DEFAULT_X))));
            posY = clamp(Double.parseDouble(p.getProperty("posY", String.valueOf(DEFAULT_Y))));
        } catch (IOException | NumberFormatException e) {
            posX = DEFAULT_X;
            posY = DEFAULT_Y;
        }
    }

    public static void save() {
        Properties p = new Properties();
        p.setProperty("posX", String.valueOf(posX));
        p.setProperty("posY", String.valueOf(posY));
        try (Writer w = Files.newBufferedWriter(file())) {
            p.store(w, "Day and Night Tracker");
        } catch (IOException ignored) {
        }
    }

    private static double clamp(double v) {
        return Math.max(0.0, Math.min(1.0, v));
    }
}
