package net.wouterb.structureblock.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.gen.structure.Structure;
import net.wouterb.structureblock.permissions.PermissionManager;


public class BlockInfoCommand {

    public static void register(CommandDispatcher<ServerCommandSource> serverCommandSourceCommandDispatcher, CommandRegistryAccess commandRegistryAccess, CommandManager.RegistrationEnvironment registrationEnvironment) {
        LiteralArgumentBuilder<ServerCommandSource> command = CommandManager.literal("sb").requires(source -> source.hasPermissionLevel(2));

        var commandBlockInfo = CommandManager.literal("block_info").requires(source -> source.hasPermissionLevel(2))
                .executes(context -> run(context.getSource()));

        command.then(commandBlockInfo);
        serverCommandSourceCommandDispatcher.register(command);
    }


    private static int run(ServerCommandSource source) throws CommandSyntaxException {
        ServerPlayerEntity player = (ServerPlayerEntity) source.getEntity();
        if (player == null) {
            System.out.println("Player is null!");
            return 1;
        }
        BlockState targetedBlockState = getPlayerTargetedBlock((PlayerEntity) player, 20f);

        String blockId = "None";
        Structure structure = PermissionManager.getStructure(player.getServerWorld(), player.getBlockPos());
        String structureId = PermissionManager.getStructureId(player.getServerWorld(), structure);

        if (structureId == null) structureId = "None";

        if (targetedBlockState != null)
            blockId = Registries.BLOCK.getId(targetedBlockState.getBlock()).toString();

        sendClickableMessage(player, "Targeted block: ", blockId);
        sendClickableMessage(player, "Currently inside structure: ", structureId);

        return 1;
    }

    private static BlockState getPlayerTargetedBlock(PlayerEntity player, double maxDistance){
        World world = player.getWorld();

        // Get the player's eye position and looking direction
        Vec3d eyePos = player.getCameraPosVec(1.0F);
        Vec3d lookVec = player.getRotationVec(1.0F);
        Vec3d maxReachVec = eyePos.add(lookVec.x * maxDistance, lookVec.y * maxDistance, lookVec.z * maxDistance);

        // Perform ray tracing
        RaycastContext raycastContext = new RaycastContext(
                eyePos,
                maxReachVec,
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                player
        );
        BlockHitResult hitResult = world.raycast(raycastContext);

        // Check if the hit result is a block
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            return world.getBlockState(hitResult.getBlockPos());
        }

        // If no block was hit, return null or an empty block position
        return null;
    }

    private static void sendClickableMessage(ServerPlayerEntity player, String message, String copyText) {
        // Create the main part of the message
        Text mainMessage = Text.literal(message).formatted(Formatting.WHITE); // Formatting is optional

        // Create the clickable part of the message
        Text clickableText = Text.literal("[" + copyText + "]")
                .formatted(Formatting.GREEN)
                .styled(style -> style.withClickEvent(new ClickEvent(ClickEvent.Action.COPY_TO_CLIPBOARD, copyText))
                        .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Click to copy"))));

        // Combine the main message and the clickable text
        Text finalMessage = mainMessage.copy().append(clickableText);

        // Send the message to the player
        player.sendMessage(finalMessage);
    }
}
