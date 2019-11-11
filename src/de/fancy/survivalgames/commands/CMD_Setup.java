package de.fancy.survivalgames.commands;

import de.fancy.survivalgames.Main;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.text.DecimalFormat;
import java.util.List;

public class CMD_Setup implements CommandExecutor {
    private String notAllowed = Main.getInstance().getPrefix() + "§cDu hast keine Berechtigung dazu!";
    private String notInSetupMode = Main.getInstance().getPrefix() + "§cDu bist nicht im Edit-Mode!";

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Player player = (Player) sender;

        if(player.hasPermission("game.setup") || player.isOp()) {
            if(args.length == 0) {
                player.sendMessage(Main.getInstance().getPrefix() + "§cFehlende Argumente!");
            } else if(args.length == 1) {
                if (args[0].equalsIgnoreCase("start")) {
                    if (!Main.getInstance().allowedBuilders.contains(player)) {
                        Main.getInstance().allowedBuilders.add(player);
                        player.setGameMode(GameMode.CREATIVE);
                        player.sendMessage(Main.getInstance().getPrefix() + "§7Du hast den Edit-Mode §abetreten§7!");
                    } else {
                        player.sendMessage(Main.getInstance().getPrefix() + "§cDu bist bereits im Edit-Mode!");
                    }
                } else if (args[0].equalsIgnoreCase("end")) {
                    if (Main.getInstance().allowedBuilders.contains(player)) {
                        Main.getInstance().allowedBuilders.remove(player);
                        player.sendMessage(Main.getInstance().getPrefix() + "§7Du hast den Edit-Mode §cverlassen§7!");
                    } else {
                        player.sendMessage(notInSetupMode);
                    }
                } else if (args[0].equalsIgnoreCase("help")) {
                    player.sendMessage(Main.getInstance().getPrefix() + "§7Übersicht über die §aSetup-Befehle§7:");
                    player.sendMessage("§a/setup <start | end>§7: Betrete/Verlasse den Setup-Mode");
                    player.sendMessage("§a/setup Waiting <setspawn | setworld>§7: Setze den Spawn / die Welt der Wartelobby");
                    player.sendMessage("§a/setup map list§7: Liste alle Maps auf");
                    player.sendMessage("§a/setup map <map> info>§7: Infos zur Map");
                    player.sendMessage("§a/setup map <create | delete> <mapname>§7: Erstelle / Lösche eine Welt");
                    player.sendMessage("§a/setup map <map> spawn add§7: Erstelle einen Spawnpunkt");
                    player.sendMessage("§a/setup map <map> spawn list§7: Liste die Spawnpunkte einer Map auf");
                    player.sendMessage("§a/setup map <map> spawn tp <id>§7: Teleportiere dich zu dem Spawnpunkt");
                    player.sendMessage("§a/setup map <map> spawn delete <id>§7: Lösche einen Spawnpunkt");
                    player.sendMessage("§a/setup map <map> set <center | world>§7: Setze die Welt / den Mittelpunkt");
                    player.sendMessage("§a/setup map <map> set <border | maxplayer | minplayer> <Wert>§7: Setze den entsprechenden Wert");
                    player.sendMessage(Main.getInstance().getPrefix() + "§7-------------------------------------------------");
                }
            } else if(args.length == 2) {
                if(Main.getInstance().allowedBuilders.contains(player)) {
                    if(args[0].equalsIgnoreCase("waiting")) {
                        if(args[1].equalsIgnoreCase("setspawn")) {
                            Main.getInstance().getMapManager().setMapValue("Waiting", "SPAWN", player.getLocation());
                            player.sendMessage(Main.getInstance().getPrefix() + "§7Der Spawn der §aWartelobby §7wurde gesetzt!");
                        } else if(args[1].equalsIgnoreCase("setworld")) {
                            Main.getInstance().getMapManager().setMapValue("Waiting", "WORLD", player.getWorld());
                            player.sendMessage(Main.getInstance().getPrefix() + "§7Die Welt der §aWartelobby §7wurde gesetzt!");
                        }
                    } else if(args[0].equalsIgnoreCase("map")) {
                        if(args[1].equalsIgnoreCase("list")) {
                            player.sendMessage(Main.getInstance().getPrefix() + "§7Übersicht über die §aMaps§7:");

                            for(String map : Main.getInstance().getMapManager().getMaps()) {
                                if(!map.equals("Waiting")) {
                                    player.sendMessage(Main.getInstance().getPrefix() + "§a" + map + " §7| §a" + Main.getInstance().getMapManager().getMapValue(map, "MINPLAYER") + "§7/§a" + Main.getInstance().getMapManager().getMapValue(map, "MAXPLAYER"));
                                }
                            }
                        }
                    }
                } else {
                    player.sendMessage(notInSetupMode);
                }
            } else if(args.length == 3) {
                if(Main.getInstance().allowedBuilders.contains(player)) {
                    if(args[0].equalsIgnoreCase("map")) {
                        if(args[1].equalsIgnoreCase("create")) {
                            if(!Main.getInstance().getMapManager().doesMapExist(args[2])) {
                                Main.getInstance().getMapManager().createMap(player, args[2].toString());
                                player.sendMessage(Main.getInstance().getPrefix() + "§7Die Map §a" + args[2].toString() + "§7 wurde erstellt!");
                            } else {
                                player.setDisplayName(Main.getInstance().getPrefix() + "§cDiese Map existiert bereits!");
                            }
                        } else if(args[1].equalsIgnoreCase("delete")) {
                            if(Main.getInstance().getMapManager().doesMapExist(args[2])) {
                                Main.getInstance().getMapManager().deleteMap(args[2]);
                                player.sendMessage(Main.getInstance().getPrefix() + "§7Die Map §c" + args[2].toString() + "§7 wurde gelöscht!");
                            } else {
                                player.sendMessage(Main.getInstance().getPrefix() + "§cDiese Map existiert nicht!");
                            }
                        } else if(args[1].equalsIgnoreCase("info")) {
                            if(Main.getInstance().getMapManager().doesMapExist(args[2])) {
                                String map = args[2];
                                player.sendMessage(Main.getInstance().getPrefix() + "§7Infos über die Map §a" + map + "§7:");
                                player.sendMessage(Main.getInstance().getPrefix() + "§7Minimale Spieler: §a" + Main.getInstance().getMapManager().getMapValue(map, "MINPLAYER"));
                                player.sendMessage(Main.getInstance().getPrefix() + "§7Maximale Spieler: §a" + Main.getInstance().getMapManager().getMapValue(map, "MAXPLAYER"));
                                player.sendMessage(Main.getInstance().getPrefix() + "§7Bordergröße: §a" + Main.getInstance().getMapManager().getMapValue(map, "BORDER"));
                            } else {
                                player.sendMessage(Main.getInstance().getPrefix() + "§cDiese Map existiert nicht!");
                            }
                        }
                    }
                } else {
                    player.sendMessage(notInSetupMode);
                }
            } else if(args.length == 4) {
                if(args[0].equalsIgnoreCase("map")) {
                    if(Main.getInstance().getMapManager().doesMapExist(args[1])) {
                        if (args[2].equalsIgnoreCase("spawn")) {
                            if (args[3].equalsIgnoreCase("add")) {
                                Main.getInstance().getMapManager().addSpawn(args[1], player.getLocation());
                                player.sendMessage(Main.getInstance().getPrefix() + "§7Ein Spawnpunkt der Map §a" + args[1].toString() + "§7 wurde gesetzt!");
                            } else if (args[3].equalsIgnoreCase("list")) {
                                int i = -1;
                                int minPlayer = (int) Main.getInstance().getMapManager().getMapValue(args[1], "MINPLAYER");
                                int maxPlayer = (int) Main.getInstance().getMapManager().getMapValue(args[1], "MAXPLAYER");
                                player.sendMessage(Main.getInstance().getPrefix() + "§7Übersicht über die Spawnpunkte (" + minPlayer + "/" + maxPlayer + "):");
                                for (Location location : Main.getInstance().getMapManager().getSpawns(args[1])) {
                                    DecimalFormat locationFormat = new DecimalFormat("#.##");
                                    String locationString = "X: " + locationFormat.format(location.getX()) + " Y: " + locationFormat.format(location.getY()) + " Z: " + locationFormat.format(location.getZ());
                                    i++;
                                    TextComponent textComponent = new TextComponent(Main.getInstance().getPrefix() + "§a" + i + "§7 | " + locationString);
                                    textComponent.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Zur Position §ateleportieren").create()));
                                    textComponent.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/setup Map " + args[1] + " spawn tp " + i));
                                    player.spigot().sendMessage(textComponent);
                                }
                            }
                        } else if (args[2].equalsIgnoreCase("set")) {
                        if (args[3].equalsIgnoreCase("center")) {
                            Main.getInstance().getMapManager().setMapValue(args[1], "CENTER", player.getLocation());
                            player.sendMessage(Main.getInstance().getPrefix() + "§7Wert §a" + args[3] + " §7der Map §a" + args[1] + " §7gesetzt!");
                        } else if (args[3].equalsIgnoreCase("world")) {
                            Main.getInstance().getMapManager().setMapValue(args[1], "WORLD", player.getLocation().getWorld());
                            player.sendMessage(Main.getInstance().getPrefix() + "§7Wert §a" + args[3] + " §7der Map §a" + args[1] + " §7gesetzt!");
                        }
                    }
                    } else {
                        player.sendMessage(Main.getInstance().getPrefix() + "§cDiese Map existiert nicht!");
                    }
                }
            } else if(args.length == 5) {
                if (args[0].equalsIgnoreCase("map")) {
                    if (Main.getInstance().getMapManager().doesMapExist(args[1])) {
                        if (args[2].equalsIgnoreCase("spawn")) {
                            if (args[3].equalsIgnoreCase("tp")) {
                                if (Main.getInstance().getMapManager().getSpawn(args[1], Integer.parseInt(args[4])) != null) {
                                    player.teleport(Main.getInstance().getMapManager().getSpawn(args[1], Integer.parseInt(args[4])));
                                } else {
                                    player.sendMessage(Main.getInstance().getPrefix() + "§cDieser Spawnpunkt existiert nicht!");
                                }
                            } else if (args[3].equalsIgnoreCase("delete")) {
                                if (Main.getInstance().getMapManager().getSpawn(args[1], Integer.parseInt(args[4])) != null) {
                                    List<Location> spawnPoints = Main.getInstance().getMapManager().getSpawns(args[1]);
                                    spawnPoints.remove(args[4]);
                                    Main.getInstance().getMapManager().setMapValue(args[1], "SPAWNS", spawnPoints);
                                } else {
                                    player.sendMessage(Main.getInstance().getPrefix() + "§cDieser Spawnpunkt existiert nicht!");
                                }
                            }
                        } else if (args[2].equalsIgnoreCase("set")) {
                            if (args[3].equalsIgnoreCase("border") || args[3].equalsIgnoreCase("minplayer") || args[3].equalsIgnoreCase("maxplayer")) {
                                if (Integer.valueOf(args[4]) != null) {
                                    Main.getInstance().getMapManager().setMapValue(args[1], args[3].toUpperCase(), Integer.valueOf(args[4]));
                                    player.sendMessage(Main.getInstance().getPrefix() + "§7Wert §a" + args[3] + " §7der Map §a" + args[1] + " §7auf §a" + args[4] + " §7gesetzt!");
                                }
                            }
                        }
                    } else {
                        player.sendMessage(Main.getInstance().getPrefix() + "§cDiese Map existiert nicht!");
                    }
                }
            }
        } else {
            player.sendMessage(notAllowed);
        }


        return true;
    }

}
