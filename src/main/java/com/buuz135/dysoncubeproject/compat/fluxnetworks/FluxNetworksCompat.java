package com.buuz135.dysoncubeproject.compat.fluxnetworks;

import com.buuz135.dysoncubeproject.DCPContent;
import com.buuz135.dysoncubeproject.block.tile.RayReceiverBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import sonar.fluxnetworks.api.FluxCapabilities;
import sonar.fluxnetworks.api.energy.IFNEnergyStorage;

public class FluxNetworksCompat {

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Register on DOWN face and null (directionless) to ensure Flux Networks detects IFNEnergyStorage support
        event.registerBlock(FluxCapabilities.BLOCK, (level, blockPos, blockState, blockEntity, direction) -> {
            if (level instanceof ServerLevel && blockEntity instanceof RayReceiverBlockEntity rayReceiverBlockEntity && (direction == null || direction == Direction.DOWN)) {
                return new RayReceiverFNEnergyStorage(rayReceiverBlockEntity);
            }
            return null;
        }, DCPContent.Blocks.RAY_RECEIVER_CONTROLLER.getBlock());
    }

    /**
     * Attempts to push energy to a Flux Networks device via the long-based IFNEnergyStorage API.
     * @return the amount transferred, or -1 if no Flux capability was found on the target
     */
    public static long pushEnergy(Level level, BlockPos targetPos, Direction targetFace, long maxSend) {
        IFNEnergyStorage storage = level.getCapability(FluxCapabilities.BLOCK, targetPos, targetFace);
        if (storage != null && storage.canReceive()) {
            long simulated = storage.receiveEnergyL(maxSend, true);
            if (simulated > 0) {
                storage.receiveEnergyL(simulated, false);
            }
            return simulated;
        }
        return -1;
    }
}
