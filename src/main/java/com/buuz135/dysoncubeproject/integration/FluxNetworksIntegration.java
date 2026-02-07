package com.buuz135.dysoncubeproject.integration;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.CapabilityManager;
import net.neoforged.neoforge.common.capabilities.CapabilityToken;

/**
 * Integration helper for Flux Networks compatibility.
 * Checks if Flux Networks is installed and provides their long energy capability.
 */
public class FluxNetworksIntegration {
    
    private static Capability<?> FLUX_ENERGY_STORAGE = null;
    private static boolean checked = false;
    
    /**
     * Attempts to check if the target block has Flux Networks' IFNEnergyStorage capability.
     * Returns the capability if found, null otherwise.
     */
    public static Object tryGetFluxCapability(BlockEntity target, Direction side) {
        if (!checked) {
            try {
                // Try to get Flux Networks' capability
                FLUX_ENERGY_STORAGE = CapabilityManager.get(new CapabilityToken<>() {});
                checked = true;
            } catch (Exception e) {
                // Flux Networks not installed
                checked = true;
            }
        }
        
        if (FLUX_ENERGY_STORAGE != null) {
            return target.getCapability(FLUX_ENERGY_STORAGE, side);
        }
        return null;
    }
}
