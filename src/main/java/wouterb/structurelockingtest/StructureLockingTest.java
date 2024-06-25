package wouterb.structurelockingtest;

import it.unimi.dsi.fastutil.longs.LongSet;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.player.PlayerBlockBreakEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructureStart;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.StructureAccessor;
import net.minecraft.world.gen.structure.Structure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;

public class StructureLockingTest implements ModInitializer {
	public static String MOD_ID = "structurelockingtest";
    public static final Logger LOGGER = LoggerFactory.getLogger("structure-locking-test");

	@Override
	public void onInitialize() {
		PlayerBlockBreakEvents.BEFORE.register(StructureLockingTest::onBlockBroken);
	}


	private static boolean onBlockBroken(World _world, PlayerEntity player, BlockPos pos, BlockState state, BlockEntity blockEntity) {
		if (player instanceof ServerPlayerEntity serverPlayer){
			ServerWorld world = serverPlayer.getServerWorld();
			StructureAccessor structureAccessor = world.getStructureAccessor();
			System.out.println(Arrays.toString(Thread.currentThread().getStackTrace()));
	//        System.out.println("Player pos : blockpos - " + player.getBlockPos() + " : " + blockPos);

			Map<Structure, LongSet> structureMap = structureAccessor.getStructureReferences(pos);
	//        System.out.println(structureMap.keySet().size());
			for (Structure structure : structureMap.keySet()) {

				StructureStart structureStart = structureAccessor.getStructureContaining(pos, structure);

				if (structureStart != StructureStart.DEFAULT) {
					// Cancel the block breaking event if the block is within a structure
	//                player.sendMessage(Text.literal("You cannot break blocks inside structures!"), false);
					return false;
				}
			}
		}
		return true;
	}
}