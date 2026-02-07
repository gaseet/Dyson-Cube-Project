package com.buuz135.dysoncubeproject.compat.fluxnetworks;

import com.buuz135.dysoncubeproject.Config;
import com.buuz135.dysoncubeproject.block.tile.RayReceiverBlockEntity;
import com.buuz135.dysoncubeproject.world.DysonSphereStructure;
import com.buuz135.dysoncubeproject.world.DysonSphereProgressSavedData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
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
        Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide()) return 0;
        BlockPos pos = blockEntity.getBlockPos();
        if (!level.isDay() || level.isRaining() || !level.canSeeSky(pos.above())) return 0;
        String sphereId = blockEntity.getDysonSphereId();
        if (sphereId == null || sphereId.isEmpty()) return 0;

        var dysonData = DysonSphereProgressSavedData.get(level);
        DysonSphereStructure sphere = dysonData.getSpheres().get(sphereId);
        if (sphere == null) return 0;

        long toExtract = Math.min(maxExtract, Config.RAY_RECEIVER_EXTRACT_POWER);
        if (simulate) {
            return Math.min(toExtract, sphere.getStoredPower());
        }
        return sphere.extractPower(toExtract);
    }

    @Override
    public long getEnergyStoredL() {
        Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide()) return blockEntity.getEnergyStorageComponent().getEnergyStored();
        String sphereId = blockEntity.getDysonSphereId();
        if (sphereId == null || sphereId.isEmpty()) return blockEntity.getEnergyStorageComponent().getEnergyStored();

        var dysonData = DysonSphereProgressSavedData.get(level);
        DysonSphereStructure sphere = dysonData.getSpheres().get(sphereId);
        if (sphere == null) return blockEntity.getEnergyStorageComponent().getEnergyStored();
        return sphere.getStoredPower();
    }

    @Override
    public long getMaxEnergyStoredL() {
        Level level = blockEntity.getLevel();
        if (level == null || level.isClientSide()) return blockEntity.getEnergyStorageComponent().getMaxEnergyStored();
        String sphereId = blockEntity.getDysonSphereId();
        if (sphereId == null || sphereId.isEmpty()) return blockEntity.getEnergyStorageComponent().getMaxEnergyStored();

        var dysonData = DysonSphereProgressSavedData.get(level);
        DysonSphereStructure sphere = dysonData.getSpheres().get(sphereId);
        if (sphere == null) return blockEntity.getEnergyStorageComponent().getMaxEnergyStored();
        long maxPower = (long) sphere.getSolarPanels() * Config.POWER_PER_SAIL;
        return Math.max(maxPower, blockEntity.getEnergyStorageComponent().getMaxEnergyStored());
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
