package com.buuz135.dysoncubeproject.compat.fluxnetworks;

import com.buuz135.dysoncubeproject.DCPContent;
import com.buuz135.dysoncubeproject.block.tile.RayReceiverBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import sonar.fluxnetworks.api.FluxCapabilities;

public class FluxNetworksCompat {

    public static void registerCapabilities(RegisterCapabilitiesEvent event) {
        // Register on DOWN face only, consistent with the standard FE capability registration for this block
        event.registerBlock(FluxCapabilities.BLOCK, (level, blockPos, blockState, blockEntity, direction) -> {
            if (level instanceof ServerLevel && blockEntity instanceof RayReceiverBlockEntity rayReceiverBlockEntity && direction == Direction.DOWN) {
                return new RayReceiverFNEnergyStorage(rayReceiverBlockEntity);
            }
            return null;
        }, DCPContent.Blocks.RAY_RECEIVER_CONTROLLER.getBlock());
    }
}
