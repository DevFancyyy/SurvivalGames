package de.fancy.survivalgames.listener;

import de.fancy.survivalgames.Main;
import de.fancy.survivalgames.utils.Achievements;
import de.fancy.survivalgames.utils.GameManager;
import de.fancy.survivalgames.utils.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class PlayerKillListener implements Listener {
    public Main plugin;

    private boolean isFirstKill = true;

    public PlayerKillListener(Main main) {
        this.plugin = main;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if(player.getKiller() != null) {
            Player killer = event.getEntity().getKiller();

            event.setDeathMessage(Main.getInstance().getPrefix() + player.getDisplayName() + "§7 wurde von " + killer.getDisplayName() + " §7getötet!\n" +
                    Main.getInstance().getPrefix() + "§7Es leben noch §a" + (Main.getInstance().getLivingPlayers().size()-1) + " §7Spieler!");
            event.getDrops().clear();

            if(isFirstKill == true) {
                isFirstKill = false;
                Achievements.addAchievement(player, Achievements.FIRST_BLOOD);
                Achievements.addAchievement(killer, Achievements.FIRST_KILL);
            }

            if(player.getName().equals("FancyPlay")) {
                Achievements.addAchievement(killer, Achievements.FANCYMODE);
            }

            if(killer.getHealth() <= 1) {
                Achievements.addAchievement(killer, Achievements.LUCKY);
            }

            Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), new Runnable() {
                @Override
                public void run() {
                    MySQL.addPlayerStatistic(killer.getUniqueId(), "kills");
                }
            });
        } else {
            event.setDeathMessage(Main.getInstance().getPrefix() + player.getDisplayName() + "§7 ist gestorben!\n" +
                    Main.getInstance().getPrefix() + "§7Es leben noch §a" + (Main.getInstance().getLivingPlayers().size()-1) + " §7Spieler!");
            event.getDrops().clear();

            if(isFirstKill == true) {
                isFirstKill = false;
                Achievements.addAchievement(player, Achievements.FIRST_BLOOD);
            }
        }

        Main.getInstance().getGameManager().removePlayerFromGame(player);
        Main.getInstance().getGameManager().createPlayerZombie(player);
    }

    @EventHandler
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();

        if(Main.getInstance().getLivingPlayers().size() <= 2) {
            Bukkit.getScheduler().runTaskLater(plugin, new Runnable() {
                @Override
                public void run() {
                    Main.getInstance().getGameManager().setPlayerAsSpectator(player);
                    player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100000, 1));
                    Main.getInstance().getSpectator().addPlayer(player);
                }
            }, 1);
        }
    }

}
