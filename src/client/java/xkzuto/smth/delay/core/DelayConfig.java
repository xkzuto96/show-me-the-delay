package xkzuto.smth.delay.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class DelayConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final File FILE = new File(FabricLoader.getInstance().getConfigDir().toFile(), "show-me-the-delay.json");

    public int xOffset = -120;
    public int yOffset = 0;
    public int maxHistory = 10;
    public float backgroundAlpha = 170; // ~0xAA
    public int backgroundColor = 0x000000;
    public int fadeDelayMs = 3000;
    public int cornerRadius = 8;

    public static DelayConfig INSTANCE = new DelayConfig();

    public static void load() {
        if (FILE.exists()) {
            try (FileReader reader = new FileReader(FILE)) {
                INSTANCE = GSON.fromJson(reader, DelayConfig.class);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        DelayTracker.MAX_HISTORY = INSTANCE.maxHistory;
    }

    public static void save() {
        INSTANCE.maxHistory = DelayTracker.MAX_HISTORY;
        try (FileWriter writer = new FileWriter(FILE)) {
            GSON.toJson(INSTANCE, writer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
