package net.wouterb.structureblock.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.wouterb.structureblock.StructureBlock;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

public class LockedStructures {
    public final String[] breaking_and_placing;
    public final String[] breaking;
    public final String[] placement;


    public LockedStructures(){
        breaking_and_placing = new String[]{};
        breaking = new String[]{};
        placement = new String[]{};
    }

    public LockedStructures(String[] breaking, String[] placement, String[] breaking_and_placing) {
        this.breaking_and_placing = breaking_and_placing;
        this.breaking = breaking;
        this.placement = placement;
    }

    public String[] getFieldByString(String propertyName) {
        return switch (propertyName) {
            case "breaking_and_placing" -> breaking_and_placing;
            case "breaking" -> breaking;
            case "placement" -> placement;
            default -> throw new IllegalArgumentException("Invalid property name: " + propertyName);
        };
    }

    public static LockedStructures generateDefaultLockedStructures() {
        File defaultValuesFile = ModConfigManager.getLockedStructuresFile();
        try {
            Files.createFile(defaultValuesFile.toPath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        LockedStructures lockedStructures = new LockedStructures();

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        String json = gson.toJson(lockedStructures);
        try (FileWriter writer = new FileWriter(defaultValuesFile.toString())) {
            writer.write(json);
        } catch (IOException e) {
            StructureBlock.LOGGER.error(e.toString());
        }

        return lockedStructures;
    }
}