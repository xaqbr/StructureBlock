package net.wouterb.structureblock.permissions;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class Events {

    public static boolean onBlockBroken(World world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
        if (player instanceof ServerPlayerEntity serverPlayer){
            return !PermissionManager.isBlockBreakingLocked(serverPlayer.getServerWorld(), state, pos, serverPlayer);
        }
        return true;
    }

    public static ActionResult onBlockPlaced(ServerWorld world, ServerPlayerEntity player, BlockPos blockPos, String blockId) {

        if (PermissionManager.isBlockPlacementLocked(world, blockId, blockPos, player)) {
            return ActionResult.FAIL;
        }

        return ActionResult.PASS;
    }
}
