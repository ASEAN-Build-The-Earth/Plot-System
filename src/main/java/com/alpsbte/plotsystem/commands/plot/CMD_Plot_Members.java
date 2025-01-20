/*
 * The MIT License (MIT)
 *
 *  Copyright © 2023, Alps BTE <bte.atchli@gmail.com>
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package com.alpsbte.plotsystem.commands.plot;

import com.alpsbte.alpslib.utils.AlpsUtils;
import com.alpsbte.plotsystem.PlotSystem;
import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.commands.SubCommand;
import com.alpsbte.plotsystem.core.menus.PlotMemberMenu;
import com.alpsbte.plotsystem.core.system.Builder;
import com.alpsbte.plotsystem.core.system.plot.AbstractPlot;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.utils.PlotUtils;
import com.alpsbte.plotsystem.utils.Utils;
import com.alpsbte.plotsystem.utils.enums.Status;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.Objects;

import static net.kyori.adventure.text.Component.text;

public class CMD_Plot_Members extends SubCommand {

    public CMD_Plot_Members(BaseCommand baseCommand) {
        super(baseCommand);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        try {
            if (getPlayer(sender) != null) {
                Plot plot;
                // Get Plot
                if (args.length > 0 && AlpsUtils.tryParseInt(args[0]) != null) {
                    //plot members <id>
                    int plotID = Integer.parseInt(args[0]);
                    if (PlotUtils.plotExists(plotID)) {
                        plot = new Plot(plotID);
                    } else {
                        sender.sendMessage(Utils.ChatUtils.getAlertFormat("This plot does not exist!"));
                        return;
                    }
                } else if (PlotUtils.isPlotWorld(getPlayer(sender).getWorld())) {
                    //plot members
                    AbstractPlot p = PlotUtils.getCurrentPlot(Builder.byUUID(getPlayer(sender).getUniqueId()), Status.unfinished, Status.unreviewed);
                    if (p instanceof Plot) {
                        plot = (Plot) p;
                    } else {
                        sendInfo(sender);
                        return;
                    }
                } else {
                    sendInfo(sender);
                    return;
                }

                if (Objects.requireNonNull(plot).getPlotOwner().getUUID().equals(getPlayer(sender).getUniqueId()) || getPlayer(sender).hasPermission("plotsystem.admin")) {
                    new PlotMemberMenu(plot, getPlayer(sender));
                } else {
                    sender.sendMessage(Utils.ChatUtils.getAlertFormat("You don't have permission to manage this plot's members!"));
                }
            } else {
                Bukkit.getConsoleSender().sendMessage(text("This command can only be used as a player!", NamedTextColor.RED));
            }
        } catch (SQLException ex) {
            sender.sendMessage(Utils.ChatUtils.getAlertFormat("An error occurred while executing command!"));
            PlotSystem.getPlugin().getComponentLogger().error(text("A SQL error occurred!"), ex);
        }
    }

    @Override
    public String[] getNames() {
        return new String[]{"members"};
    }

    @Override
    public String getDescription() {
        return "Opens the plot member menu.";
    }

    @Override
    public String[] getParameter() {
        return new String[]{"ID"};
    }

    @Override
    public String getPermission() {
        return "plotsystem.plot.members";
    }
}
