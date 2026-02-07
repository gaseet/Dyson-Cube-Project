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

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Proxy;

/**
 * Integration helper for Flux Networks compatibility.
 *
 * Flux Networks uses its own long-based energy capability ({@code IFNEnergyStorage})
 * registered at {@code fluxnetworks:fn_energy}. This class bridges DCP's energy system
 * with Flux Networks using reflection to avoid a compile-time dependency.
 */
public class FluxNetworksIntegration {

    private static final Logger LOGGER = LogUtils.getLogger();
    private static boolean initialized = false;
    private static boolean available = false;

    @SuppressWarnings("rawtypes")
    private static BlockCapability fnBlockCapability;
    private static Class<?> fnEnergyClass;
    private static MethodHandle receiveEnergyL;
    private static MethodHandle canReceiveHandle;

    private FluxNetworksIntegration() {
    }

    private static void initialize() {
        if (initialized) return;
        initialized = true;

        if (!ModList.get().isLoaded("fluxnetworks")) {
            return;
        }

        try {
            Class<?> capClass = Class.forName("sonar.fluxnetworks.api.FluxCapabilities");
            fnBlockCapability = (BlockCapability) capClass.getField("BLOCK").get(null);

            fnEnergyClass = Class.forName("sonar.fluxnetworks.api.energy.IFNEnergyStorage");
            MethodHandles.Lookup lookup = MethodHandles.publicLookup();
            receiveEnergyL = lookup.findVirtual(fnEnergyClass, "receiveEnergyL",
                    MethodType.methodType(long.class, long.class, boolean.class));
            canReceiveHandle = lookup.findVirtual(fnEnergyClass, "canReceive",
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
     * Attempts to transfer energy to a Flux Networks block using their long-based API.
     *
     * @return the amount of energy sent, or {@code -1} if the target does not support Flux Networks
     */
    @SuppressWarnings("unchecked")
    public static long transferEnergy(Level level, BlockPos pos, Direction direction, long maxTransfer) {
        if (!isAvailable()) return -1;

        try {
            Object fnStorage = level.getCapability(fnBlockCapability, pos, direction);
            if (fnStorage == null) return -1;

            boolean canRcv = (boolean) canReceiveHandle.invoke(fnStorage);
            if (!canRcv) return 0;

            return (long) receiveEnergyL.invoke(fnStorage, maxTransfer, false);
        } catch (Throwable e) {
            LOGGER.error("Error during Flux Networks energy transfer", e);
            return -1;
        }
    }

    /**
     * Registers DCP blocks as providers of the Flux Networks energy capability,
     * allowing Flux Networks blocks to pull long-based energy from DCP blocks.
     */
    @SuppressWarnings("unchecked")
    public static void registerCapabilities(RegisterCapabilitiesEvent event,
                                            net.minecraft.world.level.block.Block rayReceiverBlock) {
        if (!isAvailable()) return;

        try {
            event.registerBlock(fnBlockCapability, (level, blockPos, blockState, blockEntity, direction) -> {
                if (blockEntity instanceof com.buuz135.dysoncubeproject.block.tile.RayReceiverBlockEntity rayReceiver
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
