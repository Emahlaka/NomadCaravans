package com.sappyeddie.nomadcaravans;

import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class NomadCaravans {
    public static final String MOD_ID = "nomadcaravans";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private NomadCaravans() {
    }

    public static void init() {
        LOGGER.info("Initializing {}", MOD_ID);
        NomadConfig.load();
        ModRegistries.init();
        ModSpawns.init();
    }

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }
}
