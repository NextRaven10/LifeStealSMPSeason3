package com.github.tatercertified.lifesteal.util;

import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.util.Objects;
import java.util.Properties;

public class Config {
    public static final Properties properties = new Properties();

    private static final String cfgver = "1.4";
    public static String revivalBlock;
    public static boolean generateOres;
    private static final Logger LOGGER = LoggerFactory.getLogger("Lifesteal-Config");


    public static void init() {
        var path = FabricLoader.getInstance().getConfigDir().resolve("lifesteal.properties");

        if (Files.notExists(path)) {
            try {
                mkfile();
            } catch (IOException e) {
                LOGGER.error("Failed to create config: ", e);
            }
        } else {
            try {
                loadcfg();
            } catch (IOException e) {
                LOGGER.error("Failed to load config: ", e);
            }
            if (!(Objects.equals(properties.getProperty("config-version"), cfgver))) {
                try {
                    mkfile();
                } catch (IOException e) {
                    LOGGER.error("Failed to update config: ", e);
                }
            } else {
                parse();
            }
        }
    }

    private static void mkfile() throws IOException {
        OutputStream output = new FileOutputStream(String.valueOf(FabricLoader.getInstance().getConfigDir().resolve("lifesteal.properties")));
        if (!properties.contains("config-version")) {properties.setProperty("config-version", cfgver);}
        if (!properties.contains("revival_block")) {
            properties.setProperty("revival_block", "minecraft:netherite_block");
        }
        if (!properties.contains("generate_ores")) {
            properties.setProperty("generate_ores", "true");
        }
        properties.store(output, null);
        parse();
        output.close();
    }

    private static void loadcfg() throws IOException {
        InputStream input = new FileInputStream(String.valueOf(FabricLoader.getInstance().getConfigDir().resolve("lifesteal.properties")));
        properties.load(input);
        input.close();
    }

    private static void parse() {
        revivalBlock = properties.getProperty("revival_block");
        generateOres = Boolean.parseBoolean(properties.getProperty("generate_ores"));
    }
}
