package com.fosder.pocketdimension.world;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.storage.WorldSavedData;

import java.util.ArrayList;
import java.util.List;

/**
 * Persistent state for temporary dimensional rifts.
 *
 * Rifts are stored per dimension so their expiry survives a server restart
 * without keeping a process-only static list.
 */
public final class PocketRiftSavedData extends WorldSavedData {
    public static final String ID = "pocketdimension_temporary_rifts";

    private final List<Rift> rifts = new ArrayList<>();

    public PocketRiftSavedData() {
        super(ID);
    }

    public void addRift(List<BlockPos> blocks, long expiresAt) {
        long[] positions = new long[blocks.size()];
        for (int i = 0; i < blocks.size(); i++) {
            positions[i] = blocks.get(i).asLong();
        }
        rifts.add(new Rift(positions, expiresAt));
        setDirty();
    }

    public List<Rift> getRifts() {
        return rifts;
    }

    public boolean hasOtherRiftAt(BlockPos position, Rift ignored) {
        long packed = position.asLong();
        for (Rift rift : rifts) {
            if (rift == ignored) continue;
            for (long value : rift.blocks) {
                if (value == packed) return true;
            }
        }
        return false;
    }

    public void remove(Rift rift) {
        if (rifts.remove(rift)) {
            setDirty();
        }
    }

    @Override
    public void load(CompoundNBT tag) {
        rifts.clear();
        long[] packedRifts = tag.getLongArray("Rifts");
        // Kept for forward compatibility with old/invalid files; the current
        // format stores each rift as a compound in the Rifts list.
        if (packedRifts.length > 0) return;

        net.minecraft.nbt.ListNBT savedRifts = tag.getList("Rifts", 10);
        for (int i = 0; i < savedRifts.size(); i++) {
            CompoundNBT savedRift = savedRifts.getCompound(i);
            long expiresAt = savedRift.getLong("ExpiresAt");
            long[] blocks = savedRift.getLongArray("Blocks");
            if (blocks.length > 0) {
                rifts.add(new Rift(blocks, expiresAt));
            }
        }
    }

    @Override
    public CompoundNBT save(CompoundNBT tag) {
        net.minecraft.nbt.ListNBT savedRifts = new net.minecraft.nbt.ListNBT();
        for (Rift rift : rifts) {
            CompoundNBT savedRift = new CompoundNBT();
            savedRift.putLong("ExpiresAt", rift.expiresAt);
            savedRift.putLongArray("Blocks", rift.blocks);
            savedRifts.add(savedRift);
        }
        tag.put("Rifts", savedRifts);
        return tag;
    }

    public static final class Rift {
        private final long[] blocks;
        private final long expiresAt;

        private Rift(long[] blocks, long expiresAt) {
            this.blocks = blocks;
            this.expiresAt = expiresAt;
        }

        public long[] getBlocks() {
            return blocks;
        }

        public long getExpiresAt() {
            return expiresAt;
        }
    }
}
