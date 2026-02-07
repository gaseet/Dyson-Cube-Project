package com.buuz135.dysoncubeproject.component;

import com.buuz135.dysoncubeproject.api.energy.ILongEnergyStorage;
import com.google.common.collect.Lists;
import com.hrznstudio.titanium.api.IFactory;
import com.hrznstudio.titanium.api.client.IScreenAddon;
import com.hrznstudio.titanium.api.client.IScreenAddonProvider;
import com.hrznstudio.titanium.client.screen.addon.EnergyBarScreenAddon;
import com.hrznstudio.titanium.component.IComponentHarness;
import com.hrznstudio.titanium.container.addon.IContainerAddon;
import com.hrznstudio.titanium.container.addon.IContainerAddonProvider;
import com.hrznstudio.titanium.container.addon.IntReferenceHolderAddon;
import com.hrznstudio.titanium.container.referenceholder.FunctionReferenceHolder;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;
import net.neoforged.neoforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Energy storage component that implements BOTH standard Forge Energy (int-based)
 * and Dyson Cube Project's extended long energy interface.
 * 
 * Similar to Flux Networks' FNEnergyStorage, this allows:
 * - Long.MAX_VALUE energy transfers between compatible blocks
 * - Automatic fallback to Integer.MAX_VALUE for standard Forge Energy
 */
public class LongEnergyStorageComponent<T extends IComponentHarness> 
    implements ILongEnergyStorage, IEnergyStorage, INBTSerializable<CompoundTag>, 
               IScreenAddonProvider, IContainerAddonProvider {

    protected long energy;
    protected long capacity;
    protected long maxReceive;
    protected long maxExtract;
    
    private final int xPos;
    private final int yPos;
    protected T componentHarness;

    public LongEnergyStorageComponent(long maxCapacity, int xPos, int yPos) {
        this(maxCapacity, maxCapacity, xPos, yPos);
    }

    public LongEnergyStorageComponent(long maxCapacity, long maxIO, int xPos, int yPos) {
        this(maxCapacity, maxIO, maxIO, xPos, yPos);
    }

    public LongEnergyStorageComponent(long maxCapacity, long maxReceive, long maxExtract, int xPos, int yPos) {
        this.capacity = maxCapacity;
        this.maxReceive = maxReceive;
        this.maxExtract = maxExtract;
        this.xPos = xPos;
        this.yPos = yPos;
        this.energy = 0;
    }

    ///// FORGE ENERGY IMPLEMENTATION (int-based for compatibility) \\\\\

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return (int) Math.min(receiveLongEnergy(maxReceive, simulate), Integer.MAX_VALUE);
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return (int) Math.min(extractLongEnergy(maxExtract, simulate), Integer.MAX_VALUE);
    }

    @Override
    public int getEnergyStored() {
        return (int) Math.min(energy, Integer.MAX_VALUE);
    }

    @Override
    public int getMaxEnergyStored() {
        return (int) Math.min(capacity, Integer.MAX_VALUE);
    }

    ///// LONG ENERGY IMPLEMENTATION (for DCP and compatible mods) \\\\\

    @Override
    public long receiveLongEnergy(long maxReceive, boolean simulate) {
        if (!canReceive())
            return 0;

        long energyReceived = Math.min(capacity - energy, Math.min(this.maxReceive, maxReceive));
        if (!simulate) {
            energy += energyReceived;
            this.update();
        }
        return energyReceived;
    }

    @Override
    public long extractLongEnergy(long maxExtract, boolean simulate) {
        if (!canExtract())
            return 0;

        long energyExtracted = Math.min(energy, Math.min(this.maxExtract, maxExtract));
        if (!simulate) {
            energy -= energyExtracted;
            this.update();
        }
        return energyExtracted;
    }

    @Override
    public long getLongEnergyStored() {
        return energy;
    }

    @Override
    public long getMaxLongEnergyStored() {
        return capacity;
    }

    @Override
    public boolean canExtract() {
        return this.maxExtract > 0;
    }

    @Override
    public boolean canReceive() {
        return this.maxReceive > 0;
    }

    ///// ADDITIONAL METHODS \\\\\

    public void setEnergyStored(long energy) {
        if (energy > this.capacity) {
            this.energy = this.capacity;
        } else {
            this.energy = Math.max(energy, 0);
        }
        this.update();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putLong("energy", this.energy);
        return nbt;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        this.energy = nbt.getLong("energy");
    }

    @Override
    @Nonnull
    public List<IFactory<? extends IScreenAddon>> getScreenAddons() {
        return Lists.newArrayList(
            () -> new EnergyBarScreenAddon(xPos, yPos, this)
        );
    }

    @Override
    @Nonnull
    public List<IFactory<? extends IContainerAddon>> getContainerAddons() {
        // For long values, we need to cap to int for container sync compatibility
        // Note: The setter is intentionally a no-op to prevent client-side syncing from
        // overwriting server-side energy values when energy exceeds Integer.MAX_VALUE
        return Lists.newArrayList(
            () -> new IntReferenceHolderAddon(new FunctionReferenceHolder(
                (value) -> {}, // No-op setter to prevent client overwrite
                () -> (int) Math.min(this.getLongEnergyStored(), Integer.MAX_VALUE)
            ))
        );
    }

    public void setComponentHarness(T componentHarness) {
        this.componentHarness = componentHarness;
    }

    private void update() {
        if (this.componentHarness != null) {
            this.componentHarness.markComponentForUpdate(true);
        }
    }

    public int getX() {
        return xPos;
    }

    public int getY() {
        return yPos;
    }
}
