package de.fancy.survivalgames.utils;

import de.fancy.survivalgames.Main;
import net.minecraft.server.v1_8_R3.Entity;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.*;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Chest;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;
import org.bukkit.scoreboard.Scoreboard;

import java.util.*;

public class GameManager {
    HashMap<Chest, Inventory> chestLoot = new HashMap<>();
    HashMap<LivingEntity, Inventory> playerZombies = new HashMap<>();

    public GameManager() {

    }

    public void sendCountdownMessage(int remainingTime, Gamestate nextGamestate) {
        String message = Main.getInstance().getPrefix();

        if(remainingTime != 0) {
            if(nextGamestate == Gamestate.LOBBY) {
                message = message + "§7Der Server wird in §4" + remainingTime + "§7 Sekunden neugestartet!";
            } else if(nextGamestate == Gamestate.PREPARATION) {
                message = message + "§7Das Spiel startet in §a" + remainingTime + "§7 Sekunden!";
            } else if(nextGamestate == Gamestate.PROTECTION) {
                message = message + "§7Die Vorbereitungsphase endet in §a" + remainingTime + "§7 Sekunden!";
            } else if(nextGamestate == Gamestate.INGAME) {
                message = message + "§7Die Schutzphase endet in §a" + remainingTime + "§7 Sekunden!";
            } else if(nextGamestate == Gamestate.DEATHMATCH) {
                message = message + "§7Das Deathmatch beginnt in §c" + remainingTime + "§7 Sekunden!";
            } else if(nextGamestate == Gamestate.ENDING) {
                message = message + "§7Die Runde endet in §c" + remainingTime + "§7 Sekunden!";
            }
        } else {
            if(nextGamestate == Gamestate.LOBBY) {
                message = message + "§7Der Server wird §4jetzt §7neugestartet!";
            } else if(nextGamestate == Gamestate.PREPARATION) {
                message = message + "§7Das Spiel startet §ajetzt!";
            } else if(nextGamestate == Gamestate.PROTECTION) {
                message = message + "§7Die Vorbereitungsphase ist §abeendet§7!";
            } else if(nextGamestate == Gamestate.INGAME) {
                message = message + "§7Die Schutzphase ist §abeendet§7!";
            } else if(nextGamestate == Gamestate.DEATHMATCH) {
                message = message + "§7Das Deathmatch hat §cbegonnen§7!";
            } else {
                message = message + "§7Die Runde ist §4beendet§7!";
            }
        }

        for(Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
            player.playNote(player.getLocation(), Instrument.PIANO, Note.natural(1, Note.Tone.C));
        }
    }

    public void changeGamestate(Gamestate newGamestate) {
        GameCountdown gameCountdown = Main.getInstance().getGameCountdown();

        if(newGamestate == Gamestate.LOBBY) {
            gameCountdown.setNextGamestate(Gamestate.PREPARATION);
            gameCountdown.setCountdown(60);
            gameCountdown.setLevelChange(true);

            for(Player allPlayers : Bukkit.getOnlinePlayers()) {
                allPlayers.teleport((Location) Main.getInstance().getMapManager().getMapValue("Waiting", "SPAWN"));
            }
        } else if(newGamestate == Gamestate.PREPARATION) {
            Main.getInstance().getMapManager().setupMap(Main.getInstance().getCurrentMap());

            gameCountdown.setNextGamestate(Gamestate.PROTECTION);
            gameCountdown.setCountdown(20);
            gameCountdown.setLevelChange(true);

            List<Location> spawnList = Main.getInstance().getMapManager().getSpawns(Main.getInstance().getCurrentMap());
            int i = -1;
            for(Player allPlayers : Bukkit.getOnlinePlayers()) {
                i++;
                Main.getInstance().getLivingPlayers().add(allPlayers);
                allPlayers.getInventory().clear();
                allPlayers.teleport(spawnList.get(i));

                Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), new Runnable() {
                    @Override
                    public void run() {
                        MySQL.addPlayerStatistic(allPlayers.getUniqueId(), "pgames");
                    }
                });
            }
        } else if(newGamestate == Gamestate.PROTECTION) {
            Main.getInstance().getMapManager().setupMap(Main.getInstance().getCurrentMap());

            gameCountdown.setNextGamestate(Gamestate.INGAME);
            gameCountdown.setCountdown(20);
            gameCountdown.setLevelChange(false);
        } else if(newGamestate == Gamestate.INGAME) {
            gameCountdown.setNextGamestate(Gamestate.DEATHMATCH);
            gameCountdown.setCountdown(900);
            gameCountdown.setLevelChange(false);
        } else if(newGamestate == Gamestate.DEATHMATCH) {
            World gameWorld = Bukkit.getServer().getWorld((String) Main.getInstance().getMapManager().getMapValue(Main.getInstance().getCurrentMap(), "WORLD"));
            int borderSize = (int) Main.getInstance().getMapManager().getMapValue(Main.getInstance().getCurrentMap(), "Border");

            gameWorld.getWorldBorder().setSize(10, 150);
            gameWorld.getWorldBorder().setDamageAmount(2.0);

            gameCountdown.setNextGamestate(Gamestate.ENDING);
            gameCountdown.setCountdown(300);
            gameCountdown.setLevelChange(false);
        } else if(newGamestate == Gamestate.ENDING) {
            for(Player survivingPlayer : Main.getInstance().getLivingPlayers()) {
                Main.getInstance().getGameManager().removePlayerFromGame(survivingPlayer);
            }

            for(Player allPlayers : Bukkit.getOnlinePlayers()) {
                allPlayers.teleport((Location) Main.getInstance().getMapManager().getMapValue("Waiting", "SPAWN"));
                setPlayerEquipment(allPlayers, Gamestate.ENDING);
                allPlayers.removePotionEffect(PotionEffectType.INVISIBILITY);
            }

            gameCountdown.setNextGamestate(Gamestate.LOBBY);
            gameCountdown.setCountdown(30);
            gameCountdown.setLevelChange(true);
        }

        int countdownTask = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getInstance(), gameCountdown, 0, 20);
        gameCountdown.setTaskID(countdownTask);

        Main.getInstance().setCurrentState(newGamestate);
    }

    public void createScoreboard() {
        Scoreboard gameScoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective scoreboardObject = gameScoreboard.registerNewObjective("GameSB", "GameSB_SG");

        Team teamSurvivor = gameScoreboard.registerNewTeam("001Player");
        teamSurvivor.setDisplayName("§a");
        teamSurvivor.setPrefix("§a");

        Team teamSpectator = gameScoreboard.registerNewTeam("002Spectator");
        teamSpectator.setCanSeeFriendlyInvisibles(true);
        teamSpectator.setDisplayName("§7");
        teamSpectator.setPrefix("§7");

        scoreboardObject.setDisplayName("§aSurvivalGames");
        scoreboardObject.setDisplaySlot(DisplaySlot.SIDEBAR);

        Score mapHeadline = scoreboardObject.getScore("§7Map:");
        Score mapTag = scoreboardObject.getScore("§a" + Main.getInstance().getCurrentMap());
        Score blankSpace1 = scoreboardObject.getScore(" ");
        Score playerHeadline = scoreboardObject.getScore("§7Spieler:");
        Score playerAmount = scoreboardObject.getScore("§a" + Main.getInstance().getLivingPlayers().size());
        Score blankSpace2 = scoreboardObject.getScore("");
        Score timeHeadline = scoreboardObject.getScore("§7Verbleibende Zeit:");
        Score timeAmount = scoreboardObject.getScore("§a" + "00:00 Minuten");

        mapHeadline.setScore(7);
        mapTag.setScore(6);
        blankSpace1.setScore(5);
        playerHeadline.setScore(4);
        playerAmount.setScore(3);
        blankSpace2.setScore(2);
        timeHeadline.setScore(1);
        timeAmount.setScore(0);

        Main.getInstance().setScoreboard(gameScoreboard);
        Main.getInstance().setSurvivor(teamSurvivor);
        Main.getInstance().setSpectator(teamSpectator);

        List<Score> scores = new ArrayList<>();
        scores.add(mapTag);
        scores.add(playerAmount);
        scores.add(timeAmount);
        Main.getInstance().setScoreboardScores(scores);
    }

    public void updateScoreboard() {
        Objective scoreboardObjective = Main.getInstance().getScoreboard().getObjective("GameSB");

        Score mapScore = Main.getInstance().getScoreboardScores().get(0);
        Score playerScore = Main.getInstance().getScoreboardScores().get(1);
        Score timeScore = Main.getInstance().getScoreboardScores().get(2);

        Main.getInstance().getScoreboard().resetScores(mapScore.getEntry());
        mapScore = scoreboardObjective.getScore("§a" + Main.getInstance().getCurrentMap());
        mapScore.setScore(6);

        Main.getInstance().getScoreboard().resetScores(playerScore.getEntry());

        if(Main.getInstance().getCurrentState() == Gamestate.LOBBY || Main.getInstance().getCurrentState() == Gamestate.ENDING) {
            playerScore = scoreboardObjective.getScore("§a" + Bukkit.getOnlinePlayers().size());
        } else {
            playerScore = scoreboardObjective.getScore("§a" + Main.getInstance().getLivingPlayers().size());
        }

        playerScore.setScore(3);

        Main.getInstance().getScoreboard().resetScores(timeScore.getEntry());
        timeScore = scoreboardObjective.getScore("§a" + formatTime(Main.getInstance().getGameCountdown().getCountdown()));
        timeScore.setScore(0);

        List<Score> scores = new ArrayList<>();
        scores.add(mapScore);
        scores.add(playerScore);
        scores.add(timeScore);
        Main.getInstance().setScoreboardScores(scores);
    }

    private String formatTime(int timeInSeconds) {
        String newTime = "";

        int minutes = timeInSeconds / 60;
        int seconds = timeInSeconds - (minutes * 60);

        String minutePart = minutes + "";
        String secondsPart = seconds + "";

        if(minutes < 10) {
            minutePart = "0" + minutes;
        }

        if(seconds < 10) {
            secondsPart = "0" + seconds;
        }

        newTime = minutePart + ":" + secondsPart + " Minuten";

        return newTime;
    }

    public void setPlayerAsSpectator(Player player) {
        if(Main.getInstance().getCurrentState() != Gamestate.ENDING) {
            player.setScoreboard(Main.getInstance().getScoreboard());
            player.spigot().setCollidesWithEntities(false);

            if(Main.getInstance().getSurvivor().getPlayers().contains(player)) {
                Main.getInstance().getSurvivor().removePlayer(player);
            }

            Main.getInstance().getSpectator().addPlayer(player);

            player.setAllowFlight(true);
            player.setFlying(true);
            player.setFoodLevel(20);
            player.setDisplayName("§7" + player.getName());

            for(Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if(Main.getInstance().getLivingPlayers().contains(onlinePlayer)) {
                    onlinePlayer.hidePlayer(player);
                }

                player.showPlayer(onlinePlayer);
            }

            setPlayerEquipment(player, Main.getInstance().getCurrentState());
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, 100000, 1));
        } else {
            setPlayerEquipment(player, Gamestate.ENDING);
            player.teleport((Location) Main.getInstance().getMapManager().getMapValue("Waiting", "SPAWN"));
        }
    }

    public void setPlayerEquipment(Player player, Gamestate currentState) {
        player.getInventory().clear();

        if(currentState == Gamestate.LOBBY || currentState == Gamestate.ENDING) {
            ItemStack achievements = new ItemStack(Material.NETHER_STAR);
            ItemMeta achievementMeta = achievements.getItemMeta();
            achievementMeta.setDisplayName("§aErfolge §7(Rechtsklick)");
            achievements.setItemMeta(achievementMeta);

            player.getInventory().setItem(0, achievements);

            player.getInventory().setArmorContents(new ItemStack[] {new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR), new ItemStack(Material.AIR)});

            for(Player allPlayers : Bukkit.getOnlinePlayers()) {
                player.showPlayer(allPlayers);
            }
        } else if(currentState == Gamestate.PREPARATION || currentState == Gamestate.INGAME || currentState == Gamestate.DEATHMATCH) {
            ItemStack compass = new ItemStack(Material.COMPASS);
            ItemMeta compassMeta = compass.getItemMeta();
            compassMeta.setDisplayName("§cNavigator §7(Rechtsklick)");
            compass.setItemMeta(compassMeta);

            ItemStack achievements = new ItemStack(Material.NETHER_STAR);
            ItemMeta achievementMeta = achievements.getItemMeta();
            achievementMeta.setDisplayName("§aErfolge §7(Rechtsklick)");
            achievements.setItemMeta(achievementMeta);

            player.getInventory().setItem(0, compass);
            player.getInventory().setItem(1, achievements);
        }
    }

    public void openLootChest(Player player, Chest chest) {
        if (!chestLoot.containsKey(chest)) {
            Inventory chestInventory = Bukkit.createInventory(null, InventoryType.CHEST);

            chest.getInventory().clear();

            Random random = new Random();
            int lootAmount = random.nextInt(15);

            if(lootAmount < 2) {
                lootAmount = 2;
            }

            List<ItemStack> lootItems = new ArrayList<>();

            for(String item : Main.getInstance().loot.getStringList("items")) {
                int ID = 0;
                int subID = 0;
                int maxAmount = 0;
                int chance = 0;

                String[] itemData = item.split(",");

                for(int l = 0; l < 3; l++) {
                    itemData[l] = itemData[l].replaceAll("\\s", "");
                }

                if(itemData[0].contains(":")) {
                    String[] itemId = itemData[0].split(":");
                    ID = Integer.valueOf(itemId[0]);
                    subID = Integer.valueOf(itemId[1]);
                }

                maxAmount = Integer.valueOf(itemData[1]);
                chance = Integer.valueOf(itemData[2]);

                for(int j = 0; j < chance; j++) {
                    lootItems.add(new ItemStack(ID, maxAmount, (byte) subID));
                }
            }

            while(lootAmount != 0) {
                lootAmount--;

                Random slotRandom = new Random();
                Random itemSelected = new Random();

                int slot = slotRandom.nextInt(27);
                int itemIndex = itemSelected.nextInt(lootItems.size());

                Random itemAmountRandom = new Random();
                int itemAmount = itemAmountRandom.nextInt(lootItems.get(itemIndex).getAmount());

                ItemStack lootItemStack = lootItems.get(itemIndex);

                if(itemAmount == 0) {
                    itemAmount = 1;
                }

                lootItemStack.setAmount(itemAmount);

                chest.getInventory().setItem(slot, lootItemStack);
            }

            //player.openInventory(chest.getInventory());
            chestLoot.put(chest, chest.getInventory());
        }
    }

    public void openAchievements(Player player) {
        Inventory achievementInventory = Bukkit.getServer().createInventory (null, 18, "§aAchievements");

        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
                List<Achievements> playerAchievements = MySQL.getPlayerAchievements(player.getUniqueId());

                int i = -1;

                for(Achievements achievement : Achievements.values()) {
                    i++;

                    if(playerAchievements.contains(achievement)) {
                        ItemStack hasAchievement = new ItemStack(Material.INK_SACK, 1, (byte) 10);
                        hasAchievement.getData();
                        ItemMeta hasAchievementMeta = hasAchievement.getItemMeta();
                        List<String> desc = Arrays.asList("§7" + achievement.description, "", "§2✔");
                        hasAchievementMeta.setLore(desc);
                        hasAchievementMeta.setDisplayName("§a" + achievement.label);
                        hasAchievement.setItemMeta(hasAchievementMeta);

                        achievementInventory.setItem(i, hasAchievement);
                    } else {
                        ItemStack hasntAchievement = new ItemStack(Material.INK_SACK, 1, (byte) 8);
                        ItemMeta hasntAchievementMeta = hasntAchievement.getItemMeta();
                        List<String> desc = Arrays.asList("§7§k???", "", "§4✖");
                        hasntAchievementMeta.setLore(desc);
                        hasntAchievementMeta.setDisplayName("§c" + achievement.label);
                        hasntAchievement.setItemMeta(hasntAchievementMeta);

                        achievementInventory.setItem(i, hasntAchievement);
                    }
                }
            }
        });

        player.openInventory(achievementInventory);
    }

    public void openPlayerCompass(Player player) {
        Inventory compassInventory = Bukkit.createInventory(null, 27, "§cNavigator");

        for(Player livingPlayer : Main.getInstance().getLivingPlayers()) {
            ItemStack playerSkull = new ItemStack(Material.SKULL_ITEM, 1, (byte) 3);
            SkullMeta playerSkullMeta = (SkullMeta) playerSkull.getItemMeta();
            playerSkullMeta.setOwner(livingPlayer.getName());
            playerSkullMeta.setDisplayName("§a" + livingPlayer.getName());
            playerSkull.setItemMeta(playerSkullMeta);

            compassInventory.addItem(playerSkull);
        }

        player.openInventory(compassInventory);
    }

    public boolean isValidPlayerZombie(LivingEntity entity) {
        if(playerZombies.containsKey(entity)) {
            return true;
        } else {
            return false;
        }
    }

    public void openPlayerZombieInventory(Player player, LivingEntity entity) {
        player.openInventory(playerZombies.get(entity));
    }

    public void createPlayerZombie(Player player) {
        World world = player.getWorld();
        Location location = player.getLocation();

        ItemStack head = new ItemStack(Material.SKULL_ITEM, 1, (byte) 1);

        LivingEntity playerZombie = (LivingEntity) world.spawnEntity(location, EntityType.ZOMBIE);
        playerZombie.setCustomName("§a" + player.getName());

        ItemStack[] zombieArmor = new ItemStack[] {
                createColoredArmor(Material.LEATHER_BOOTS, Color.fromRGB(1, 84, 11)),
                createColoredArmor(Material.LEATHER_LEGGINGS, Color.fromRGB(1, 84, 11)),
                createColoredArmor(Material.LEATHER_CHESTPLATE, Color.fromRGB(1, 84, 11)),
                head
        };
        playerZombie.getEquipment().setArmorContents(zombieArmor);

        playerZombie.setCanPickupItems(false);
        playerZombie.setRemoveWhenFarAway(false);

        Entity nmsEntity = ((CraftEntity) playerZombie).getHandle();
        NBTTagCompound compound = new NBTTagCompound();
        nmsEntity.c(compound);
        compound.setByte("NoAI", (byte) 1);
        nmsEntity.f(compound);

        Inventory zombieInventory = Bukkit.createInventory(null, 36, "Inventar von " + player.getName());

        zombieInventory.setContents(player.getInventory().getContents());

        for(int i = 0; i < 4; i++) {
            if(player.getInventory().getArmorContents()[i] != null) {
                zombieInventory.addItem(player.getInventory().getArmorContents()[i]);
            }
        }

        playerZombie.teleport(location);
        playerZombies.put(playerZombie, zombieInventory);
    }

    public void removePlayerFromGame(Player player) {
        Main.getInstance().getLivingPlayers().remove(player);

        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
                MySQL.addPlayerStatistic(player.getUniqueId(), "deaths");
                MySQL.addPlayerStatistic(player.getUniqueId(), "lgames");
            }
        });

        Bukkit.getScheduler().runTaskLater(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
                if(Main.getInstance().getLivingPlayers().size() == 1) {
                    Player winner = Main.getInstance().getLivingPlayers().get(0);

                    MySQL.addPlayerStatistic(winner.getUniqueId(), "wgames");

                    for(Player allPlayers : Bukkit.getOnlinePlayers()) {
                        allPlayers.teleport((Location) Main.getInstance().getMapManager().getMapValue("Waiting", "SPAWN"));
                        setPlayerEquipment(allPlayers, Gamestate.ENDING);
                        allPlayers.removePotionEffect(PotionEffectType.INVISIBILITY);
                        allPlayers.setHealth(20);
                        allPlayers.setFoodLevel(20);
                        player.setAllowFlight(false);

                        for(OfflinePlayer spectatingPlayer : Main.getInstance().getSpectator().getPlayers()) {
                            allPlayers.showPlayer((Player) spectatingPlayer);
                        }

                        Main.getInstance().getSurvivor().addPlayer(allPlayers);

                        allPlayers.sendMessage(Main.getInstance().getPrefix() + winner.getDisplayName() + " §7hat das Spiel §agewonnen§7!");
                        allPlayers.sendTitle(winner.getDisplayName(), "§7hat die Runde gewonnen!");
                    }

                    GameCountdown gameCountdown = Main.getInstance().getGameCountdown();

                    gameCountdown.cancel();

                    Main.getInstance().setCurrentState(Gamestate.ENDING);

                    gameCountdown.setNextGamestate(Gamestate.LOBBY);
                    gameCountdown.setCountdown(30);
                    gameCountdown.setLevelChange(true);

                    Main.getInstance().getLivingPlayers().remove(0);

                    Location waitingLocation = (Location) Main.getInstance().getMapManager().getMapValue("Waiting", "SPAWN");
                    waitingLocation.getWorld().spawnEntity(waitingLocation, EntityType.FIREWORK);
                } else if(Main.getInstance().getLivingPlayers().size() == 0) {
                    for(Player allPlayers : Bukkit.getOnlinePlayers()) {
                        allPlayers.teleport((Location) Main.getInstance().getMapManager().getMapValue("Waiting", "SPAWN"));
                        setPlayerEquipment(allPlayers, Gamestate.ENDING);
                        allPlayers.removePotionEffect(PotionEffectType.INVISIBILITY);
                        allPlayers.setHealth(20);
                        allPlayers.setFoodLevel(20);
                        player.setAllowFlight(false);

                        for(OfflinePlayer spectatingPlayer : Main.getInstance().getSpectator().getPlayers()) {
                            allPlayers.showPlayer((Player) spectatingPlayer);
                        }

                        Main.getInstance().getSurvivor().addPlayer(allPlayers);

                        allPlayers.sendMessage(Main.getInstance().getPrefix() + "§7Es gibt §ckeinen §7Gewinner!");

                        allPlayers.sendTitle("§cNiemand", "§7hat die Runde gewonnen!");
                    }

                    GameCountdown gameCountdown = Main.getInstance().getGameCountdown();

                    gameCountdown.cancel();

                    Main.getInstance().setCurrentState(Gamestate.ENDING);

                    gameCountdown.setNextGamestate(Gamestate.LOBBY);
                    gameCountdown.setCountdown(30);
                    gameCountdown.setLevelChange(true);

                    Location waitingLocation = (Location) Main.getInstance().getMapManager().getMapValue("Waiting", "SPAWN");
                    waitingLocation.getWorld().spawnEntity(waitingLocation, EntityType.FIREWORK);
                }
            }
        }, 40);
    }

    public ItemStack createColoredArmor(Material material, Color color) {
        ItemStack armorStack = new ItemStack(material);
        LeatherArmorMeta armorMeta = (LeatherArmorMeta) armorStack.getItemMeta();
        armorMeta.setColor(color);
        armorStack.setItemMeta(armorMeta);

        return armorStack;
    }

}