package com.buuz135.dysoncubeproject.integration;

import com.buuz135.dysoncubeproject.api.energy.ILongEnergyStorage;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.slf4j.Logger;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Integration with Flux Networks' IFNEnergyStorage capability.
 *
 * When Flux Networks is installed, this class bridges DCP's ILongEnergyStorage
 * with Flux Networks' IFNEnergyStorage so that energy transfers between the two
 * mods can exceed Integer.MAX_VALUE.
 *
 * Uses reflection and dynamic proxies to avoid a compile-time dependency on Flux Networks.
 */
public class FluxNetworksIntegration {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static final String FLUX_NETWORKS_MODID = "fluxnetworks";

    private static boolean initialized;
    private static boolean available;

    // Flux Networks capability reference (BlockCapability<IFNEnergyStorage, Direction>)
    @SuppressWarnings("rawtypes")
    private static BlockCapability fnBlockCapability;

    // IFNEnergyStorage class and methods
    private static Class<?> ifnEnergyStorageClass;
    private static Method receiveEnergyLMethod;
    private static Method extractEnergyLMethod;
    private static Method getEnergyStoredLMethod;
    private static Method getMaxEnergyStoredLMethod;
    private static Method canReceiveMethod;
    private static Method canExtractMethod;

    private FluxNetworksIntegration() {
    }

    /**
     * Initialize the integration. Safe to call multiple times.
     * Must be called before any other methods.
     */
    public static void init() {
        if (initialized) return;
        initialized = true;

        if (!ModList.get().isLoaded(FLUX_NETWORKS_MODID)) {
            LOGGER.debug("Flux Networks not detected, skipping integration");
            available = false;
            return;
        }

        try {
            // Load Flux Networks capability class and get the BLOCK capability field
            Class<?> fluxCapabilitiesClass = Class.forName("sonar.fluxnetworks.api.FluxCapabilities");
            fnBlockCapability = (BlockCapability) fluxCapabilitiesClass.getField("BLOCK").get(null);

            // Load IFNEnergyStorage interface and cache its methods
            ifnEnergyStorageClass = Class.forName("sonar.fluxnetworks.api.energy.IFNEnergyStorage");
            receiveEnergyLMethod = ifnEnergyStorageClass.getMethod("receiveEnergyL", long.class, boolean.class);
            extractEnergyLMethod = ifnEnergyStorageClass.getMethod("extractEnergyL", long.class, boolean.class);
            getEnergyStoredLMethod = ifnEnergyStorageClass.getMethod("getEnergyStoredL");
            getMaxEnergyStoredLMethod = ifnEnergyStorageClass.getMethod("getMaxEnergyStoredL");
            canReceiveMethod = ifnEnergyStorageClass.getMethod("canReceive");
            canExtractMethod = ifnEnergyStorageClass.getMethod("canExtract");

            available = true;
            LOGGER.info("Flux Networks integration initialized successfully - long energy transfers enabled");
        } catch (Exception e) {
            LOGGER.warn("Failed to initialize Flux Networks integration, long transfers to FN blocks will be unavailable", e);
            available = false;
        }
    }

    /**
     * @return true if Flux Networks is loaded and integration is active.
     */
    public static boolean isAvailable() {
        return available;
    }

    /**
     * Try to send energy to a block using Flux Networks' IFNEnergyStorage capability.
     *
     * @param level     the level
     * @param pos       position of the target block
     * @param direction the side to access
     * @param amount    maximum energy to send
     * @param simulate  if true, only simulate the transfer
     * @return the amount of energy accepted, or -1 if the FN capability is not available on the target
     */
    public static long trySendEnergy(Level level, BlockPos pos, Direction direction, long amount, boolean simulate) {
        if (!available) return -1;
        try {
            @SuppressWarnings("unchecked")
            Object storage = level.getCapability(fnBlockCapability, pos, direction);
            if (storage == null) return -1;
            if (!(boolean) canReceiveMethod.invoke(storage)) return 0;
            return (long) receiveEnergyLMethod.invoke(storage, amount, simulate);
        } catch (Exception e) {
            LOGGER.debug("Error during Flux Networks energy send", e);
            return -1;
        }
    }

    /**
     * Try to extract energy from a block using Flux Networks' IFNEnergyStorage capability.
     *
     * @param level     the level
     * @param pos       position of the target block
     * @param direction the side to access
     * @param amount    maximum energy to extract
     * @param simulate  if true, only simulate the transfer
     * @return the amount of energy extracted, or -1 if the FN capability is not available on the target
     */
    public static long tryExtractEnergy(Level level, BlockPos pos, Direction direction, long amount, boolean simulate) {
        if (!available) return -1;
        try {
            @SuppressWarnings("unchecked")
            Object storage = level.getCapability(fnBlockCapability, pos, direction);
            if (storage == null) return -1;
            if (!(boolean) canExtractMethod.invoke(storage)) return 0;
            return (long) extractEnergyLMethod.invoke(storage, amount, simulate);
        } catch (Exception e) {
            LOGGER.debug("Error during Flux Networks energy extract", e);
            return -1;
        }
    }

    /**
     * Creates a dynamic proxy that implements Flux Networks' IFNEnergyStorage interface,
     * delegating to a DCP ILongEnergyStorage instance.
     *
     * @param storage the DCP long energy storage to wrap
     * @return a proxy implementing IFNEnergyStorage, or null if FN integration is not available
     */
    public static Object createFNEnergyStorageProxy(ILongEnergyStorage storage) {
        if (!available || ifnEnergyStorageClass == null) return null;
        return Proxy.newProxyInstance(
                ifnEnergyStorageClass.getClassLoader(),
                new Class<?>[]{ifnEnergyStorageClass},
                new FNEnergyStorageHandler(storage)
        );
    }

    /**
     * Register a block as a Flux Networks IFNEnergyStorage capability provider.
     * Call this during RegisterCapabilitiesEvent handling.
     *
     * @param event    the capability registration event
     * @param provider a function that returns an ILongEnergyStorage for a given block, or null
     * @param blocks   the blocks to register
     */
    @SuppressWarnings("unchecked")
    public static void registerBlockProvider(RegisterCapabilitiesEvent event,
                                             FNBlockProvider provider,
                                             net.minecraft.world.level.block.Block... blocks) {
        if (!available) return;
        try {
            event.registerBlock(fnBlockCapability, (level, pos, state, entity, direction) -> {
                ILongEnergyStorage storage = provider.getStorage(level, pos, state, entity, direction);
                if (storage == null) return null;
                return createFNEnergyStorageProxy(storage);
            }, blocks);
            LOGGER.debug("Registered Flux Networks capability provider for {} block(s)", blocks.length);
        } catch (Exception e) {
            LOGGER.warn("Failed to register Flux Networks capability provider", e);
        }
    }

    /**
     * Functional interface for providing ILongEnergyStorage from block context.
     */
    @FunctionalInterface
    public interface FNBlockProvider {
        ILongEnergyStorage getStorage(Level level, BlockPos pos,
                                      net.minecraft.world.level.block.state.BlockState state,
                                      net.minecraft.world.level.block.entity.BlockEntity entity,
                                      Direction direction);
    }

    /**
     * InvocationHandler that bridges IFNEnergyStorage method calls to ILongEnergyStorage.
     */
    private static class FNEnergyStorageHandler implements InvocationHandler {
        private final ILongEnergyStorage storage;

        FNEnergyStorageHandler(ILongEnergyStorage storage) {
            this.storage = storage;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return switch (method.getName()) {
                case "receiveEnergyL" -> storage.receiveLongEnergy((long) args[0], (boolean) args[1]);
                case "extractEnergyL" -> storage.extractLongEnergy((long) args[0], (boolean) args[1]);
                case "getEnergyStoredL" -> storage.getLongEnergyStored();
                case "getMaxEnergyStoredL" -> storage.getMaxLongEnergyStored();
                case "canExtract" -> storage.canExtract();
                case "canReceive" -> storage.canReceive();
                case "toString" -> "FNEnergyStorageProxy[" + storage + "]";
                case "hashCode" -> System.identityHashCode(proxy);
                case "equals" -> proxy == args[0];
                default -> null;
            };
        }
    }
}
