package de.fancy.survivalgames.commands;

import de.fancy.survivalgames.Main;
import de.fancy.survivalgames.utils.Achievements;
import de.fancy.survivalgames.utils.GameManager;
import de.fancy.survivalgames.utils.MySQL;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.UUID;

public class CMD_Stats implements CommandExecutor {
    public Main plugin;

    public CMD_Stats(Main main) {
        this.plugin = main;
    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args){
        Player player = (Player) sender;

        if(args.length == 0) {
            loadPlayerStats(player, player);
        } else if (args.length == 1) {
            if(args[0].contains("#")) {
                loadPositionStats(player, Integer.valueOf(args[0].replace("#", "")));
            } else {
                OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);

                if(target != null) {
                    Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                        @Override
                        public void run() {
                            if(!MySQL.ifUserexists(target.getUniqueId())) {
                                player.sendMessage(Main.getInstance().getPrefix() + "§cEs wurden keine Statistiken zu " + target.getName() + "§c gefunden!");
                            } else {
                                loadPlayerStats(player, target);
                            }
                        }
                    });
                } else {
                    player.sendMessage(Main.getInstance().getPrefix() + "§cEs wurden keine Statistiken zu §4" + args[0] + "§c gefunden!");
                }
            }
        } else if(args.length > 1) {
            player.sendMessage(Main.getInstance().getPrefix() + "§cZu viele Argumente: /stats (Spieler)");
        }

        return true;
    }

    public void loadPlayerStats(Player player, OfflinePlayer target) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                ResultSet result = MySQL.getPlayerData(target.getUniqueId());

                if(result != null) {
                    DecimalFormat statsFormat = new DecimalFormat("#.##");
                    int kills = 0;
                    int deaths = 0;
                    String kd = "";
                    int pgames = 0; /* PLAYED GAMES */
                    int wgames = 0; /* WON GAMES */
                    int lgames = 0; /* LOST GAMES */
                    String winrate = "";
                    int receivedAchievements = MySQL.getPlayerAchievements(target.getUniqueId()).size();

                    try {
                        kills = result.getInt("kills");
                        deaths = result.getInt("deaths");
                        pgames = result.getInt("pgames");
                        wgames = result.getInt("wgames");
                        lgames = result.getInt("lgames");
                    } catch(SQLException e) {
                        e.printStackTrace();
                    }

                    if(deaths > 0) {
                        kd = statsFormat.format((float) kills / deaths);
                    } else {
                        kd = "0";
                    }

                    if(pgames > 0) {
                        winrate = statsFormat.format((float) wgames / pgames * 100);
                    } else {
                        winrate = "0,00";
                    }

                    player.sendMessage(Main.getInstance().getPrefix() + "§7----- Stats von " + target.getName() + "§7 -----");
                    player.sendMessage(Main.getInstance().getPrefix() + "§7Platzierung: §a" + MySQL.getPlayerPosition(target.getUniqueId()));
                    player.sendMessage(Main.getInstance().getPrefix() + "§7Kills: §a" + kills);
                    player.sendMessage(Main.getInstance().getPrefix() + "§7Deaths: §a" + deaths);
                    player.sendMessage(Main.getInstance().getPrefix() + "§7KD: §a" + kd);
                    player.sendMessage(Main.getInstance().getPrefix() + "§7Gespielte Spiele: §a" + pgames);
                    player.sendMessage(Main.getInstance().getPrefix() + "§7Gewonnene Spiele: §a" + wgames);
                    player.sendMessage(Main.getInstance().getPrefix() + "§7Verlorene Spiele: §a" + lgames);
                    player.sendMessage(Main.getInstance().getPrefix() + "§7Siegesquote: §a" + winrate + "%");
                    player.sendMessage(Main.getInstance().getPrefix() + "§7Achievements: §a" + receivedAchievements + "§7/§a" + Achievements.values().length);
                    player.sendMessage(Main.getInstance().getPrefix() + "§7----- Stats von " + target.getName() + "§7 -----");
                } else {
                    player.sendMessage(Main.getInstance().getPrefix() + "§cEs wurden keine Statistiken zu " + target.getName() + "§c gefunden!");
                }
            }
        });
    }

    public void loadPositionStats(Player player, int position) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                ResultSet result = MySQL.getPositionData(position);

                if (result != null) {
                    OfflinePlayer target = player;
                    DecimalFormat statsFormat = new DecimalFormat("#.##");
                    int kills = 0;
                    int deaths = 0;
                    String kd = "";
                    int pgames = 0; /* PLAYED GAMES */
                    int wgames = 0; /* WON GAMES */
                    int lgames = 0; /* LOST GAMES */
                    String winrate = "";
                    String receivedAchievements = 0 + "";

                    try {
                        target = Bukkit.getOfflinePlayer(UUID.fromString(result.getString("uuid")));

                        kills = result.getInt("kills");
                        deaths = result.getInt("deaths");
                        pgames = result.getInt("pgames");
                        wgames = result.getInt("wgames");
                        lgames = result.getInt("lgames");
                        MySQL.getPlayerAchievements(UUID.fromString(result.getString("uuid"))).size();
                    } catch (SQLException e) {
                        e.printStackTrace();
                    }

                    if(deaths > 0) {
                        kd = statsFormat.format((float) kills / deaths);
                    } else {
                        kd = "0";
                    }

                    if(pgames > 0) {
                        winrate = statsFormat.format((float) wgames / pgames * 100);
                    } else {
                        winrate = "0,00";
                    }

                    player.sendMessage(Main.getInstance().getPrefix() + "§7----- Stats von " + target.getName() + "§7 -----");
                    player.sendMessage(Main.getInstance().getPrefix() + "§7Platzierung: §a" + MySQL.getPlayerPosition(target.getUniqueId()));
                    player.sendMessage(Main.getInstance().getPrefix() + "§7Kills: §a" + kills);
                    player.sendMessage(Main.getInstance().getPrefix() + "§7Deaths: §a" + deaths);
                    player.sendMessage(Main.getInstance().getPrefix() + "§7KD: §a" + kd);
                    player.sendMessage(Main.getInstance().getPrefix() + "§7Gespielte Spiele: §a" + pgames);
                    player.sendMessage(Main.getInstance().getPrefix() + "§7Gewonnene Spiele: §a" + wgames);
                    player.sendMessage(Main.getInstance().getPrefix() + "§7Verlorene Spiele: §a" + lgames);
                    player.sendMessage(Main.getInstance().getPrefix() + "§7Siegesquote: §a" + winrate + "%");
                    player.sendMessage(Main.getInstance().getPrefix() + "§7Achievements: §a" + receivedAchievements + "§7/§a" + Achievements.values().length);
                    player.sendMessage(Main.getInstance().getPrefix() + "§7----- Stats von " + target.getName() + "§7 -----");
                } else {
                    player.sendMessage(Main.getInstance().getPrefix() + "§cEs wurden keine Statistiken zum Platz " + position + " gefunden!");
                }
            }
        });
    }
}
