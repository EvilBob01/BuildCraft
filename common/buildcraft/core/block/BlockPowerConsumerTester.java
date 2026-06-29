package buildcraft.core.block;

import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;

import buildcraft.lib.block.BlockBCTile_Neptune;
import buildcraft.lib.tile.TileBC_Neptune;

import buildcraft.core.tile.TilePowerConsumerTester;

public class BlockPowerConsumerTester extends BlockBCTile_Neptune {

    public BlockPowerConsumerTester(Material material, String id) {
        super(material, id);
    }

    @Override
    public TileBC_Neptune createTileEntity(Level worldIn, BlockState state) {
        return new TilePowerConsumerTester();
    }
}
