package de.fancy.survivalgames.listener;

import de.fancy.survivalgames.Main;
import de.fancy.survivalgames.utils.Achievements;
import de.fancy.survivalgames.utils.Gamestate;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class PlayerInventoryInteractListener implements Listener {
    public Main plugin;

    public PlayerInventoryInteractListener(Main main) {
        this.plugin = main;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory clickedInventory = event.getClickedInventory();

        if(!(Main.getInstance().getCurrentState() == Gamestate.PROTECTION || Main.getInstance().getCurrentState() == Gamestate.INGAME || Main.getInstance().getCurrentState() == Gamestate.DEATHMATCH) || Main.getInstance().getSpectator().getPlayers().contains(player)) {
            event.setCancelled(true);
        }

        if(clickedInventory != null && clickedInventory.getName() != null) {
            if(clickedInventory.getName().equalsIgnoreCase("§aAchievements")) {
                event.setCancelled(true);
            } else if(clickedInventory.getName().equalsIgnoreCase("§cNavigator")) {
                if(Main.getInstance().getSpectator().getPlayers().contains(player)) {
                    event.setCancelled(true);

                    ItemStack clickedItem = event.getCurrentItem();

                    if(clickedItem.getType() == Material.SKULL_ITEM) {
                        Player targetPlayer = Bukkit.getPlayer(clickedItem.getItemMeta().getDisplayName().replace("§a", ""));
                        player.teleport(targetPlayer);
                    }
                }
            }
        }

        if(clickedInventory == player.getInventory()) {
            ItemStack ironHelmet = new ItemStack(Material.IRON_HELMET);
            ItemStack ironChestplate = new ItemStack(Material.IRON_CHESTPLATE);
            ItemStack ironLeggings = new ItemStack(Material.IRON_LEGGINGS);
            ItemStack ironBoots = new ItemStack(Material.IRON_BOOTS);

            if(player.getInventory().getHelmet() != null &&  player.getInventory().getChestplate() != null && player.getInventory().getLeggings() != null && player.getInventory().getBoots() != null) {
                if(player.getInventory().getHelmet().equals(ironHelmet) && player.getInventory().getChestplate().equals(ironChestplate) && player.getInventory().getLeggings().equals(ironLeggings) && player.getInventory().getBoots().equals(ironBoots)) {
                    Achievements.addAchievement(player, Achievements.EQUIPED);
                }
            }
        }

        if(Main.getInstance().allowedBuilders.contains(player)) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onInventoryItemDrag(InventoryDragEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory currentInventory = event.getInventory();

        if(currentInventory == player.getInventory()) {
            ItemStack ironHelmet = new ItemStack(Material.IRON_HELMET);
            ItemStack ironChestplate = new ItemStack(Material.IRON_CHESTPLATE);
            ItemStack ironLeggings = new ItemStack(Material.IRON_LEGGINGS);
            ItemStack ironBoots = new ItemStack(Material.IRON_BOOTS);

            if(player.getInventory().getHelmet() != null &&  player.getInventory().getChestplate() != null && player.getInventory().getLeggings() != null && player.getInventory().getBoots() != null) {
                if(player.getInventory().getHelmet().equals(ironHelmet) && player.getInventory().getChestplate().equals(ironChestplate) && player.getInventory().getLeggings().equals(ironLeggings) && player.getInventory().getBoots().equals(ironBoots)) {
                    Achievements.addAchievement(player, Achievements.EQUIPED);
                }
            }
        }
    }
}
