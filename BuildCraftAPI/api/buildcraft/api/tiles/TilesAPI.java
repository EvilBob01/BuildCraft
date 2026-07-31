package buildcraft.api.tiles;

import javax.annotation.Nonnull;

import net.minecraft.core.Direction;

import net.neoforged.neoforge.capabilities.BlockCapability;

import buildcraft.api.core.CapabilitiesHelper;

public class TilesAPI {
    @Nonnull
    public static final BlockCapability<IControllable, Direction> CAP_CONTROLLABLE;

    @Nonnull
    public static final BlockCapability<IHasWork, Direction> CAP_HAS_WORK;

    @Nonnull
    public static final BlockCapability<IHeatable, Direction> CAP_HEATABLE;

    @Nonnull
    public static final BlockCapability<ITileAreaProvider, Direction> CAP_TILE_AREA_PROVIDER;

    static {
        CAP_CONTROLLABLE = CapabilitiesHelper.registerCapability(IControllable.class);
        CAP_HAS_WORK = CapabilitiesHelper.registerCapability(IHasWork.class);
        CAP_HEATABLE = CapabilitiesHelper.registerCapability(IHeatable.class);
        CAP_TILE_AREA_PROVIDER = CapabilitiesHelper.registerCapability(ITileAreaProvider.class);
    }
}
