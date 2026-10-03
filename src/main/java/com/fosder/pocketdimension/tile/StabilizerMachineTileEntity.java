package com.fosder.pocketdimension.tile;

import com.fosder.pocketdimension.setup.ModTileEntities;
import com.fosder.pocketdimension.util.PocketStabilityData;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Inventario técnico de la Máquina Estabilizadora.
 *
 * Tiene 1 slot interno para tolvas. Si una tolva coloca una Batería Estabilizadora
 * u otro combustible dimensional, la máquina lo consume automáticamente y recarga
 * la dimensión del dueño vinculado.
 */
public class StabilizerMachineTileEntity extends TileEntity implements ISidedInventory, ITickableTileEntity {
    private static final int[] SLOTS = new int[]{0};
    private final NonNullList<ItemStack> items = NonNullList.withSize(1, ItemStack.EMPTY);
    private UUID owner;
    private String ownerName = "";

    public StabilizerMachineTileEntity() {
        super(ModTileEntities.STABILIZER_MACHINE.get());
    }

    @Override
    public void tick() {
        if (this.level == null || this.level.isClientSide) return;
        if (this.level.getGameTime() % 20L != 0L) return; // 1 vez por segundo MC.

        ItemStack stack = this.items.get(0);
        if (stack.isEmpty()) return;

        int fuel = PocketStabilityData.fuelValue(stack);
        if (fuel <= 0) return;

        ServerPlayerEntity linkedPlayer = getLinkedPlayer();
        if (linkedPlayer == null) return; // Si el dueño no está conectado, la batería queda guardada.

        int current = PocketStabilityData.getEnergy(linkedPlayer);
        int capacity = PocketStabilityData.getCapacity(linkedPlayer);
        if (current >= capacity) return; // No desperdicia combustible si está llena.

        PocketStabilityData.addEnergy(linkedPlayer, fuel);
        stack.shrink(1);
        if (stack.isEmpty()) {
            this.items.set(0, ItemStack.EMPTY);
        }
        setChanged();
    }

    @Nullable
    private ServerPlayerEntity getLinkedPlayer() {
        if (this.owner == null || !(this.level instanceof ServerWorld)) return null;
        ServerWorld serverWorld = (ServerWorld) this.level;
        return serverWorld.getServer().getPlayerList().getPlayer(this.owner);
    }

    public void setOwner(PlayerEntity player) {
        this.owner = player.getUUID();
        this.ownerName = player.getName().getString();
        setChanged();
    }

    public boolean hasOwner() {
        return this.owner != null;
    }

    @Nullable
    public UUID getOwnerUUID() {
        return this.owner;
    }

    public boolean isOwner(UUID uuid) {
        return uuid != null && this.owner != null && this.owner.equals(uuid);
    }


    public String getOwnerName() {
        return this.ownerName == null ? "" : this.ownerName;
    }

    public ITextComponent getOwnerDisplayName() {
        return this.ownerName == null || this.ownerName.isEmpty()
                ? new TranslationTextComponent("text.pocketdimension.unknown_owner")
                : new StringTextComponent(this.ownerName);
    }

    public boolean canAcceptFuel(ItemStack stack) {
        return !stack.isEmpty() && PocketStabilityData.fuelValue(stack) > 0;
    }

    public int getStoredFuelValue() {
        ItemStack stack = this.items.get(0);
        return stack.isEmpty() ? 0 : PocketStabilityData.fuelValue(stack) * stack.getCount();
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        super.save(tag);
        ItemStack stack = this.items.get(0);
        if (!stack.isEmpty()) {
            CompoundNBT itemTag = new CompoundNBT();
            stack.save(itemTag);
            tag.put("AutoFuel", itemTag);
        }
        if (this.owner != null) {
            tag.putUUID("Owner", this.owner);
            tag.putString("OwnerName", this.ownerName == null ? "" : this.ownerName);
        }
        return tag;
    }

    @Override
    public void load(net.minecraft.block.BlockState state, CompoundNBT tag) {
        super.load(state, tag);
        this.items.set(0, tag.contains("AutoFuel") ? ItemStack.of(tag.getCompound("AutoFuel")) : ItemStack.EMPTY);
        if (tag.hasUUID("Owner")) {
            this.owner = tag.getUUID("Owner");
            this.ownerName = tag.getString("OwnerName");
        } else {
            this.owner = null;
            this.ownerName = "";
        }
    }

    // ----- Inventario para tolvas -----

    @Override
    public int getContainerSize() {
        return 1;
    }

    @Override
    public boolean isEmpty() {
        return this.items.get(0).isEmpty();
    }

    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? this.items.get(0) : ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItem(int index, int count) {
        if (index != 0 || count <= 0) return ItemStack.EMPTY;
        ItemStack current = this.items.get(0);
        if (current.isEmpty()) return ItemStack.EMPTY;
        ItemStack removed = current.split(count);
        if (current.isEmpty()) this.items.set(0, ItemStack.EMPTY);
        setChanged();
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int index) {
        if (index != 0) return ItemStack.EMPTY;
        ItemStack current = this.items.get(0);
        this.items.set(0, ItemStack.EMPTY);
        return current;
    }

    @Override
    public void setItem(int index, ItemStack stack) {
        if (index != 0) return;
        if (!stack.isEmpty() && !canAcceptFuel(stack)) return;
        this.items.set(0, stack);
        if (!stack.isEmpty() && stack.getCount() > getMaxStackSize()) {
            stack.setCount(getMaxStackSize());
        }
        setChanged();
    }

    @Override
    public boolean stillValid(PlayerEntity player) {
        World world = this.level;
        BlockPos pos = this.worldPosition;
        return world != null && world.getBlockEntity(pos) == this && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return index == 0 && canAcceptFuel(stack);
    }

    @Override
    public void clearContent() {
        this.items.set(0, ItemStack.EMPTY);
        setChanged();
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return false; // La tolva no extrae: la máquina consume el combustible internamente.
    }
}
