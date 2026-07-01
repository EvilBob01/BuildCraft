/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */

package buildcraft.silicon.block;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;

import buildcraft.api.enums.EnumLaserTableType;
import buildcraft.api.mj.ILaserTargetBlock;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.silicon.BCSiliconGuis;
import buildcraft.silicon.tile.TileAdvancedCraftingTable;
import buildcraft.silicon.tile.TileAssemblyTable;
import buildcraft.silicon.tile.TileChargingTable;
import buildcraft.silicon.tile.TileIntegrationTable;
import buildcraft.silicon.tile.TileProgrammingTable_Neptune;

public class BlockLaserTable extends BlockBCTile_Neptune implements ILaserTargetBlock {
    private final EnumLaserTableType type;

    public BlockLaserTable(EnumLaserTableType type, Material material, String id) {
        super(material, id);
        this.type = type;
    }

    @Override
    public boolean isOpaqueCube(BlockState state) {
        return false;
    }

    @Override
    public boolean isFullCube(BlockState state) {
        return false;
    }

    @Override
    public BlockRenderLayer getBlockLayer() {
        return BlockRenderLayer.CUTOUT;
    }

    @Override
    public TileBC_Neptune createTileEntity(Level world, BlockState state) {
        switch(type) {
            case ASSEMBLY_TABLE:
                return new TileAssemblyTable();
            case ADVANCED_CRAFTING_TABLE:
                return new TileAdvancedCraftingTable();
            case INTEGRATION_TABLE:
                return new TileIntegrationTable();
            case CHARGING_TABLE:
                return new TileChargingTable();
            case PROGRAMMING_TABLE:
                return new TileProgrammingTable_Neptune();
        }
        return null;
    }

    @Override
    public AABB getBoundingBox(BlockState state, BlockGetter source, BlockPos pos) {
        return new AABB(0 / 16D, 0 / 16D, 0 / 16D, 16 / 16D, 9 / 16D, 16 / 16D);
    }

    @Override
    public boolean onBlockActivated(Level world, BlockPos pos, BlockState state, Player player, InteractionHand hand, Direction side, float hitX, float hitY, float hitZ) {
        switch(type) {
            case ASSEMBLY_TABLE:
                if (!world.isClientSide) {
                    BCSiliconGuis.ASSEMBLY_TABLE.openGUI(player, pos);
                }
                return true;
            case ADVANCED_CRAFTING_TABLE:
                if (!world.isClientSide) {
                    BCSiliconGuis.ADVANCED_CRAFTING_TABLE.openGUI(player, pos);
                }
                return true;
            case INTEGRATION_TABLE:
                if (!world.isClientSide) {
                    BCSiliconGuis.INTEGRATION_TABLE.openGUI(player, pos);
                }
                return true;
            case CHARGING_TABLE:
            case PROGRAMMING_TABLE:
        }
        return false;
    }
}
