package net.wouterb.structureblock.mixin.item;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.wouterb.structureblock.events.PlaceBlockCallback;
import net.wouterb.structureblock.network.SyncHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BlockItem.class)
public class BlockItemMixin {

    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    public void place(ItemPlacementContext context, CallbackInfoReturnable<ActionResult> ci) {
        String stackId = Registries.ITEM.getId(context.getStack().getItem()).toString();

        PlayerEntity player = context.getPlayer();
        if (player instanceof ServerPlayerEntity serverPlayer) {
            ActionResult result = PlaceBlockCallback.EVENT.invoker().interact(serverPlayer.getServerWorld(), serverPlayer, context.getBlockPos(), stackId);

            if (result == ActionResult.FAIL) {
                ci.setReturnValue(ActionResult.FAIL);
                SyncHelper.updateInventory(serverPlayer);
            }
        }
    }
}
