package com.sappyeddie.nomadcaravans;

import dev.architectury.platform.Platform;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class NomadConfig {

    private NomadConfig() {
    }

    public static boolean lanternLightBlocks = false;

    private static final String FILE_NAME = "nomadcaravans.properties";
    private static final String KEY_LANTERN = "lanternLightBlocks";

    public static void load() {
        Path path = Platform.getConfigFolder().resolve(FILE_NAME);
        if (!Files.exists(path)) {
            save();
            return;
        }
        Properties props = new Properties();
        try (InputStream in = Files.newInputStream(path)) {
            props.load(in);
        } catch (IOException e) {
            NomadCaravans.LOGGER.warn("Failed to read {} — using defaults", FILE_NAME, e);
            return;
        }
        lanternLightBlocks = Boolean.parseBoolean(props.getProperty(KEY_LANTERN, "false"));
        NomadCaravans.LOGGER.info("Config loaded: {}={}", KEY_LANTERN, lanternLightBlocks);
    }

    private static void save() {
        Properties props = new Properties();
        props.setProperty(KEY_LANTERN, String.valueOf(lanternLightBlocks));
        Path path = Platform.getConfigFolder().resolve(FILE_NAME);
        try (OutputStream out = Files.newOutputStream(path)) {
            props.store(out, "Nomad Caravans configuration");
        } catch (IOException e) {
            NomadCaravans.LOGGER.warn("Failed to write default {}", FILE_NAME, e);
        }
    }
}
