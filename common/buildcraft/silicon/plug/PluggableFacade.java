/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.plug;

import javax.annotation.Nullable;

import net.minecraft.world.level.block.SupportType;
import buildcraft.lib.misc.BlockFaceShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.BlockColors;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.Explosion;

import net.minecraft.nbt.Tag;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import buildcraft.api.BCModules;
import buildcraft.api.facades.FacadeType;
import buildcraft.api.facades.IFacade;
import buildcraft.api.facades.IFacadePhasedState;
import buildcraft.api.transport.pipe.IPipeHolder;
import buildcraft.api.transport.pluggable.PipePluggable;
import buildcraft.api.transport.pluggable.PluggableDefinition;
import buildcraft.api.transport.pluggable.PluggableModelKey;

import buildcraft.lib.misc.MathUtil;
import buildcraft.lib.net.PacketBufferBC;

import buildcraft.silicon.BCSiliconItems;
import buildcraft.silicon.client.model.key.KeyPlugFacade;
import buildcraft.transport.client.model.key.KeyPlugBlocker;

public class PluggableFacade extends PipePluggable implements IFacade {

    private static final AABB[] BOXES = new AABB[6];

    static {
        double ll = 0 / 16.0;
        double lu = 2 / 16.0;
        double ul = 14 / 16.0;
        double uu = 16 / 16.0;

        double min = 0 / 16.0;
        double max = 16 / 16.0;

        BOXES[Direction.DOWN.get3DDataValue()] = new AABB(min, ll, min, max, lu, max);
        BOXES[Direction.UP.get3DDataValue()] = new AABB(min, ul, min, max, uu, max);
        BOXES[Direction.NORTH.get3DDataValue()] = new AABB(min, min, ll, max, max, lu);
        BOXES[Direction.SOUTH.get3DDataValue()] = new AABB(min, min, ul, max, max, uu);
        BOXES[Direction.WEST.get3DDataValue()] = new AABB(ll, min, min, lu, max, max);
        BOXES[Direction.EAST.get3DDataValue()] = new AABB(ul, min, min, uu, max, max);
    }

    public static final int SIZE = 2;
    public final FacadeInstance states;
    public final boolean isSideSolid;
    public final BlockFaceShape blockFaceShape;
    public int activeState;

    public PluggableFacade(PluggableDefinition definition, IPipeHolder holder, Direction side, FacadeInstance states) {
        super(definition, holder, side);
        this.states = states;
        isSideSolid = states.areAllStatesSolid(side);
        blockFaceShape = states.getBlockFaceShape(side);
    }

    public PluggableFacade(PluggableDefinition def, IPipeHolder holder, Direction side, CompoundTag nbt) {
        super(def, holder, side);
        if (nbt.contains("states") && !nbt.contains("facade")) {
            ListTag tagStates = nbt.getList("states", Tag.TAG_COMPOUND);
            if (tagStates.size() > 0) {
                boolean isHollow = tagStates.getCompoundTagAt(0).getBoolean("isHollow");
                CompoundTag tagFacade = new CompoundTag();
                tagFacade.put("states", tagStates);
                tagFacade.putBoolean("isHollow", isHollow);
                nbt.put("facade", tagFacade);
            }
        }
        this.states = FacadeInstance.readFromNbt(nbt.getCompound("facade"));
        activeState = MathUtil.clamp(nbt.getInt("activeState"), 0, states.phasedStates.length - 1);
        isSideSolid = states.areAllStatesSolid(side);
        blockFaceShape = states.getBlockFaceShape(side);
    }

    @Override
    public CompoundTag writeToNbt() {
        CompoundTag nbt = super.writeToNbt();
        nbt.put("facade", states.writeToNbt());
        nbt.putInt("activeState", activeState);
        return nbt;
    }

    // Networking

    public PluggableFacade(PluggableDefinition def, IPipeHolder holder, Direction side, FriendlyByteBuf buffer) {
        super(def, holder, side);
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        states = FacadeInstance.readFromBuffer(buf);
        isSideSolid = buf.readBoolean();
        blockFaceShape = buf.readEnumValue(BlockFaceShape.class);
    }

    @Override
    public void writeCreationPayload(FriendlyByteBuf buffer) {
        PacketBufferBC buf = PacketBufferBC.asPacketBufferBc(buffer);
        states.writeToBuffer(buf);
        buf.writeBoolean(isSideSolid);
        buf.writeEnumValue(blockFaceShape);
    }

    // Pluggable methods

    @Override
    public AABB getBoundingBox() {
        return BOXES[side.get3DDataValue()];
    }

    @Override
    public boolean isBlocking() {
        return !isHollow();
    }

    @Override
    public boolean canBeConnected() {
        return !isHollow();
    }

    @Override
    public boolean isSideSolid() {
        return isSideSolid;
    }

    @Override
    public float getExplosionResistance(@Nullable Entity exploder, Explosion explosion) {
        return states.phasedStates[activeState].stateInfo.state.getBlock().getExplosionResistance(exploder);
    }

    @Override
    public BlockFaceShape getBlockFaceShape() {
        return blockFaceShape;
    }

    @Override
    public ItemStack getPickStack() {
        return BCSiliconItems.plugFacade.createItemStack(states);
    }

    @Override
    public PluggableModelKey getModelRenderKey(BlockRenderLayer layer) {
        if (states.type == FacadeType.Basic) {
            FacadePhasedState facadeState = states.phasedStates[activeState];
            BlockState blockState = facadeState.stateInfo.state;
            BlockRenderLayer targetLayer = blockState.getBlock().getBlockLayer();
            if (targetLayer == BlockRenderLayer.TRANSLUCENT) {
                if (layer != targetLayer) {
                    return null;
                }
            } else if (layer == BlockRenderLayer.TRANSLUCENT) {
                return null;
            }
            return new KeyPlugFacade(layer, side, blockState, isHollow());
        } else if (layer == BlockRenderLayer.CUTOUT && BCModules.TRANSPORT.isLoaded()) {
            return KeyPlugBlocker.create(side);
        }
        return null;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public int getBlockColor(int tintIndex) {
        FacadePhasedState state = states.phasedStates[activeState];
        BlockColors colours = Minecraft.getInstance().getBlockColors();
        return colours.colorMultiplier(state.stateInfo.state, holder.getPipeWorld(), holder.getPipePos(), tintIndex);
    }

    // IFacade

    @Override
    public FacadeType getType() {
        return states.getType();
    }

    @Override
    public boolean isHollow() {
        return states.isHollow();
    }

    @Override
    public IFacadePhasedState[] getPhasedStates() {
        return states.getPhasedStates();
    }
}
