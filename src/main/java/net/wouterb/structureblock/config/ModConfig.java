package net.wouterb.structureblock.config;

import net.wouterb.structureblock.StructureBlock;
import net.wouterb.structureblock.util.config.BlankLine;
import net.wouterb.structureblock.util.config.Comment;
import net.wouterb.structureblock.util.config.StoreInConfig;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.Properties;

public class ModConfig {

    @BlankLine
    @StoreInConfig
    @Comment("If true, will use the structure's large bounding box")
    static boolean use_expanded_boundingbox = false;
    @StoreInConfig
    @Comment("If true, will send a message to the player when they try to break a block")
    static boolean notify_player_on_break = true;
    @StoreInConfig
    @Comment("If true, will send a message to the player when they enter a locked structure")
    static boolean notify_player_on_enter = false;
    @StoreInConfig
    @Comment("If true, operators will not be affected by restrictions")
    static boolean operators_bypass_restrictions = true;
    @StoreInConfig
    @Comment("If true, players in creative will not be affected by restrictions")
    static boolean creative_bypass_restrictions = true;


    public static boolean getUseExpandedBoundingBox() {
        return use_expanded_boundingbox;
    }

    public static boolean getNotifyPlayerOnBreak(){
        return notify_player_on_break;
    }

    public static boolean getNotifyPlayerOnEnter(){
        return notify_player_on_enter;
    }

    public static boolean getOperatorsBypassRestrictions(){
        return operators_bypass_restrictions;
    }

    public static boolean getCreativeBypassRestrictions() {
        return creative_bypass_restrictions;
    }


    //<editor-fold desc="Utility methods for ModConfig class">
    public static void load() {
        Properties properties = new Properties();
        try (FileInputStream fileInputStream = new FileInputStream(ModConfigManager.getConfigFile().getAbsolutePath())) {
            properties.load(fileInputStream);
            StructureBlock.LOGGER.info("Configuration loaded from file successfully.");
        } catch (Exception e) {
            StructureBlock.LOGGER.error("Error loading configuration from file: {}", e.getMessage());
        }

        Field[] fields = ModConfig.class.getDeclaredFields();

        for (String propertyName : properties.stringPropertyNames()) {
            String propertyValue = properties.getProperty(propertyName);

            for (Field field : fields) {
                if (field.getName().equals(propertyName)) {
                    field.setAccessible(true);
                    try {
                        setFieldValue(field, propertyValue);
                    } catch (IllegalAccessException | NumberFormatException e) {
                        System.err.println("Error setting field: " + propertyName);
                    }
                    break;
                }
            }
        }
    }

    public static void generateDefaultConfig() {
        File configFile = ModConfigManager.getConfigFile();
        configFile.getParentFile().mkdirs();
        try (FileWriter writer = new FileWriter(configFile)) {
            writer.write("# StructureBlock Config\n");
            Field[] fields = ModConfig.class.getDeclaredFields();

            for (Field field : fields) {
                field.setAccessible(true);

                if (!field.isAnnotationPresent(StoreInConfig.class)) continue;

                String fieldName = field.getName();

                if (field.isAnnotationPresent(BlankLine.class)){
                    writer.write("\n");
                }

                if (field.isAnnotationPresent(Comment.class)){
                    Comment comment = field.getAnnotation(Comment.class);
                    writer.write("# " + comment.value() + "\n");
                }
                try {
                    writeProperty(writer, fieldName, field.get(null).toString());
                } catch (IllegalAccessException e) {
                    StructureBlock.LOGGER.error("Error accessing field: {}", fieldName);
                }
            }
        } catch (IOException e) {
            StructureBlock.LOGGER.error("Error writing configuration to file: {}", e.getMessage());
        }
    }

    private static void writeProperty(FileWriter writer, String propertyName, String propertyValue) throws IOException {
        writer.write(propertyName + "=" + propertyValue + "\n");
    }

    public static void setFieldValue(String fieldName, Object value) {
        try {
            Field field = ModConfig.class.getDeclaredField(fieldName);
            ModConfig.setFieldValue(field, value);
        } catch (Exception e) {
            StructureBlock.LOGGER.error("setFieldValue: {}", e.getMessage());
        }
    }

    private static void setFieldValue(Field field, Object value) throws IllegalAccessException {
        try {
            field.setAccessible(true);
            var type = field.getType();
            if (type == int.class)
                field.setInt(null, (int) value);
            else if (type == boolean.class)
                field.setBoolean(null, Boolean.parseBoolean(value.toString()));
            else if (type == float.class)
                field.setFloat(null, Float.parseFloat(value.toString()));
            else
                field.set(null, value);
        } catch (ClassCastException e) {
            StructureBlock.LOGGER.error(String.format("Could not parse %s with value: '%s'! Using default value...", field.getName(), value));
        }
    }
    //</editor-fold>
}
