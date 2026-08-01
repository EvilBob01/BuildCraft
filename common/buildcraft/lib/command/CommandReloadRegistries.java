package buildcraft.lib.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

import buildcraft.lib.script.ReloadableRegistryManager;

public class CommandReloadRegistries {

    public static LiteralArgumentBuilder<CommandSourceStack> register() {
        return Commands.literal("reload")
            .requires(src -> src.hasPermission(2))
            .executes(ctx -> {
                ReloadableRegistryManager.DATA_PACKS.reloadAll();
                return 1;
            });
    }
}
