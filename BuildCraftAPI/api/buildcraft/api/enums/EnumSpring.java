package buildcraft.api.enums;

import java.util.Locale;
import java.util.function.Supplier;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.util.StringRepresentable;

import buildcraft.api.properties.BuildCraftProperties;

public enum EnumSpring implements StringRepresentable {
    WATER(5, -1, Blocks.WATER.getDefaultState()),
    OIL(6000, 8, null); // Set in BuildCraftEnergy

    public static final EnumSpring[] VALUES = values();

    public final int tickRate, chance;
    public BlockState liquidBlock;
    public boolean canGen = true;
    public Supplier<BlockEntity> tileConstructor;

    private final String lowerCaseName = name().toLowerCase(Locale.ROOT);

    EnumSpring(int tickRate, int chance, BlockState liquidBlock) {
        this.tickRate = tickRate;
        this.chance = chance;
        this.liquidBlock = liquidBlock;
    }

    public static EnumSpring fromState(BlockState state) {
        return state.getValue(BuildCraftProperties.SPRING_TYPE);
    }

    @Override
    public String getSerializedName() {
        return lowerCaseName;
    }
    public String getName() { return getSerializedName(); }
}