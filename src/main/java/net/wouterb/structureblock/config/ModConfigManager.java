package net.wouterb.structureblock.config;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.wouterb.structureblock.StructureBlock;

import java.io.*;
import java.nio.file.Path;

public class ModConfigManager {
    private static final Path CONFIG_DIR = Path.of(String.valueOf(FabricLoader.getInstance().getConfigDir()), StructureBlock.MOD_ID);
    private static final String CONFIG_FILE_NAME = "config.properties";
    private static final String DEFAULT_VALUES_FILE_NAME = "locked_structures.json";

    private static File configFile;
    private static File lockedStructuresFile;

    private static LockedStructures lockedStructures;


    public static File getConfigFile() {
        return configFile;
    }

    public static File getLockedStructuresFile() {
        return lockedStructuresFile;
    }

    public static void registerConfig() {
        Gson gson = new Gson();

        Path configPath = Path.of(String.valueOf(CONFIG_DIR), CONFIG_FILE_NAME);
        configFile = configPath.toFile();
        if (!configFile.exists()) {
            StructureBlock.LOGGER.info("No config found, generating one...");
            ModConfig.generateDefaultConfig();
        } else {
            ModConfig.load();
        }

        Path defaultValuesPath = Path.of(String.valueOf(CONFIG_DIR), DEFAULT_VALUES_FILE_NAME);
        lockedStructuresFile = defaultValuesPath.toFile();
        if (!lockedStructuresFile.exists()){
            StructureBlock.LOGGER.info("No locked structures file found, generating one...");
            lockedStructures = LockedStructures.generateDefaultLockedStructures();
        } else {
            try {
                FileReader reader = new FileReader(lockedStructuresFile);
                String json = JsonParser.parseReader(reader).toString();
                lockedStructures = gson.fromJson(json, LockedStructures.class);
            } catch (FileNotFoundException e) {
                lockedStructures = LockedStructures.generateDefaultLockedStructures();
                throw new RuntimeException(e);
            }
        }
    }

    public static LockedStructures getLockedStructures() {
        return lockedStructures;
    }



}
