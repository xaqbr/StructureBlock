package net.wouterb.structureblock.permissions;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.block.BlockState;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.structure.Structure;
import net.wouterb.structureblock.config.ModConfig;
import net.wouterb.structureblock.config.ModConfigManager;
import org.apache.commons.lang3.ArrayUtils;

import java.util.Arrays;
import java.util.Map;
import java.util.regex.Pattern;

public class PermissionManager {

    public static boolean isBlockPlacementLocked(ServerWorld world, String blockId, BlockPos blockPos, ServerPlayerEntity player) {
        String[] lockedBreakingList = ModConfigManager.getLockedStructures().placement;
        return isBlockLocked(world, blockId, blockPos, lockedBreakingList, player);
    }

    public static boolean isBlockBreakingLocked(ServerWorld world, BlockState blockState, BlockPos blockPos, ServerPlayerEntity player) {
        String blockId = Registries.BLOCK.getId(blockState.getBlock()).toString();

        String[] lockedBreakingList = ModConfigManager.getLockedStructures().breaking;
        return isBlockLocked(world, blockId, blockPos, lockedBreakingList, player);
    }


    private static boolean isBlockLocked(ServerWorld world, String blockId, BlockPos blockPos, String[] lockedList, ServerPlayerEntity player) {
        if (player.isSpectator()) return false;
        if (ModConfig.getOperatorsBypassRestrictions() && player.hasPermissionLevel(2)) return false;
        if (ModConfig.getCreativeBypassRestrictions() && player.isCreative()) return false;

        if (!isBlockInsideStructure(world, blockPos))
            return false;

        Structure structure = getStructure(world, blockPos);
        if (structure == null)
            return false;

        boolean isLocked = isStructureLocked(world, structure, lockedList);
        if (isLocked)
            notifyPlayerLocked(player);

        return isLocked;
    }

    private static boolean isBlockInsideStructure(ServerWorld world, BlockPos blockPos) {
        StructureAccessor structureAccessor = world.getStructureAccessor();
        return structureAccessor.hasStructureReferences(blockPos);
    }

    public static Structure getStructure(ServerWorld world, BlockPos blockPos) {
        StructureAccessor structureAccessor = world.getStructureAccessor();
        Map<Structure, LongSet> structureMap = structureAccessor.getStructureReferences(blockPos);

        for (Structure structure : structureMap.keySet()) {
            StructureStart structureStart;
            if (ModConfig.getUseExpandedBoundingBox())
                structureStart = structureAccessor.getStructureAt(blockPos, structure);
            else
                structureStart = structureAccessor.getStructureContaining(blockPos, structure);

            if (structureStart != StructureStart.DEFAULT) {
                return structure;
            }
        }
        return null;
    }

    public static String getStructureId(ServerWorld world, Structure structure) {
        return world.getRegistryManager().get(RegistryKeys.STRUCTURE).getId(structure).toString();
    }

    private static boolean isStructureLocked(ServerWorld world, Structure structure, String[] lockedList) {
        String structureId = getStructureId(world, structure);

        String[] completeLockedList = ArrayUtils.addAll(lockedList, ModConfigManager.getLockedStructures().breaking_and_placing);
        if (Arrays.asList(completeLockedList).contains(structureId))
            return true;

        String[] tagList = Arrays.stream(completeLockedList).filter(s -> s.contains("*")).toArray(String[]::new);
        if (doesIdMatchWildcard(structureId, tagList))
            return true;

        return false;
    }

    public static boolean doesIdMatchWildcard(String structureId, String[] wildcardList) {

        for (String wildcard : wildcardList) {
            String regex = convertWildcardToRegex(wildcard);
            if (Pattern.matches(regex, structureId)) {
                return true;
            }
        }
        return false;
    }

    private static String convertWildcardToRegex(String wildcard) {
        StringBuilder regex = new StringBuilder();
        for (char c : wildcard.toCharArray()) {
            if (c == '*') {
                regex.append(".*");
            } else {
                regex.append(Pattern.quote(String.valueOf(c)));
            }
        }
        return regex.toString();
    }

    public static void notifyPlayerLocked(ServerPlayerEntity player) {
        if (!ModConfig.getNotifyPlayerOnBreak()) return;

        String message = "You are not allowed to modify blocks inside this structure!";
        player.sendMessage(Text.of(message), true);
    }
}
