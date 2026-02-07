package com.buuz135.dysoncubeproject.integration;

/**
 * Integration helper for Flux Networks compatibility.
 * 
 * Note: This class is provided for future integration purposes.
 * The current implementation already supports Flux Networks through the
 * ILongEnergyStorage capability which is compatible with Flux Networks'
 * IFNEnergyStorage capability.
 * 
 * To properly integrate with Flux Networks when it's installed:
 * 1. Add a soft dependency in your mod metadata
 * 2. Check if Flux Networks is loaded at runtime using ModList.get().isLoaded("fluxnetworks")
 * 3. Use reflection or a proper API dependency to access their capability
 */
public class FluxNetworksIntegration {
    
    private FluxNetworksIntegration() {
        // Utility class - no instantiation
    }
    
    // Future integration methods can be added here when needed
}
