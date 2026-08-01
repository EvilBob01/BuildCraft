package buildcraft.lib.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CommandBuildCraft {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("buildcraft")
            .then(CommandVersion.register())
            .then(CommandChangelog.register())
            .then(CommandReloadRegistries.register());
    }
}
