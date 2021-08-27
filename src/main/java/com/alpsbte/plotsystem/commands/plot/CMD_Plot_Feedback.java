/*
 * The MIT License (MIT)
 *
 *  Copyright © 2021, Alps BTE <bte.atchli@gmail.com>
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

import com.alpsbte.plotsystem.commands.BaseCommand;
import com.alpsbte.plotsystem.commands.SubCommand;
import com.alpsbte.plotsystem.core.menus.FeedbackMenu;
import com.alpsbte.plotsystem.core.system.plot.Plot;
import com.alpsbte.plotsystem.core.system.plot.PlotManager;
import com.alpsbte.plotsystem.utils.Utils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.sql.SQLException;
import java.util.logging.Level;

public class CMD_Plot_Feedback extends SubCommand {

    public CMD_Plot_Feedback(BaseCommand baseCommand) {
        super(baseCommand);
    }

    @Override
    public void onCommand(CommandSender sender, String[] args) {
        try {
            if (getPlayer(sender) != null) {
                Plot plot;
                if (args.length > 0 && Utils.TryParseInt(args[0]) != null) {
                    int plotID = Integer.parseInt(args[0]);
                    if (PlotManager.plotExists(plotID)) {
                        plot = new Plot(plotID);
                    } else {
                        sender.sendMessage(Utils.getErrorMessageFormat("This plot does not exist!"));
                        return;
                    }
                } else if (PlotManager.isPlotWorld(getPlayer(sender).getWorld())) {
                    plot = PlotManager.getPlotByWorld(getPlayer(sender).getWorld());
                } else {
                    sendInfo(sender);
                    return;
                }

                if(plot.getPlotOwner().getUUID().equals(getPlayer(sender).getUniqueId()) || plot.getPlotMembers().stream().anyMatch(m -> m.getUUID().equals(getPlayer(sender).getUniqueId())) || getPlayer(sender).hasPermission("alpsbte.plot.review")) {
                    if (plot.isReviewed()) {
                        new FeedbackMenu(getPlayer(sender), plot.getID());
                    } else {
                        sender.sendMessage(Utils.getErrorMessageFormat("This plot has not yet been reviewed!"));
                    }
                } else {
                    sender.sendMessage(Utils.getErrorMessageFormat("You don't have permission to see this plot feedback!"));
                }
            } else {
                Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "This command can only be used as a player!");
            }
        } catch (SQLException ex) {
            sender.sendMessage(Utils.getErrorMessageFormat("An error occurred while executing command!"));
            Bukkit.getLogger().log(Level.SEVERE, "A SQL error occurred!", ex);
        }
    }

    @Override
    public String[] getNames() {
        return new String[] { "feedback" };
    }

    @Override
    public String getDescription() {
        return "Shows all feedback information of a plot (Points, Feedback and Reviewer).";
    }

    @Override
    public String[] getParameter() {
        return new String[] { "ID" };
    }

    @Override
    public String getPermission() {
        return "plotsystem.plot.feedback";
    }
}