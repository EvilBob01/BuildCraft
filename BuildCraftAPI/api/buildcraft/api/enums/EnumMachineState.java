package buildcraft.api.enums;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.IStringSerializable;

import buildcraft.api.properties.BuildCraftProperties;

public enum EnumMachineState implements IStringSerializable {
    OFF,
    ON,
    DONE;

    public static EnumMachineState getType(BlockState state) {
        return state.getValue(BuildCraftProperties.MACHINE_STATE);
    }

    @Override
    public String getName() {
        return name();
    }
}
