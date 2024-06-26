package net.wouterb.structureblock.events;


import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.block.BlockState;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

public interface PlaceBlockCallback {
    Event<PlaceBlockCallback> EVENT = EventFactory.createArrayBacked(PlaceBlockCallback.class,
            (listeners) -> (world, player, blockPos, blockId) -> {
                for (PlaceBlockCallback listener : listeners) {
                    ActionResult result = listener.interact(world, player, blockPos, blockId);

                    if (result != ActionResult.PASS) {
                        return result;
                    }
                }
                return ActionResult.PASS;
    });

    ActionResult interact(ServerWorld world, ServerPlayerEntity player,  BlockPos blockPos, String blockId);
}
