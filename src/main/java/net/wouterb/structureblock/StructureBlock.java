package net.wouterb.structureblock;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.wouterb.structureblock.command.BlockInfoCommand;
import net.wouterb.structureblock.command.ReloadCommand;
import net.wouterb.structureblock.config.ModConfigManager;
import net.wouterb.structureblock.events.PlaceBlockCallback;
import net.wouterb.structureblock.permissions.Events;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StructureBlock implements ModInitializer {
	public static String MOD_ID = "structureblock";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		ModConfigManager.registerConfig();
		registerEvents();
		registerCommands();
	}

	public static void registerEvents() {
		PlayerBlockBreakEvents.BEFORE.register(Events::onBlockBroken);
		PlaceBlockCallback.EVENT.register(Events::onBlockPlaced);
	}

	public static void registerCommands(){
		CommandRegistrationCallback.EVENT.register(BlockInfoCommand::register);
		CommandRegistrationCallback.EVENT.register(ReloadCommand::register);
	}
}