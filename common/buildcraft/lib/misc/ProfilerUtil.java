package buildcraft.lib.misc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

import net.minecraft.util.profiling.ProfilerFiller;

import buildcraft.api.core.BCLog;

public class ProfilerUtil {

    public static void printProfilerResults(ProfilerFiller profiler, String rootName) {
        // TODO (Phase 5): ProfilerFiller.getProfilingData() was removed in 1.18; no-op for now
    }

    public static void printProfilerResults(ProfilerFiller profiler, String rootName, long totalNanoseconds) {
        // TODO (Phase 5): no-op
    }

    public static void logProfilerResults(ProfilerFiller profiler, String rootName) {
        // TODO (Phase 5): no-op
    }

    public static void logProfilerResults(ProfilerFiller profiler, String rootName, long totalNanoseconds) {
        // TODO (Phase 5): no-op
    }

    public static void saveProfilerResults(ProfilerFiller profiler, String rootName, Path dest) throws IOException {
        // TODO (Phase 5): no-op
    }

    public static void saveProfilerResults(ProfilerFiller profiler, String rootName, File dest) throws IOException {
        // TODO (Phase 5): no-op
    }

    public static void saveProfilerResults(ProfilerFiller profiler, String rootName, long totalNanoseconds, Path dest)
        throws IOException {
        // TODO (Phase 5): no-op
    }

    public static <E extends Throwable> void writeProfilerResults(ProfilerFiller profiler, String rootName,
        ILogAcceptor<E> dest) throws E {
        // TODO (Phase 5): no-op
    }

    public static <E extends Throwable> void writeProfilerResults(ProfilerFiller profiler, String rootName,
        long totalNanoseconds, ILogAcceptor<E> dest) throws E {
        // TODO (Phase 5): no-op
    }

    public interface ILogAcceptor<E extends Throwable> {
        void write(String line) throws E;
    }

    public interface ProfilerEntry {
        void startSection(String name);

        void endSection();

        default void endStartSection(String name) {
            endSection();
            startSection(name);
        }
    }

    public static ProfilerEntry createEntry(ProfilerFiller p1, ProfilerFiller p2) {
        if (p1 != null && p2 != null) {
            return new ProfilerEntry2(p1, p2);
        } else if (p1 != null) {
            return new ProfilerEntry1(p1);
        } else if (p2 != null) {
            return new ProfilerEntry1(p2);
        } else {
            return ProfilerEntry0.INSTANCE;
        }
    }

    static enum ProfilerEntry0 implements ProfilerEntry {
        INSTANCE;

        @Override
        public void startSection(String name) {}

        @Override
        public void endSection() {}
    }

    static final class ProfilerEntry1 implements ProfilerEntry {
        final ProfilerFiller p;

        ProfilerEntry1(ProfilerFiller p) {
            this.p = p;
        }

        @Override
        public void startSection(String name) {
            p.push(name);
        }

        @Override
        public void endSection() {
            p.pop();
        }
    }

    static final class ProfilerEntry2 implements ProfilerEntry {
        final ProfilerFiller p1, p2;

        ProfilerEntry2(ProfilerFiller p1, ProfilerFiller p2) {
            this.p1 = p1;
            this.p2 = p2;
        }

        @Override
        public void startSection(String name) {
            p1.push(name);
            p2.push(name);
        }

        @Override
        public void endSection() {
            p1.pop();
            p2.pop();
        }
    }
}
