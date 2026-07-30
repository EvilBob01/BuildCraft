package buildcraft.api.transport.pipe;


import net.minecraft.client.renderer.BufferBuilder;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public interface IPipeBehaviourRenderer<B extends PipeBehaviour> {
    void render(B behaviour, double x, double y, double z, float partialTicks, BufferBuilder bb);
}
