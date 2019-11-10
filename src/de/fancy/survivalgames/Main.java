package de.fancy.survivalgames;

import de.fancy.survivalgames.commands.CMD_Setup;
import de.fancy.survivalgames.commands.CMD_Start;
import de.fancy.survivalgames.commands.CMD_Stats;
import de.fancy.survivalgames.listener.*;
import de.fancy.survivalgames.utils.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Main extends JavaPlugin {
    public static Main instance;
    public GameManager gameManager = new GameManager();
    public MapManager mapManager = new MapManager();

    private String prefix = "§7[§aSurvivalGames§7] §f";

    public FileConfiguration config = getConfig();

    public YamlConfiguration location;
    public File location_file;

    public YamlConfiguration loot;
    public File loot_file;

    public List<Player> allowedBuilders = new ArrayList<>();

    public GameCountdown gameCountdown = new GameCountdown();

    public Gamestate currentState = Gamestate.LOBBY;
    public List<Player> livingPlayers = new ArrayList<>();
    public String map = "Default";
    public Scoreboard scoreboard;
    public List<Score> scoreboardScores;
    public Team survivor;
    public Team spectator;

    @Override
    public void onEnable() {
        instance = this;
        loadStandardConfig();
        MySQL.connect();
        MySQL.createTable();
        System.out.println("[SurvivalGames] Das Plugin wurde erfolgreich gestartet!");

        loadLocations();
        loadLoot();

        this.getServer().getPluginManager().registerEvents(new ChatListener(), this);
        this.getServer().getPluginManager().registerEvents(new PlayerConnectionListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerInteractionListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerInventoryInteractListener(this), this);
        this.getServer().getPluginManager().registerEvents(new PlayerKillListener(this), this);
        this.getServer().getPluginManager().registerEvents(new WorldEventListener(), this);

        getCommand("stats").setExecutor(new CMD_Stats(this));
        getCommand("start").setExecutor(new CMD_Start());
        getCommand("setup").setExecutor(new CMD_Setup());

        Main.getInstance().getGameManager().createScoreboard();
    }

    @Override
    public void onDisable() {
        MySQL.disconnect();
        System.out.println("[SurvivalGames] Das Plugin wurde erfolgreich gestoppt!");
    }

    public void loadLocations() {
        File locations = new File(getDataFolder(), "locations.yml");
        if (!locations.exists()) {
            try {
                getDataFolder().mkdir();
                locations.createNewFile();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(locations);
        location = config;
        location_file = locations;
    }

    public void loadLoot() {
        File lootItems = new File(getDataFolder(), "loot.yml");
        if (!lootItems.exists()) {
            try {
                getDataFolder().mkdir();
                lootItems.createNewFile();
            } catch(IOException e) {
                e.printStackTrace();
            }
        }
        YamlConfiguration config = YamlConfiguration.loadConfiguration(lootItems);
        loot = config;
        loot_file = lootItems;
    }

    public void loadStandardConfig() {
        config.addDefault("host", "localhost");
        config.addDefault("port", "3306");
        config.addDefault("database", "db");
        config.addDefault("username", "root");
        config.addDefault("password", "password");
        config.options().copyDefaults(true);
        saveConfig();
    }

    public static Main getInstance() {
        return instance;
    }

    public String getPrefix() {
        return this.prefix;
    }

    public Gamestate getCurrentState() {
        return this.currentState;
    }

    public void setCurrentState(Gamestate newState) {
        currentState = newState;
    }

    public String getCurrentMap() {
        return this.map;
    }

    public void setCurrentMap(String newMap) {
        map = newMap;
    }

    public GameCountdown getGameCountdown() {
        return this.gameCountdown;
    }

    public Scoreboard getScoreboard() {
        return this.scoreboard;
    }

    public void setScoreboard(Scoreboard newScoreboard) {
        this.scoreboard = newScoreboard;
    }

    public List<Score> getScoreboardScores() {
        return this.scoreboardScores;
    }

    public void setScoreboardScores(List<Score> newScores) {
        this.scoreboardScores = newScores;
    }

    public GameManager getGameManager() {
        return this.gameManager;
    }

    public MapManager getMapManager() {
        return this.mapManager;
    }

    public List<Player> getLivingPlayers() {
        return this.livingPlayers;
    }

    public Team getSpectator() {
        return this.spectator;
    }

    public void setSpectator(Team newTeam) {
        this.spectator = newTeam;
    }

    public Team getSurvivor() {
        return this.survivor;
    }

    public void setSurvivor(Team newTeam) {
        this.survivor = newTeam;
    }

    public YamlConfiguration getLocationConfiguration() {
        return this.location;
    }

    public YamlConfiguration getLootConfiguration() {
        return this.loot;
    }
}
