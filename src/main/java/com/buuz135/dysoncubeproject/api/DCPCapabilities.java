package com.buuz135.dysoncubeproject.api;

import com.buuz135.dysoncubeproject.api.energy.ILongEnergyStorage;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.capabilities.BlockCapability;

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
    public static final BlockCapability<ILongEnergyStorage, Direction> LONG_ENERGY_STORAGE = 
        BlockCapability.createSided(ResourceLocation.fromNamespaceAndPath("dysoncubeproject", "long_energy_storage"), ILongEnergyStorage.class);

    private DCPCapabilities() {
    }
}
