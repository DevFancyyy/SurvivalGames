package de.fancy.survivalgames.listener;

import de.fancy.survivalgames.Main;
import de.fancy.survivalgames.utils.*;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.potion.PotionEffectType;

public class PlayerConnectionListener implements Listener {
    public Main plugin;

    public PlayerConnectionListener(Main main) {
        this.plugin = main;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if(!Main.getInstance().getMapManager().doesMapExist("Waiting") || Main.getInstance().getMapManager().getSpawns(Main.getInstance().getCurrentMap()).size() < (int) Main.getInstance().getMapManager().getMapValue(Main.getInstance().getCurrentMap(), "MAXPLAYER")) {
            Main.getInstance().getMapManager().createStandardMap(player);

            event.setJoinMessage("");
            player.sendMessage(Main.getInstance().getPrefix() + "§4FEHLER: §7Es gibt Fehler bei der §4Mapaufsetzung, kontrolliere die Maps auf Vollständigkeit§7! §7Das Spiel wird nicht beginnen.");

            return;
        }

        player.setHealth(20);
        player.setFoodLevel(20);
        player.setGameMode(GameMode.SURVIVAL);

        player.removePotionEffect(PotionEffectType.INVISIBILITY);

        Main.getInstance().getGameManager().setPlayerEquipment(player, Main.getInstance().getCurrentState());

        Bukkit.getScheduler().runTaskAsynchronously(plugin, new Runnable() {
            @Override
            public void run() {
                if(!MySQL.ifUserexists(player.getUniqueId())) {
                    MySQL.addUser(player.getUniqueId());
                }
            }
        });

        event.setJoinMessage("");

        player.setScoreboard(Main.getInstance().getScoreboard());

        if(Main.getInstance().getCurrentState() == Gamestate.LOBBY || Main.getInstance().getCurrentState() == Gamestate.ENDING) {
            event.setJoinMessage(Main.getInstance().getPrefix() + player.getDisplayName() + " §7hat das Spiel betreten!");

            player.teleport((Location) Main.getInstance().getMapManager().getMapValue("Waiting", "SPAWN"));
            player.setDisplayName("§a" + player.getName());
            Main.getInstance().getSurvivor().addPlayer(player);
        } else if(Main.getInstance().getCurrentState() == Gamestate.PREPARATION || Main.getInstance().getCurrentState() == Gamestate.PROTECTION || Main.getInstance().getCurrentState() == Gamestate.INGAME || Main.getInstance().getCurrentState() == Gamestate.DEATHMATCH) {
            player.teleport((Location) Main.getInstance().getMapManager().getMapValue(Main.getInstance().getCurrentMap(), "Center"));

            Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
                @Override
                public void run() {
                    Main.getInstance().getGameManager().setPlayerAsSpectator(player);
                }
            }, 1);
        }

        if(Main.getInstance().getCurrentState() == Gamestate.LOBBY) {
            if((int) Main.getInstance().getMapManager().getMapValue(Main.getInstance().getCurrentMap(), "MINPLAYER") <= Bukkit.getOnlinePlayers().size()) {
                GameCountdown gameStart = Main.getInstance().getGameCountdown();
                gameStart.setCountdown(60);
                gameStart.setLevelChange(true);
                gameStart.setNextGamestate(Gamestate.PREPARATION);
                if (gameStart.getTaskID() == -1) {
                    int task = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), gameStart, 0, 20);
                    gameStart.setTaskID(task);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        event.setQuitMessage("");

        if(Main.getInstance().getCurrentState() == Gamestate.LOBBY || Main.getInstance().getCurrentState() == Gamestate.ENDING) {
            event.setQuitMessage(Main.getInstance().getPrefix() + player.getDisplayName() + " §7hat das Spiel verlassen!");
        }

        if(Main.getInstance().getCurrentState() == Gamestate.LOBBY) {
            if(Bukkit.getOnlinePlayers().size() == 0) {
                Main.getInstance().getGameCountdown().cancel();
            }
        }

        if(Main.getInstance().getCurrentState() == Gamestate.PREPARATION || Main.getInstance().getCurrentState() == Gamestate.PROTECTION || Main.getInstance().getCurrentState() == Gamestate.INGAME || Main.getInstance().getCurrentState() == Gamestate.DEATHMATCH) {
            if(Main.getInstance().getLivingPlayers().contains(player)) {
                for(Player allPlayers : Bukkit.getOnlinePlayers()) {
                    allPlayers.sendMessage(Main.getInstance().getPrefix() + player.getDisplayName() + "§7 ist gestorben!\n" +
                            Main.getInstance().getPrefix() + "§7Es leben noch §a" + Main.getInstance().livingPlayers.size() + " §7Spieler!");
                }

                Main.getInstance().getGameManager().removePlayerFromGame(player);
                Main.getInstance().getGameManager().createPlayerZombie(player);
            }
        }
    }
}
