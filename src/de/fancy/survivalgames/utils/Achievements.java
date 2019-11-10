package de.fancy.survivalgames.utils;

import de.fancy.survivalgames.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Achievements {
    FIRST_KILL("First Kill", "Töte als erster Spieler in einer Runde einen anderen Spieler!"),
    FIRST_BLOOD("First Blood", "Werde in einer Runde als erster getötet!"),
    FANCYMODE("Fancymode", "Töte FancyPlay!"),
    LUCKY("Glückspilz", "Überlebe einen Kampf mit einem halben Herz!"),
    EQUIPED("Gerüstet", "Habe in einer Runde eine volle Eisenrüstung!");

    public final String label;
    public final String description;

    private Achievements(String label, String description) {
        this.label = label;
        this.description = description;
    }

    public static List<Achievements> getAchievementsFromString(String achievementString) {
        if(achievementString != null && achievementString != "") {
            String newAchievementString = achievementString.replace("[", "").replace("]", "");
            List<String> stringAchievements = new ArrayList<String>(Arrays.asList(newAchievementString.split(", ")));
            List<Achievements> achievementList = new ArrayList<Achievements>();

            for (String string : stringAchievements) {
                achievementList.add(getAchievementByName(string));
            }

            return achievementList;
        } else {
            return new ArrayList<>();
        }
    }

    public static Achievements getAchievementByName(String name) {
        for(Achievements achievement : Achievements.values()) {
            if(achievement.name().equals(name)) {
                return achievement;
            }
        }

        return null;
    }

    public static void addAchievement(Player player, Achievements achievement) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
                List<Achievements> playerAchievements = MySQL.getPlayerAchievements(player.getUniqueId());

                if(!playerAchievements.contains(achievement)) {
                    MySQL.addPlayerAchievement(player.getUniqueId(), achievement);

                    player.sendMessage(Main.getInstance().getPrefix() + "§7§kxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                    player.sendMessage("\n");
                    player.sendMessage(Main.getInstance().getPrefix() + "§7Du hast einen Erfolg erzielt: §a" + achievement.label);
                    player.sendMessage(Main.getInstance().getPrefix() + "§7" + achievement.description);
                    player.sendMessage("\n");
                    player.sendMessage(Main.getInstance().getPrefix() + "§7§kxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx");
                }
            }
        });
    }
}
