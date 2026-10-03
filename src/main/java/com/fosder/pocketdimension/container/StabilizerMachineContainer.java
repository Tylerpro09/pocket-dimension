package com.fosder.pocketdimension.container;

import com.fosder.pocketdimension.setup.ModBlocks;
import com.fosder.pocketdimension.setup.ModContainers;
import com.fosder.pocketdimension.util.PocketStabilityData;
import com.fosder.pocketdimension.util.PocketEditorMode;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIntArray;
import net.minecraft.util.IWorldPosCallable;

/** Container sincronizado de la Máquina Estabilizadora. */
public class StabilizerMachineContainer extends Container {
    public static final int MAP_EDITOR_BUTTON = 42;
    private static final int DATA_COUNT = 12;
    private static final int MACHINE_SLOT = 0;
    private static final int PLAYER_INV_START = 1;
    private static final int PLAYER_INV_END = PLAYER_INV_START + 27;
    private static final int HOTBAR_START = PLAYER_INV_END;
    private static final int HOTBAR_END = HOTBAR_START + 9;

    private final IWorldPosCallable access;
    private final IIntArray data;
    private final IInventory machineInventory;

    public StabilizerMachineContainer(int windowId, PlayerInventory inventory, IWorldPosCallable access) {
        this(windowId, inventory, access, new Inventory(1));
    }

    public StabilizerMachineContainer(int windowId, PlayerInventory inventory, IWorldPosCallable access, IInventory machineInventory) {
        super(ModContainers.STABILIZER_MACHINE.get(), windowId);
        this.access = access;
        this.machineInventory = machineInventory == null ? new Inventory(1) : machineInventory;
        this.data = createSyncedData(inventory.player);
        this.addDataSlots(this.data);

        // Slot técnico de combustible: tolvas y jugador pueden insertar baterías/combustibles aquí.
        this.addSlot(new Slot(this.machineInventory, 0, 248, 154) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return PocketStabilityData.fuelValue(stack) > 0;
            }

            @Override
            public int getMaxStackSize() {
                return 64;
            }
        });

        // Inventario del jugador, colocado en una sección separada debajo de los paneles.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 65 + col * 18, 211 + row * 18));
            }
        }

        // Hotbar.
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, 65 + col * 18, 269));
        }
    }

    private static IIntArray createSyncedData(final PlayerEntity player) {
        final int[] cache = new int[DATA_COUNT];
        return new IIntArray() {
            @Override
            public int get(int index) {
                if (player != null && player.level != null && !player.level.isClientSide) {
                    switch (index) {
                        case 0: return PocketStabilityData.getEnergy(player);
                        case 1: return PocketStabilityData.getCapacity(player);
                        case 2: return PocketStabilityData.getStabilityPercent(player);
                        case 3: return PocketStabilityData.getSizeLevel(player);
                        case 4: return PocketStabilityData.getSecurityLevel(player);
                        case 5: return PocketStabilityData.isMachineLinked(player) ? 1 : 0;
                        case 6: return PocketStabilityData.getDrainPerCycle(player);
                        case 7: return PocketStabilityData.getSecondsUntilNextDrain(player);
                        case 8: return PocketStabilityData.getActiveMinutes(player);
                        case 9: return PocketStabilityData.getStateCode(player);
                        case 10: return PocketStabilityData.getEnergyLevel(player);
                        case 11: return PocketEditorMode.isActive(player) ? 1 : 0;
                        default: return 0;
                    }
                }
                return index >= 0 && index < cache.length ? cache[index] : 0;
            }

            @Override
            public void set(int index, int value) {
                if (index >= 0 && index < cache.length) {
                    cache[index] = value;
                }
            }

            @Override
            public int getCount() {
                return DATA_COUNT;
            }
        };
    }

    public int getEnergy() { return data.get(0); }
    public int getCapacity() { return Math.max(1, data.get(1)); }
    public int getStability() { return data.get(2); }
    public int getSizeLevel() { return data.get(3); }
    public int getSecurityLevel() { return data.get(4); }
    public boolean isLinked() { return data.get(5) == 1; }
    public int getDrainPerCycle() { return data.get(6); }
    public int getSecondsUntilNextDrain() { return data.get(7); }
    public int getActiveMinutes() { return data.get(8); }
    public int getStateCode() { return data.get(9); }
    public int getEnergyLevel() { return data.get(10); }
    public boolean isEditorMode() { return data.get(11) == 1; }

    @Override
    public boolean clickMenuButton(PlayerEntity player, int id) {
        if (id == MAP_EDITOR_BUTTON) {
            if (!player.level.isClientSide && player instanceof net.minecraft.entity.player.ServerPlayerEntity) {
                net.minecraft.entity.player.ServerPlayerEntity serverPlayer = (net.minecraft.entity.player.ServerPlayerEntity) player;
                PocketEditorMode.setActive(serverPlayer, !PocketEditorMode.isActive(serverPlayer));
            }
            return true;
        }
        return super.clickMenuButton(player, id);
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return stillValid(this.access, player, ModBlocks.STABILIZER_MACHINE.get());
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = index >= 0 && index < this.slots.size() ? this.slots.get(index) : null;
        if (slot == null || !slot.hasItem()) return ItemStack.EMPTY;

        ItemStack stack = slot.getItem();
        result = stack.copy();

        if (index == MACHINE_SLOT) {
            if (!this.moveItemStackTo(stack, PLAYER_INV_START, HOTBAR_END, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            if (PocketStabilityData.fuelValue(stack) > 0) {
                if (!this.moveItemStackTo(stack, MACHINE_SLOT, MACHINE_SLOT + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= PLAYER_INV_START && index < PLAYER_INV_END) {
                if (!this.moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= HOTBAR_START && index < HOTBAR_END) {
                if (!this.moveItemStackTo(stack, PLAYER_INV_START, PLAYER_INV_END, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                return ItemStack.EMPTY;
            }
        }

        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return result;
    }
}
