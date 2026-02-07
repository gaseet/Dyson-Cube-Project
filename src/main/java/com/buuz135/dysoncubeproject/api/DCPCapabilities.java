package com.buuz135.dysoncubeproject.api;

import com.buuz135.dysoncubeproject.api.energy.ILongEnergyStorage;
import net.neoforged.neoforge.common.capabilities.Capability;
import net.neoforged.neoforge.common.capabilities.CapabilityManager;
import net.neoforged.neoforge.common.capabilities.CapabilityToken;

/**
 * Custom capabilities for Dyson Cube Project.
 * Similar to Flux Networks' FluxCapabilities.
 */
public final class DCPCapabilities {

    /**
     * Extended energy storage capability that supports Long.MAX_VALUE.
     * Only use this capability if your block can send/receive energy at rates greater than Integer.MAX_VALUE.
     * 
     * DCP will still handle standard Forge Energy implementations normally.
     * This is for advanced energy transfer between DCP blocks and compatible mods (e.g., Flux Networks).
     */
    public static final Capability<ILongEnergyStorage> LONG_ENERGY_STORAGE = 
        CapabilityManager.get(new CapabilityToken<>() {});

    private DCPCapabilities() {
    }
}
