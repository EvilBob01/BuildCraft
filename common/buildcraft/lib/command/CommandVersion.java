package buildcraft.lib.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;

import buildcraft.api.core.BCLog;

import buildcraft.lib.BCLib;

public class CommandVersion {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("version")
            .requires(src -> src.hasPermission(0))
            .executes(ctx -> {
                CommandSourceStack sender = ctx.getSource();
                String currentVersion = BCLib.VERSION;
                ChatFormatting colour = ChatFormatting.GREEN;
                if (currentVersion.startsWith("$")) {
                    currentVersion = "?.??.??";
                    colour = ChatFormatting.GRAY;
                }
                BCLog.logger.info("[lib.command.version] Version = " + currentVersion);
                final String versionStr = currentVersion;
                final ChatFormatting colourFinal = colour;
                sender.sendSuccess(() -> Component.translatable("command.buildcraft.version", versionStr)
                    .withStyle(colourFinal), false);
                if (versionStr.contains("-pre")) {
                    sender.sendSuccess(() -> Component.translatable("command.buildcraft.version.prerelease"), false);
                }
                return 1;
            });
    }
}
