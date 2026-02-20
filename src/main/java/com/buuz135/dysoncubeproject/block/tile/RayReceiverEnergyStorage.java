package com.buuz135.dysoncubeproject.block.tile;

import com.buuz135.dysoncubeproject.Config;
import net.neoforged.neoforge.energy.IEnergyStorage;

public class RayReceiverEnergyStorage implements IEnergyStorage {

    private final RayReceiverBlockEntity blockEntity;

    public RayReceiverEnergyStorage(RayReceiverBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        long stored = blockEntity.getStoredEnergy();
        int extracted = (int) Math.min(maxExtract, clampToInt(stored));
        if (!simulate && extracted > 0) {
            blockEntity.setStoredEnergy(stored - extracted);
        }
        return extracted;
    }

    @Override
    public int getEnergyStored() {
        return clampToInt(blockEntity.getStoredEnergy());
    }

    @Override
    public int getMaxEnergyStored() {
        return clampToInt(Config.RAY_RECEIVER_POWER_BUFFER);
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return false;
    }

    private static int clampToInt(long value) {
        return (int) Math.min(value, (long) Integer.MAX_VALUE);
    }
}
