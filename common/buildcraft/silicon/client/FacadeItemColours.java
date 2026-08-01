package buildcraft.silicon.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;

import buildcraft.silicon.item.ItemPluggableFacade;
import buildcraft.silicon.plug.FacadeInstance;
import buildcraft.silicon.plug.FacadePhasedState;

public enum FacadeItemColours implements IItemColor {
    INSTANCE;

    @Override
    public int colorMultiplier(ItemStack stack, int tintIndex) {
        FacadeInstance states = ItemPluggableFacade.getStates(stack);
        FacadePhasedState state = states.getCurrentStateForStack();
        int colour = -1;
        ResourceLocation id = state.stateInfo.state.getBlock().builtInRegistryHolder().key().location();
        if (id != null && "wildnature".equals(id.getNamespace())) {
            // Fixes https://github.com/BuildCraft/BuildCraft/issues/4435
            // (Basically wildnature doesn't handle the null world+position correctly)
            // (But instead of throwing an NPE they pass invalid values to "ColourizerGrass")
            return -1;
        }
        try {
            colour = Minecraft.getInstance().getBlockColors().getColor(state.stateInfo.state, null, null);
        } catch (NullPointerException ex) {
            // the block didn't like the null world or player
        }
        return colour;
    }
}
