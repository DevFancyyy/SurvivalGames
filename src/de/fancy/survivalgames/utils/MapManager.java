package de.fancy.survivalgames.utils;

import de.fancy.survivalgames.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class MapManager {
    public MapManager() {

    }

    public void createStandardMap(Player player) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
                if(Main.getInstance().getLocationConfiguration().get("Waiting") == null) {
                    Main.getInstance().getLocationConfiguration().createSection("Waiting");
                    Main.getInstance().getLocationConfiguration().set("Waiting.SPAWN", player.getLocation());
                    Main.getInstance().getLocationConfiguration().set("Default.WORLD", player.getLocation().getWorld());

                    Main.getInstance().getLocationConfiguration().createSection("Default");
                    Main.getInstance().getLocationConfiguration().set("Default.NAME", "Default");
                    Main.getInstance().getLocationConfiguration().set("Default.WORLD", player.getLocation().getWorld());
                    Main.getInstance().getLocationConfiguration().set("Default.CENTER", player.getLocation());
                    Main.getInstance().getLocationConfiguration().set("Default.BORDER", 250);
                    Main.getInstance().getLocationConfiguration().set("Default.MAXPLAYER", 24);
                    Main.getInstance().getLocationConfiguration().set("Default.MINPLAYER", 6);
                    Main.getInstance().getLocationConfiguration().set("Default.SPAWNS", new ArrayList<Location>());

                    saveConfig();
                }
            }
        });
    }

    public boolean doesMapExist(String map) {
        return Main.getInstance().getLocationConfiguration().isSet(map);
    }

    public Set<String> getMaps() {
        return Main.getInstance().getLocationConfiguration().getKeys(false);
    }

    public void createMap(Player player, String mapname) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
                if(!doesMapExist(mapname)) {
                    List<Location> spawnList = new ArrayList<>();

                    Main.getInstance().getLocationConfiguration().createSection(mapname);
                    Main.getInstance().getLocationConfiguration().set(mapname + ".NAME", mapname);
                    Main.getInstance().getLocationConfiguration().set(mapname + ".WOLRD", player.getWorld().getName());
                    Main.getInstance().getLocationConfiguration().set(mapname + ".CENTER", player.getLocation());
                    Main.getInstance().getLocationConfiguration().set(mapname + ".BORDER", 150);
                    Main.getInstance().getLocationConfiguration().set(mapname + ".MAXPLAYER", 24);
                    Main.getInstance().getLocationConfiguration().set(mapname + ".MINPLAYER", 6);
                    Main.getInstance().getLocationConfiguration().set(mapname + ".SPAWNS", spawnList);

                    saveConfig();
                }
            }
        });
    }

    public void deleteMap(String mapname) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
                Main.getInstance().getLocationConfiguration().set(mapname, null);
            }
        });
    }

    public void addSpawn(String mapname, Location location) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
                List<Location> locationList = (List<Location>) Main.getInstance().getLocationConfiguration().getList(mapname + ".Spawns");

                if(locationList == null) {
                    locationList = new ArrayList<>();
                }

                if(locationList.size()+1 <= (int) getMapValue(mapname, "MAXPLAYER")) {
                    locationList.add(location);
                    Main.getInstance().getLocationConfiguration().set(mapname + ".SPAWNS", locationList);
                    saveConfig();
                }
            }
        });
    }

    public void removeSpawn(String mapname, Location location) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
                List<Location> locationList = (List<Location>) Main.getInstance().getLocationConfiguration().getList(mapname + ".SPAWNS");
                locationList.remove(location);
                saveConfig();
            }
        });
    }

    public List<Location> getSpawns(String map) {
        return (List<Location>) Main.getInstance().getLocationConfiguration().getList(map + ".SPAWNS");
    }

    public Location getSpawn(String map, int index) {
        return getSpawns(map).get(index);
    }

    public Object getMapValue(String map, String key) {
        return Main.getInstance().getLocationConfiguration().get(map + "." + key.toUpperCase());
    }

    public void setMapValue(String map, String key, Object value) {
        Bukkit.getScheduler().runTaskAsynchronously(Main.getInstance(), new Runnable() {
            @Override
            public void run() {
                Main.getInstance().getLocationConfiguration().set(map + "." + key, value);

                saveConfig();
            }
        });
    }

    public void saveConfig() {
        try {
            Main.getInstance().getLocationConfiguration().save(Main.getInstance().location_file);
        } catch(IOException ex) {
            ex.printStackTrace();
        }
    }

    public void setupMap(String map) {
        World world = Bukkit.getWorld ((String) getMapValue(map, "WORLD"));
        Location worldCenter = (Location) getMapValue(map, "CENTER");
        int borderSize = (int) getMapValue(map, "BORDER");

        world.getWorldBorder().setCenter(worldCenter);
        world.getWorldBorder().setSize(borderSize);

        world.setTime(7500);
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("doDaylightCycle", "false");
    }
}
