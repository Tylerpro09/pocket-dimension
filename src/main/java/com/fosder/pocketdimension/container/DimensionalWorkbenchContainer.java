package com.fosder.pocketdimension.container;

import com.fosder.pocketdimension.recipe.DimensionalTableRecipes;
import com.fosder.pocketdimension.setup.ModBlocks;
import com.fosder.pocketdimension.setup.ModContainers;
import com.fosder.pocketdimension.setup.ModItems;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IWorldPosCallable;

/**
 * Contenedor propio de la Mesa Dimensional.
 * No usa ContainerType.CRAFTING; por eso puede tener una pantalla GUI propia.
 */
public class DimensionalWorkbenchContainer extends Container {
    private final CraftingInventory craftSlots = new CraftingInventory(this, 3, 3);
    private final CraftResultInventory resultSlots = new CraftResultInventory();
    private final IWorldPosCallable access;

    public DimensionalWorkbenchContainer(int windowId, PlayerInventory inventory, IWorldPosCallable access) {
        super(ModContainers.DIMENSIONAL_WORKBENCH.get(), windowId);
        this.access = access;
        // Resultado dimensional.
        this.addSlot(new DimensionalResultSlot(this, this.craftSlots, this.resultSlots, 0, 124, 35));

        // Grid 3x3.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                this.addSlot(new Slot(this.craftSlots, col + row * 3, 30 + col * 18, 17 + row * 18));
            }
        }

        // Inventario del jugador.
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inventory, col + row * 9 + 9, 8 + col * 18, 88 + row * 18));
            }
        }

        // Hotbar.
        for (int col = 0; col < 9; col++) {
            this.addSlot(new Slot(inventory, col, 8 + col * 18, 146));
        }
    }

    @Override
    public void slotsChanged(IInventory inventory) {
        this.updateResult();
        super.slotsChanged(inventory);
    }

    private void updateResult() {
        ItemStack result = DimensionalTableRecipes.findResult(this.craftSlots);
        this.resultSlots.setItem(0, result);
        this.broadcastChanges();
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        return stillValid(this.access, player, ModBlocks.DIMENSIONAL_TABLE.get());
    }

    @Override
    public ItemStack quickMoveStack(PlayerEntity player, int index) {
        // Shift-click desactivado para evitar duplicaciones con recetas custom.
        return ItemStack.EMPTY;
    }

    @Override
    public void removed(PlayerEntity player) {
        super.removed(player);
        if (!player.level.isClientSide) {
            for (int i = 0; i < this.craftSlots.getContainerSize(); i++) {
                ItemStack stack = this.craftSlots.removeItemNoUpdate(i);
                if (!stack.isEmpty()) {
                    player.drop(stack, false);
                }
            }
        }
    }

    private static class DimensionalResultSlot extends Slot {
        private final DimensionalWorkbenchContainer menu;
        private final CraftingInventory craftSlots;

        public DimensionalResultSlot(DimensionalWorkbenchContainer menu, CraftingInventory craftSlots, IInventory resultSlots, int index, int x, int y) {
            super(resultSlots, index, x, y);
            this.menu = menu;
            this.craftSlots = craftSlots;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack onTake(PlayerEntity player, ItemStack stack) {
            if (!stack.isEmpty() && stack.getItem() == ModItems.DIMENSIONAL_KEY.get()) {
                stack.getOrCreateTag().putUUID("Owner", player.getUUID());
                stack.getOrCreateTag().putString("OwnerName", player.getName().getString());
            }

            DimensionalTableRecipes.consumeIngredients(this.craftSlots);
            this.menu.updateResult();
            return super.onTake(player, stack);
        }
    }
}
