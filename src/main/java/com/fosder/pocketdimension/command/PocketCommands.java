package com.fosder.pocketdimension.command;

import com.fosder.pocketdimension.setup.ModBlocks;
import com.fosder.pocketdimension.setup.ModItems;
import com.fosder.pocketdimension.tile.StabilizerMachineTileEntity;
import com.fosder.pocketdimension.util.PocketConfig;
import com.fosder.pocketdimension.util.PocketStabilityData;
import com.fosder.pocketdimension.util.PocketText;
import com.fosder.pocketdimension.util.PocketTeleport;
import com.fosder.pocketdimension.world.PocketDarkCollapseEngine;
import com.fosder.pocketdimension.world.PocketDimension;
import com.fosder.pocketdimension.world.PocketMath;
import com.fosder.pocketdimension.world.PocketOverworldHellEngine;
import com.fosder.pocketdimension.world.PocketPortalEngine;
import com.fosder.pocketdimension.world.PocketWorldBuilder;
import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;

public final class PocketCommands {
    private PocketCommands() {}

    public static void register(CommandDispatcher<CommandSource> dispatcher) {
        dispatcher.register(Commands.literal("pocketdim")
                .then(Commands.literal("info").executes(ctx -> info(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("home").executes(ctx -> home(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("return").executes(ctx -> ret(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("force")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> forceStop(ctx.getSource())))
                .then(Commands.literal("rebuild")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> rebuild(ctx.getSource().getPlayerOrException())))
                .then(Commands.literal("test")
                        .requires(src -> src.hasPermission(2))
                        .executes(ctx -> testHelp(ctx.getSource().getPlayerOrException()))
                        .then(Commands.literal("help").executes(ctx -> testHelp(ctx.getSource().getPlayerOrException())))
                        .then(Commands.literal("kit").executes(ctx -> testKit(ctx.getSource().getPlayerOrException())))
                        .then(Commands.literal("pocket").executes(ctx -> testPocket(ctx.getSource().getPlayerOrException())))
                        .then(Commands.literal("machine").executes(ctx -> testMachine(ctx.getSource().getPlayerOrException())))
                        .then(Commands.literal("charge").executes(ctx -> testCharge(ctx.getSource().getPlayerOrException())))
                        .then(Commands.literal("drain").executes(ctx -> testDrain(ctx.getSource().getPlayerOrException())))
                        .then(Commands.literal("rift").executes(ctx -> testRift(ctx.getSource().getPlayerOrException())))
                        .then(Commands.literal("gas")
                                .executes(ctx -> testGas(ctx.getSource().getPlayerOrException()))
                                .then(Commands.literal("rapidodisimo").executes(ctx -> testGasRapid(ctx.getSource().getPlayerOrException()))))
                        .then(Commands.literal("collapse").executes(ctx -> testCollapse(ctx.getSource().getPlayerOrException())))
                        .then(Commands.literal("status").executes(ctx -> testStatus(ctx.getSource().getPlayerOrException())))));
    }

    private static int info(ServerPlayerEntity player) {
        BlockPos center = PocketMath.centerFor(player.getUUID());
        PocketText.send(player, "message.pocketdimension.command.info",
                        center.getX(), center.getY(), center.getZ(), PocketConfig.getPocketSize(), PocketConfig.getPocketSize());
        return 1;
    }

    private static int home(ServerPlayerEntity player) {
        CompoundNBT temp = new CompoundNBT();
        return PocketTeleport.enterPocket(player, player.getUUID(), temp) ? 1 : 0;
    }

    private static int ret(ServerPlayerEntity player) {
        CompoundNBT temp = new CompoundNBT();
        return PocketTeleport.returnBack(player, temp) ? 1 : 0;
    }

    private static int forceStop(CommandSource source) {
        MinecraftServer server = source.getServer();
        ServerWorld overworld = server.getLevel(net.minecraft.world.World.OVERWORLD);
        PocketOverworldHellEngine.forceStop(overworld);
        source.sendSuccess(new TranslationTextComponent("message.pocketdimension.command.hell_gas_stopped"), true);
        return 1;
    }

    private static int rebuild(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return 0;
        ServerWorld pocket = server.getLevel(PocketDimension.POCKET_WORLD);
        if (pocket == null) {
            PocketText.send(player, "message.pocketdimension.world_missing");
            return 0;
        }
        PocketWorldBuilder.rebuildPocket(pocket, player.getUUID());
        PocketText.send(player, "message.pocketdimension.command.rebuilt");
        return 1;
    }

    private static int testHelp(ServerPlayerEntity player) {
        PocketText.send(player, "message.pocketdimension.command.test_kit");
        PocketText.send(player, "message.pocketdimension.command.test_pocket");
        PocketText.send(player, "message.pocketdimension.command.test_machine");
        PocketText.send(player, "message.pocketdimension.command.test_charge");
        PocketText.send(player, "message.pocketdimension.command.test_drain");
        PocketText.send(player, "message.pocketdimension.command.test_rift");
        PocketText.send(player, "message.pocketdimension.command.test_collapse");
        PocketText.send(player, "message.pocketdimension.command.test_status");
        return 1;
    }

    private static int testKit(ServerPlayerEntity player) {
        ItemStack key = new ItemStack(ModItems.DIMENSIONAL_KEY.get());
        key.getOrCreateTag().putUUID("Owner", player.getUUID());
        player.inventory.add(key);

        player.inventory.add(new ItemStack(ModItems.STABILIZER_MACHINE_ITEM.get(), 1));
        player.inventory.add(new ItemStack(ModItems.QUANTUM_AIR_GENERATOR_ITEM.get(), 1));
        player.inventory.add(new ItemStack(ModItems.STABILIZER_BATTERY.get(), 16));
        player.inventory.add(new ItemStack(ModItems.SIZE_UPGRADE.get(), 4));
        player.inventory.add(new ItemStack(ModItems.SECURITY_UPGRADE.get(), 4));
        player.inventory.add(new ItemStack(ModItems.ENERGY_UPGRADE.get(), 8));
        player.inventory.add(new ItemStack(ModItems.DIMENSIONAL_TABLE_ITEM.get(), 1));
        player.inventory.add(new ItemStack(ModItems.VOID_STABILIZER.get(), 4));
        player.inventory.add(new ItemStack(ModItems.POCKET_CORE.get(), 2));

        PocketText.send(player, "message.pocketdimension.command.kit_delivered");
        return 1;
    }

    private static int testPocket(ServerPlayerEntity player) {
        MinecraftServer server = player.getServer();
        if (server == null) return 0;
        ServerWorld pocket = server.getLevel(PocketDimension.POCKET_WORLD);
        if (pocket == null) {
            PocketText.send(player, "message.pocketdimension.world_missing");
            return 0;
        }
        PocketWorldBuilder.rebuildPocket(pocket, player.getUUID());
        CompoundNBT temp = new CompoundNBT();
        if (!PocketTeleport.enterPocket(player, player.getUUID(), temp)) {
            return 0;
        }
        PocketText.send(player, "message.pocketdimension.command.test_pocket_done");
        return 1;
    }

    private static int testMachine(ServerPlayerEntity player) {
        BlockPos pos = player.blockPosition().relative(player.getDirection(), 2);
        if (!player.level.isEmptyBlock(pos)) {
            pos = pos.above();
        }
        player.level.setBlock(pos, ModBlocks.STABILIZER_MACHINE.get().defaultBlockState(), 3);
        TileEntity tile = player.level.getBlockEntity(pos);
        if (tile instanceof StabilizerMachineTileEntity) {
            ((StabilizerMachineTileEntity) tile).setOwner(player);
        }
        PocketStabilityData.linkMachine(player, player.level, pos);
        PocketStabilityData.addEnergy(player, PocketStabilityData.getCapacity(player));
        PocketText.send(player, "message.pocketdimension.command.test_machine_done", pos.getX(), pos.getY(), pos.getZ());
        return 1;
    }

    private static int testCharge(ServerPlayerEntity player) {
        PocketStabilityData.addEnergy(player, PocketStabilityData.getCapacity(player));
        PocketStabilityData.setDarkCollapseActive(player, false);
        PocketText.send(player, "message.pocketdimension.command.test_charge_done");
        return testStatus(player);
    }

    private static int testDrain(ServerPlayerEntity player) {
        PocketStabilityData.addEnergy(player, -PocketStabilityData.getCapacity(player));
        PocketText.send(player, "message.pocketdimension.command.test_drain_done");
        return testStatus(player);
    }

    private static int testRift(ServerPlayerEntity player) {
        CompoundNBT tag = new CompoundNBT();
        tag.putUUID("Owner", player.getUUID());
        boolean ok = PocketPortalEngine.openTemporaryRiftNearPlayer(player, player.getUUID(), tag);
        return ok ? 1 : 0;
    }

    private static int testGas(ServerPlayerEntity player) {
        return testGasMode(player, false);
    }

    private static int testGasRapid(ServerPlayerEntity player) {
        return testGasMode(player, true);
    }

    private static int testGasMode(ServerPlayerEntity player, boolean rapid) {
        if (!(player.level instanceof ServerWorld)) return 0;
        BlockPos generatorPos = player.blockPosition().relative(player.getDirection(), 2);
        if (!player.level.isEmptyBlock(generatorPos)) {
            generatorPos = generatorPos.above();
        }
        player.level.setBlock(generatorPos, ModBlocks.QUANTUM_AIR_GENERATOR.get().defaultBlockState(), 3);
        PocketOverworldHellEngine.activate((ServerWorld) player.level, generatorPos, 0, 0, rapid);
        PocketText.send(player, rapid
                ? "message.pocketdimension.command.test_gas_rapid"
                : "message.pocketdimension.command.test_gas");
        PocketText.send(player, "message.pocketdimension.command.test_generator_done",
                generatorPos.getX(), generatorPos.getY(), generatorPos.getZ());
        PocketText.send(player, "message.pocketdimension.command.test_gas_features");
        PocketText.send(player, "message.pocketdimension.command.test_gas_hint");
        return 1;
    }

    private static int testCollapse(ServerPlayerEntity player) {
        if (!PocketStabilityData.isMachineLinked(player)) {
            testMachine(player);
        }
        PocketStabilityData.addEnergy(player, -PocketStabilityData.getCapacity(player));
        PocketStabilityData.setDarkCollapseActive(player, true);
        PocketText.send(player, "message.pocketdimension.command.test_collapse_done");
        PocketDarkCollapseEngine.tickPlayer(player);
        return testStatus(player);
    }

    private static int testStatus(ServerPlayerEntity player) {
        PocketText.send(player, "message.pocketdimension.command.status",
                PocketStabilityData.getState(player),
                PocketStabilityData.getEnergy(player),
                PocketStabilityData.getCapacity(player),
                PocketStabilityData.getStabilityPercent(player),
                PocketStabilityData.getSizeLevel(player),
                PocketStabilityData.getSecurityLevel(player));
        return 1;
    }
}
