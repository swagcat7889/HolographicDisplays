/*
 * Copyright (C) filoghost and contributors
 *
 * SPDX-License-Identifier: GPL-3.0-or-later
 */
package me.filoghost.holographicdisplays.plugin.commands.subs;

import me.filoghost.fcommons.command.sub.SubCommandContext;
import me.filoghost.holographicdisplays.nms.v1_13_R2.VersionNMSManager;
import me.filoghost.holographicdisplays.plugin.commands.HologramSubCommand;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class DebugCommand extends HologramSubCommand {

    public DebugCommand() {
        super("debug");
        setShowInHelpCommand(false);
        setDescription("Displays information useful for debugging.");
    }

    @Override
    public void execute(CommandSender sender, String[] args, SubCommandContext context) {
        //sender.sendMessage(ColorScheme.ERROR + "This command is currently unused.");
        if (args[0].equalsIgnoreCase("unload")) {
            VersionNMSManager.sendChunkUnload((Player) sender);
        } else if (args[0].equalsIgnoreCase("load")) {
            VersionNMSManager.sendChunkLoad((Player) sender);
        } else if (args[0].equalsIgnoreCase("reload")) {
            VersionNMSManager.sendChunkRefresh((Player) sender);
        }
    }

}
