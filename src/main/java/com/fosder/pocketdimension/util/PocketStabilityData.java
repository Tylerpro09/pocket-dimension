package com.fosder.pocketdimension.util;

import com.fosder.pocketdimension.setup.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

/**
 * Sistema profesional de energía/estabilidad para la dimensión bolsillo.
 *
 * No depende de tiempo real del PC: usa gameTime de Minecraft.
 * - 20 ticks = 1 segundo de Minecraft.
 * - Cada intervalo configurado consume energía si la máquina está vinculada.
 * - Guarda datos persistentes en NBT del jugador para que funcione en mundos y servidores.
 */
public final class PocketStabilityData {
    private PocketStabilityData() {}

    private static final String ROOT = "PocketDimensionStability";
    private static final String ENERGY = "Energy";
    private static final String CAPACITY = "Capacity";
    private static final String SIZE_LEVEL = "SizeLevel";
    private static final String SECURITY_LEVEL = "SecurityLevel";
    private static final String ENERGY_LEVEL = "EnergyLevel";
    private static final String MACHINE_LINKED = "MachineLinked";
    private static final String MACHINE_DIM = "MachineDimension";
    private static final String MACHINE_X = "MachineX";
    private static final String MACHINE_Y = "MachineY";
    private static final String MACHINE_Z = "MachineZ";
    private static final String LAST_GAME_TIME = "LastGameTime";
    private static final String DRAIN_BUFFER = "DrainBuffer";
    private static final String ACTIVE_TICKS = "ActiveTicks";
    private static final String LAST_WARN_LEVEL = "LastWarnLevel";
    private static final String DARK_COLLAPSE_ACTIVE = "DarkCollapseActive";
    private static final String DARK_COLLAPSE_STAGE = "DarkCollapseStage";
    private static final String LAST_DARK_COLLAPSE_TICK = "LastDarkCollapseTick";

    /** 10 segundos de Minecraft por ciclo. */
    public static final int DRAIN_INTERVAL_TICKS = 20 * 10;

    public static CompoundNBT root(PlayerEntity player) {
        CompoundNBT persistent = player.getPersistentData();
        if (!persistent.contains(ROOT)) {
            CompoundNBT tag = new CompoundNBT();
            tag.putInt(ENERGY, 2500);
            tag.putInt(CAPACITY, 5000);
            tag.putInt(SIZE_LEVEL, 1);
            tag.putInt(SECURITY_LEVEL, 1);
            tag.putInt(ENERGY_LEVEL, 1);
            tag.putBoolean(MACHINE_LINKED, false);
            tag.putLong(LAST_GAME_TIME, player.level != null ? player.level.getGameTime() : 0L);
            tag.putInt(DRAIN_BUFFER, 0);
            tag.putLong(ACTIVE_TICKS, 0L);
            tag.putInt(LAST_WARN_LEVEL, 100);
            tag.putBoolean(DARK_COLLAPSE_ACTIVE, false);
            tag.putInt(DARK_COLLAPSE_STAGE, 0);
            tag.putLong(LAST_DARK_COLLAPSE_TICK, 0L);
            persistent.put(ROOT, tag);
        }
        return persistent.getCompound(ROOT);
    }

    /** Se llama desde PlayerTickEvent. Esta es la parte “en tiempo de Minecraft”. */
    public static void serverTick(PlayerEntity player) {
        if (player == null || player.level == null || player.level.isClientSide) return;

        CompoundNBT tag = root(player);
        long now = player.level.getGameTime();
        long last = tag.getLong(LAST_GAME_TIME);

        if (last <= 0L || now < last) {
            tag.putLong(LAST_GAME_TIME, now);
            return;
        }

        long delta = now - last;
        if (delta <= 0L) return;

        // Evita drenajes absurdos si el mundo se pausó/cargó de forma extraña.
        delta = Math.min(delta, 20L * 60L * 10L);
        tag.putLong(LAST_GAME_TIME, now);

        if (!isMachineLinked(player)) return;

        tag.putLong(ACTIVE_TICKS, Math.max(0L, tag.getLong(ACTIVE_TICKS) + delta));

        int buffer = tag.getInt(DRAIN_BUFFER) + (int) delta;
        int cycles = buffer / DRAIN_INTERVAL_TICKS;
        buffer = buffer % DRAIN_INTERVAL_TICKS;
        tag.putInt(DRAIN_BUFFER, buffer);

        if (cycles > 0) {
            int totalDrain = cycles * getDrainPerCycle(player);
            addEnergy(player, -totalDrain);
        }
    }

    public static void linkMachine(PlayerEntity player, World world, BlockPos pos) {
        CompoundNBT tag = root(player);
        tag.putBoolean(MACHINE_LINKED, true);
        tag.putString(MACHINE_DIM, world.dimension().location().toString());
        tag.putInt(MACHINE_X, pos.getX());
        tag.putInt(MACHINE_Y, pos.getY());
        tag.putInt(MACHINE_Z, pos.getZ());
        tag.putLong(LAST_GAME_TIME, world.getGameTime());
        tag.putInt(DRAIN_BUFFER, 0);
    }

    public static int getEnergy(PlayerEntity player) {
        return Math.max(0, root(player).getInt(ENERGY));
    }

    public static int getCapacity(PlayerEntity player) {
        CompoundNBT tag = root(player);
        int capacity = tag.getInt(CAPACITY);
        if (capacity <= 0) {
            capacity = 5000;
            tag.putInt(CAPACITY, capacity);
        }
        return capacity;
    }

    public static int getSizeLevel(PlayerEntity player) {
        return Math.max(1, root(player).getInt(SIZE_LEVEL));
    }

    public static int getSecurityLevel(PlayerEntity player) {
        return Math.max(1, root(player).getInt(SECURITY_LEVEL));
    }

    public static int getEnergyLevel(PlayerEntity player) {
        return Math.max(1, root(player).getInt(ENERGY_LEVEL));
    }

    public static boolean isMachineLinked(PlayerEntity player) {
        return root(player).getBoolean(MACHINE_LINKED);
    }

    public static void setMachineLinked(PlayerEntity player, boolean linked) {
        root(player).putBoolean(MACHINE_LINKED, linked);
    }

    public static int getDrainPerCycle(PlayerEntity player) {
        int sizeCost = Math.max(0, getSizeLevel(player) - 1) * 4;
        int securityReduction = Math.max(0, getSecurityLevel(player) - 1) * 2;
        return Math.max(4, 12 + sizeCost - securityReduction);
    }

    public static int getDrainBuffer(PlayerEntity player) {
        return Math.max(0, root(player).getInt(DRAIN_BUFFER));
    }

    public static int getTicksUntilNextDrain(PlayerEntity player) {
        if (!isMachineLinked(player)) return DRAIN_INTERVAL_TICKS;
        return Math.max(0, DRAIN_INTERVAL_TICKS - getDrainBuffer(player));
    }

    public static int getSecondsUntilNextDrain(PlayerEntity player) {
        return Math.max(0, getTicksUntilNextDrain(player) / 20);
    }

    public static long getActiveTicks(PlayerEntity player) {
        return Math.max(0L, root(player).getLong(ACTIVE_TICKS));
    }

    public static int getActiveMinutes(PlayerEntity player) {
        return (int) Math.min(Integer.MAX_VALUE, getActiveTicks(player) / (20L * 60L));
    }

    public static int getTimeToEmptySeconds(PlayerEntity player) {
        if (!isMachineLinked(player)) return -1;
        int drain = Math.max(1, getDrainPerCycle(player));
        int cycles = getEnergy(player) / drain;
        int ticks = Math.max(0, cycles * DRAIN_INTERVAL_TICKS + getTicksUntilNextDrain(player));
        return ticks / 20;
    }

    public static int getStabilityPercent(PlayerEntity player) {
        if (!isMachineLinked(player)) return 0;
        int capacity = Math.max(1, getCapacity(player));
        int energy = Math.max(0, getEnergy(player));
        int raw = (int) Math.round((energy * 100.0D) / capacity);
        int securityBonus = Math.max(0, getSecurityLevel(player) - 1) * 4;
        return Math.max(0, Math.min(100, raw + securityBonus));
    }

    public static int getStateCode(PlayerEntity player) {
        if (!isMachineLinked(player)) return 0; // sin vínculo
        int stability = getStabilityPercent(player);
        if (stability >= 80) return 4; // estable
        if (stability >= 45) return 3; // degradando
        if (stability >= 15) return 2; // crítica
        if (stability > 0) return 1; // emergencia
        return -1; // apagada
    }

    public static ITextComponent getState(PlayerEntity player) {
        switch (getStateCode(player)) {
            case 4: return new TranslationTextComponent("screen.pocketdimension.state.stable");
            case 3: return new TranslationTextComponent("screen.pocketdimension.state.degrading");
            case 2: return new TranslationTextComponent("screen.pocketdimension.state.critical");
            case 1: return new TranslationTextComponent("screen.pocketdimension.state.emergency");
            case -1: return new TranslationTextComponent("screen.pocketdimension.state.off");
            default: return new TranslationTextComponent("screen.pocketdimension.state.unlinked");
        }
    }

    public static int addEnergy(PlayerEntity player, int amount) {
        CompoundNBT tag = root(player);
        int capacity = getCapacity(player);
        int next = Math.max(0, Math.min(capacity, tag.getInt(ENERGY) + amount));
        tag.putInt(ENERGY, next);

        // Si el jugador logra recargar antes de perder el estabilizador, el colapso oscuro se detiene.
        if (next > 0) {
            tag.putBoolean(DARK_COLLAPSE_ACTIVE, false);
            tag.putInt(DARK_COLLAPSE_STAGE, 0);
        }
        return next;
    }

    public static boolean hasEnergy(PlayerEntity player, int amount) {
        if (amount <= 0) return true;
        return getEnergy(player) >= amount;
    }

    public static boolean tryConsumeEnergy(PlayerEntity player, int amount) {
        if (amount <= 0) return true;
        if (!hasEnergy(player, amount)) return false;
        addEnergy(player, -amount);
        return true;
    }

    public static void addCapacity(PlayerEntity player, int amount) {
        CompoundNBT tag = root(player);
        int next = Math.max(5000, tag.getInt(CAPACITY) + amount);
        tag.putInt(CAPACITY, next);
    }

    public static void upgradeSize(PlayerEntity player) {
        CompoundNBT tag = root(player);
        int level = Math.min(5, Math.max(1, tag.getInt(SIZE_LEVEL)) + 1);
        tag.putInt(SIZE_LEVEL, level);
        addCapacity(player, 1500);
    }

    public static void upgradeSecurity(PlayerEntity player) {
        CompoundNBT tag = root(player);
        int level = Math.min(5, Math.max(1, tag.getInt(SECURITY_LEVEL)) + 1);
        tag.putInt(SECURITY_LEVEL, level);
        addCapacity(player, 1000);
    }

    public static void upgradeEnergy(PlayerEntity player) {
        CompoundNBT tag = root(player);
        int level = Math.max(1, tag.getInt(ENERGY_LEVEL)) + 1;
        tag.putInt(ENERGY_LEVEL, level);
        addCapacity(player, 2500 + Math.min(25000, level * 250));
    }

    public static String getMachineDimensionId(PlayerEntity player) {
        return root(player).getString(MACHINE_DIM);
    }

    public static RegistryKey<World> getMachineDimensionKey(PlayerEntity player) {
        String id = getMachineDimensionId(player);
        if (id == null || id.isEmpty()) return null;
        return RegistryKey.create(Registry.DIMENSION_REGISTRY, new ResourceLocation(id));
    }

    public static BlockPos getMachinePos(PlayerEntity player) {
        CompoundNBT tag = root(player);
        return new BlockPos(tag.getInt(MACHINE_X), tag.getInt(MACHINE_Y), tag.getInt(MACHINE_Z));
    }

    public static boolean isDarkCollapseActive(PlayerEntity player) {
        return root(player).getBoolean(DARK_COLLAPSE_ACTIVE);
    }

    public static void setDarkCollapseActive(PlayerEntity player, boolean active) {
        CompoundNBT tag = root(player);
        tag.putBoolean(DARK_COLLAPSE_ACTIVE, active);
        if (!active) {
            tag.putInt(DARK_COLLAPSE_STAGE, 0);
            tag.putLong(LAST_DARK_COLLAPSE_TICK, 0L);
        }
    }

    public static int getDarkCollapseStage(PlayerEntity player) {
        return Math.max(0, root(player).getInt(DARK_COLLAPSE_STAGE));
    }

    public static int advanceDarkCollapseStage(PlayerEntity player) {
        CompoundNBT tag = root(player);
        int next = Math.min(64, Math.max(0, tag.getInt(DARK_COLLAPSE_STAGE)) + 1);
        tag.putInt(DARK_COLLAPSE_STAGE, next);
        tag.putLong(LAST_DARK_COLLAPSE_TICK, player.level != null ? player.level.getGameTime() : 0L);
        tag.putBoolean(DARK_COLLAPSE_ACTIVE, true);
        return next;
    }

    public static long getLastDarkCollapseTick(PlayerEntity player) {
        return Math.max(0L, root(player).getLong(LAST_DARK_COLLAPSE_TICK));
    }

    public static int fuelValue(ItemStack stack) {
        if (stack.isEmpty()) return 0;
        if (stack.getItem() == Items.ENDER_PEARL) return 100;
        if (stack.getItem() == Items.ENDER_EYE) return 250;
        if (stack.getItem() == Items.QUARTZ) return 80;
        if (stack.getItem() == Items.REDSTONE) return 50;
        if (stack.getItem() == Items.END_CRYSTAL) return 1500;
        if (stack.getItem() == Items.NETHER_STAR) return 10000;
        if (stack.getItem() == ModItems.STABILIZER_BATTERY.get()) return 3500;
        if (stack.getItem() == ModItems.STABILIZER_BATTERY_ORE_ITEM.get()) return 800;
        return 0;
    }
}
