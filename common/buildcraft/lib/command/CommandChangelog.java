package buildcraft.lib.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class CommandChangelog {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("changelog")
            .requires(src -> src.hasPermission(0))
            .executes(ctx -> {
                ctx.getSource().sendSuccess(() -> Component.literal("TODO: Implement this!"), false);
                return 1;
            });
    }
}
