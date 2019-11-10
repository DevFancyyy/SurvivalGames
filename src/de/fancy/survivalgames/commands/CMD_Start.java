package de.fancy.survivalgames.commands;

import de.fancy.survivalgames.Main;
import de.fancy.survivalgames.utils.Gamestate;
import de.fancy.survivalgames.utils.MapManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CMD_Start implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        if(player.hasPermission("game.start")) {
            if(Main.getInstance().getCurrentState() == Gamestate.LOBBY) {
                if(Main.getInstance().getGameCountdown().getCountdown() > 15) {
                    if (Bukkit.getOnlinePlayers().size() >= (int) Main.getInstance().getMapManager().getMapValue(Main.getInstance().getCurrentMap(), "MinPlayer")) {
                        Main.getInstance().getGameCountdown().setCountdown(15);
                        player.sendMessage(Main.getInstance().getPrefix() + "§7Du hast das Spiel §agestartet§7!");
                    } else {
                        player.sendMessage(Main.getInstance().getPrefix() + "§cEs sind nicht genügend Spieler online!");
                    }
                } else {
                    player.sendMessage(Main.getInstance().getPrefix() + "§cDas Spiel startet bald!");
                }
            } else {
                player.sendMessage(Main.getInstance().getPrefix() + "§cDas Spiel läuft bereits!");
            }
        } else {
            player.sendMessage(Main.getInstance().getPrefix() + "§cKeine Rechte!");
        }

        //DEBUGGING
        if(args.length == 1) {
            if(args[0].equalsIgnoreCase("spec")) {
                Main.getInstance().getGameManager().setPlayerAsSpectator(player);
            }
        }

        return false;
    }
}
