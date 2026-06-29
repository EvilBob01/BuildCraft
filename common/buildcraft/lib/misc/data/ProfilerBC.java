package buildcraft.lib.misc.data;

import net.minecraft.client.Minecraft;
import net.minecraft.util.profiling.ProfilerFiller;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

/** An extension for minecraft's {@link Profiler} class that returns {@link AutoCloseable} profiler sections. */
public class ProfilerBC {

    @OnlyIn(Dist.CLIENT)
    public static ProfilerBC getClient() {
        return new ProfilerBC(Minecraft.getMinecraft().mcProfiler);
    }

    private final Profiler profiler;

    public ProfilerBC(Profiler profiler) {
        this.profiler = profiler;
    }

    public IProfilerSection start(String name) {
        profiler.startSection(name);
        return profiler::endSection;
    }

    public IProfilerSection start(String... names) {
        for (String s : names) {
            profiler.startSection(s);
        }
        return () -> {
            for (int i = 0; i < names.length; i++) {
                profiler.endSection();
            }
        };
    }

    public interface IProfilerSection extends AutoCloseable {
        /** Ends the current profiler section. Will only throw if this was called incorrectly. */
        @Override
        void close();
    }
}
