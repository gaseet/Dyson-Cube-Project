package com.buuz135.dysoncubeproject.compat.fluxnetworks;

import com.buuz135.dysoncubeproject.Config;
import com.buuz135.dysoncubeproject.block.tile.RayReceiverBlockEntity;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

public class RayReceiverFNEnergyStorage implements IFNEnergyStorage {

    private final RayReceiverBlockEntity blockEntity;

    public RayReceiverFNEnergyStorage(RayReceiverBlockEntity blockEntity) {
        this.blockEntity = blockEntity;
    }

    @Override
    public long receiveEnergyL(long maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public long extractEnergyL(long maxExtract, boolean simulate) {
        long stored = blockEntity.getStoredEnergy();
        long extracted = Math.min(Math.min(maxExtract, Config.RAY_RECEIVER_EXTRACT_POWER), stored);
        if (!simulate && extracted > 0) {
            blockEntity.setStoredEnergy(stored - extracted);
        }
        return extracted;
    }

    @Override
    public long getEnergyStoredL() {
        return blockEntity.getStoredEnergy();
    }

    @Override
    public long getMaxEnergyStoredL() {
        return Config.RAY_RECEIVER_POWER_BUFFER;
    }

    @Override
    public boolean canExtract() {
        return true;
    }

    @Override
    public boolean canReceive() {
        return false;
    }
}
