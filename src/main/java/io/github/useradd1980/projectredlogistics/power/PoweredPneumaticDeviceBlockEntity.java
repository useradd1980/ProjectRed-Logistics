package io.github.useradd1980.projectredlogistics.power;

import mrtjp.projectred.api.IConnectable;
import mrtjp.projectred.core.power.ILowLoadMachine;
import mrtjp.projectred.core.power.ILowLoadPowerLine;
import mrtjp.projectred.core.power.PowerConductor;
import mrtjp.projectred.core.tile.IPoweredBlockEntity;
import mrtjp.projectred.expansion.tile.BasePneumaticDeviceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

/**
 * Reusable ProjectRed low-load power integration for pneumatic devices.
 *
 * ProjectRed's existing LowLoadPoweredBlockEntity cannot be combined through
 * inheritance with BasePneumaticDeviceBlockEntity, so this class mirrors the
 * small amount of conductor/topology plumbing needed by powered pneumatic
 * machines such as the RP2 Sorting Machine and, later, the Manager.
 */
public abstract class PoweredPneumaticDeviceBlockEntity
        extends BasePneumaticDeviceBlockEntity
        implements IPoweredBlockEntity, ILowLoadMachine {

    protected final PowerConductor conductor =
            new PowerConductor(this, 0.01, 160);

    private final List<PowerConductor> connectedConductors =
            new LinkedList<>();

    private long powerConnMap = 0L;
    private boolean conductorCacheInvalid = true;
    private int chargeFlow = 0;

    protected PoweredPneumaticDeviceBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState state) {

        super(type, pos, state);
    }

    @Override
    public void saveToNBT(
            CompoundTag tag,
            HolderLookup.Provider lookupProvider) {

        super.saveToNBT(tag, lookupProvider);
        conductor.save(tag);
        tag.putLong("powerConnMap", powerConnMap);
        tag.putInt("chargeFlow", chargeFlow);
    }

    @Override
    public void loadFromNBT(
            CompoundTag tag,
            HolderLookup.Provider lookupProvider) {

        super.loadFromNBT(tag, lookupProvider);
        conductor.load(tag);
        powerConnMap = tag.getLong("powerConnMap");
        chargeFlow = tag.getInt("chargeFlow");
        conductorCacheInvalid = true;
    }

    @Override
    public void tick() {
        super.tick();
        if (getLevel().isClientSide) return;

        conductor.tick();

        chargeFlow <<= 1;
        if (canConductorWork()) {
            chargeFlow |= 1;
        }
    }

    @Override
    public void onBlockPlaced(
            @Nullable LivingEntity player,
            ItemStack item) {

        super.onBlockPlaced(player, item);
        if (!getBlockLevel().isClientSide) {
            updateExternals();
        }
    }

    @Override
    public void onNeighborBlockChanged(BlockPos neighborPos) {
        super.onNeighborBlockChanged(neighborPos);
        if (!getBlockLevel().isClientSide) {
            conductorCacheInvalid = true;
            updateExternals();
        }
    }

    @Override
    public void onBlockRemoved() {
        if (!getBlockLevel().isClientSide) {
            notifyConnectedExternals();
        }
        super.onBlockRemoved();
    }

    @Override
    public long getConnMap() {
        return powerConnMap;
    }

    @Override
    public void setConnMap(long connMap) {
        powerConnMap = connMap;
    }

    @Override
    public void onMaskChanged() {
        conductorCacheInvalid = true;
    }

    @Override
    public boolean canConnectPart(
            IConnectable part,
            int side,
            int edgeRot) {

        return part instanceof ILowLoadMachine
                || part instanceof ILowLoadPowerLine;
    }

    @Override
    public PowerConductor getConductor(int dir) {
        return conductor;
    }

    public int getConductorCharge() {
        return (int) (conductor.getVoltage() * 10);
    }

    public int getConductorFlow() {
        return chargeFlow;
    }

    public boolean canConductorWork() {
        // Same 60 V operating threshold used by RP2's TileSorter.
        return getConductorCharge() > 600;
    }

    @Override
    public long getTime() {
        return getBlockLevel().getGameTime();
    }

    @Override
    public List<PowerConductor> getConnectedConductors() {
        if (conductorCacheInvalid) {
            recacheConductors();
            conductorCacheInvalid = false;
        }
        return connectedConductors;
    }

    private void recacheConductors() {
        connectedConductors.clear();

        for (int s = 0; s < 6; s++) {
            for (int r = 0; r < 4; r++) {
                PowerConductor conductor =
                        IPoweredBlockEntity.getExternalConductorForFaceConn(
                                this,
                                s,
                                r);
                if (conductor != null) {
                    connectedConductors.add(conductor);
                }
            }

            PowerConductor conductor =
                    IPoweredBlockEntity.getExternalConductorForCenterConn(
                            this,
                            s);
            if (conductor != null) {
                connectedConductors.add(conductor);
            }
        }
    }
}
