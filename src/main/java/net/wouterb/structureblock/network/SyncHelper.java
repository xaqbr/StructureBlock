package net.wouterb.structureblock.network;

import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.InventoryS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.collection.DefaultedList;

public class SyncHelper {
    public static void updateInventory(ServerPlayerEntity player) {
        ScreenHandler screenHandler = player.currentScreenHandler;

        DefaultedList<ItemStack> updatedStacks = DefaultedList.ofSize(screenHandler.slots.size(), ItemStack.EMPTY);
        for (int i = 0; i < updatedStacks.size(); i++) {
            updatedStacks.set(i, screenHandler.getSlot(i).getStack());
        }

        InventoryS2CPacket inventoryUpdatePacket = new InventoryS2CPacket(screenHandler.syncId, screenHandler.nextRevision(), updatedStacks, ItemStack.EMPTY);
        player.networkHandler.sendPacket(inventoryUpdatePacket);
    }
}
