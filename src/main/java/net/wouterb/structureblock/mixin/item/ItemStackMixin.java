package net.wouterb.structureblock.mixin.item;

import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.ActionResult;
import net.wouterb.structureblock.events.PlaceBlockCallback;
import net.wouterb.structureblock.network.SyncHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackMixin {


    @Inject(method = "useOnBlock", at = @At("HEAD"), cancellable = true)
    public void useOnBlock(ItemUsageContext context, CallbackInfoReturnable<ActionResult> ci) {
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
