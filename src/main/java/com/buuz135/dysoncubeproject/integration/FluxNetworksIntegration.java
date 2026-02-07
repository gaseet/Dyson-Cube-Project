package com.buuz135.dysoncubeproject.integration;

import com.buuz135.dysoncubeproject.api.energy.ILongEnergyStorage;
import com.mojang.logging.LogUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.slf4j.Logger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Proxy;

/**
 * Integration with Flux Networks ({@code SonarSonic/Flux-Networks}).
 * <p>
 * Flux Networks defines its own long-based energy API on its 1.21 branch:
 * <ul>
 *   <li>{@code sonar.fluxnetworks.api.FluxCapabilities.BLOCK} &ndash;
 *       a sided {@link BlockCapability} registered at {@code fluxnetworks:fn_energy}</li>
 *   <li>{@code sonar.fluxnetworks.api.energy.IFNEnergyStorage} &ndash;
 *       the long-based energy interface with methods
 *       {@code receiveEnergyL}, {@code extractEnergyL},
 *       {@code getEnergyStoredL}, {@code getMaxEnergyStoredL},
 *       {@code canExtract}, {@code canReceive}</li>
 * </ul>
 * <p>
 * Flux Networks' {@code FNEnergyConnector} checks adjacent blocks for
 * {@code FluxCapabilities.BLOCK} <em>first</em> (long-based), falling back to
 * standard Forge Energy (int-based) via {@code ForgeEnergyConnector}.
 * By registering our Ray Receiver as a provider of the FN capability,
 * Flux Networks Plug/Point blocks will automatically use long-based
 * transfers with DCP instead of being capped to {@code Integer.MAX_VALUE}.
 * <p>
 * All access is via reflection to avoid a compile-time dependency.
 *
 * @see <a href="https://github.com/SonarSonic/Flux-Networks/tree/1.21">Flux Networks 1.21 branch</a>
 */
public class FluxNetworksIntegration {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean initialized = false;
    private static boolean available = false;

    // FluxCapabilities.BLOCK — BlockCapability<IFNEnergyStorage, Direction>
    @SuppressWarnings("rawtypes")
    private static BlockCapability fnBlockCapability;

    // IFNEnergyStorage class reference
    private static Class<?> fnEnergyClass;

    // Cached MethodHandles for IFNEnergyStorage methods
    private static MethodHandle receiveEnergyL;  // long receiveEnergyL(long, boolean)
    private static MethodHandle extractEnergyL;  // long extractEnergyL(long, boolean)
    private static MethodHandle canReceiveHandle; // boolean canReceive()
    private static MethodHandle canExtractHandle; // boolean canExtract()

    private FluxNetworksIntegration() {
    }

    private static void initialize() {
        if (initialized) return;
        initialized = true;

        if (!ModList.get().isLoaded("fluxnetworks")) {
            return;
        }

        try {
            // sonar.fluxnetworks.api.FluxCapabilities — defines BLOCK, ITEM, ENTITY capabilities
            Class<?> capClass = Class.forName("sonar.fluxnetworks.api.FluxCapabilities");
            fnBlockCapability = (BlockCapability) capClass.getField("BLOCK").get(null);

            // sonar.fluxnetworks.api.energy.IFNEnergyStorage — long-based energy interface
            fnEnergyClass = Class.forName("sonar.fluxnetworks.api.energy.IFNEnergyStorage");
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            receiveEnergyL = lookup.findVirtual(fnEnergyClass, "receiveEnergyL",
                    MethodType.methodType(long.class, long.class, boolean.class));
            extractEnergyL = lookup.findVirtual(fnEnergyClass, "extractEnergyL",
                    MethodType.methodType(long.class, long.class, boolean.class));
            canReceiveHandle = lookup.findVirtual(fnEnergyClass, "canReceive",
                    MethodType.methodType(boolean.class));
            canExtractHandle = lookup.findVirtual(fnEnergyClass, "canExtract",
                    MethodType.methodType(boolean.class));

            available = true;
            LOGGER.info("Flux Networks integration initialized successfully");
        } catch (Exception e) {
            LOGGER.warn("Failed to initialize Flux Networks integration", e);
            available = false;
        }
    }

    public static boolean isAvailable() {
        initialize();
        return available;
    }

    /**
     * Attempts to push energy into a Flux Networks block using
     * {@code IFNEnergyStorage.receiveEnergyL(long, boolean)}.
     *
     * @return the amount of energy accepted, or {@code -1} if the target does not expose
     *         Flux Networks' {@code FluxCapabilities.BLOCK} capability
     */
    @SuppressWarnings("unchecked")
    public static long sendEnergy(Level level, BlockPos pos, Direction direction, long maxTransfer) {
        if (!isAvailable()) return -1;

        try {
            Object fnStorage = level.getCapability(fnBlockCapability, pos, direction);
            if (fnStorage == null) return -1;

            boolean canRcv = (boolean) canReceiveHandle.invoke(fnStorage);
            if (!canRcv) return 0;

            return (long) receiveEnergyL.invoke(fnStorage, maxTransfer, false);
        } catch (Throwable e) {
            LOGGER.error("Error during Flux Networks energy send", e);
            return -1;
        }
    }

    /**
     * Attempts to pull energy from a Flux Networks block using
     * {@code IFNEnergyStorage.extractEnergyL(long, boolean)}.
     *
     * @return the amount of energy extracted, or {@code -1} if the target does not expose
     *         Flux Networks' {@code FluxCapabilities.BLOCK} capability
     */
    @SuppressWarnings("unchecked")
    public static long pullEnergy(Level level, BlockPos pos, Direction direction, long maxExtract) {
        if (!isAvailable()) return -1;

        try {
            Object fnStorage = level.getCapability(fnBlockCapability, pos, direction);
            if (fnStorage == null) return -1;

            boolean canExt = (boolean) canExtractHandle.invoke(fnStorage);
            if (!canExt) return 0;

            return (long) extractEnergyL.invoke(fnStorage, maxExtract, false);
        } catch (Throwable e) {
            LOGGER.error("Error during Flux Networks energy pull", e);
            return -1;
        }
    }

    /**
     * Registers DCP blocks as providers of the Flux Networks
     * {@code FluxCapabilities.BLOCK} capability (i.e. {@code IFNEnergyStorage}).
     * <p>
     * This allows Flux Networks' {@code FNEnergyConnector} to discover and use
     * long-based energy transfers when interacting with DCP blocks, instead of
     * falling back to int-based Forge Energy via {@code ForgeEnergyConnector}.
     */
    @SuppressWarnings("unchecked")
    public static void registerCapabilities(RegisterCapabilitiesEvent event,
                                            net.minecraft.world.level.block.Block rayReceiverBlock) {
        if (!isAvailable()) return;

        try {
            event.registerBlock(fnBlockCapability, (level, blockPos, blockState, blockEntity, direction) -> {
                if (level instanceof ServerLevel
                        && blockEntity instanceof com.buuz135.dysoncubeproject.block.tile.RayReceiverBlockEntity rayReceiver
                        && direction == Direction.DOWN) {
                    return createFNProxy(rayReceiver.getEnergyStorageComponent());
                }
                return null;
            }, rayReceiverBlock);
            LOGGER.info("Registered Flux Networks capability provider for Ray Receiver");
        } catch (Exception e) {
            LOGGER.error("Failed to register Flux Networks capability provider", e);
        }
    }

    /**
     * Creates a dynamic proxy implementing {@code IFNEnergyStorage} that delegates
     * to the given {@link ILongEnergyStorage}.
     * <p>
     * Method mapping (FN → DCP):
     * <pre>
     * receiveEnergyL     → receiveLongEnergy
     * extractEnergyL     → extractLongEnergy
     * getEnergyStoredL   → getLongEnergyStored
     * getMaxEnergyStoredL→ getMaxLongEnergyStored
     * canExtract         → canExtract
     * canReceive         → canReceive
     * </pre>
     */
    private static Object createFNProxy(ILongEnergyStorage storage) {
        return Proxy.newProxyInstance(
                fnEnergyClass.getClassLoader(),
                new Class<?>[]{fnEnergyClass},
                (proxy, method, args) -> switch (method.getName()) {
                    case "receiveEnergyL" -> storage.receiveLongEnergy((long) args[0], (boolean) args[1]);
                    case "extractEnergyL" -> storage.extractLongEnergy((long) args[0], (boolean) args[1]);
                    case "getEnergyStoredL" -> storage.getLongEnergyStored();
                    case "getMaxEnergyStoredL" -> storage.getMaxLongEnergyStored();
                    case "canExtract" -> storage.canExtract();
                    case "canReceive" -> storage.canReceive();
                    default -> {
                        if ("toString".equals(method.getName())) yield "FNEnergyProxy[" + storage + "]";
                        if ("hashCode".equals(method.getName())) yield System.identityHashCode(proxy);
                        if ("equals".equals(method.getName())) yield proxy == args[0];
                        yield null;
                    }
                }
        );
    }
}
