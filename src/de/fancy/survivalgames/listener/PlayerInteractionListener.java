package de.fancy.survivalgames.listener;

import de.fancy.survivalgames.Main;
import de.fancy.survivalgames.utils.Achievements;
import de.fancy.survivalgames.utils.GameManager;
import de.fancy.survivalgames.utils.Gamestate;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public class PlayerInteractionListener implements Listener {
    public Main plugin;

    public PlayerInteractionListener(Main main) {
        this.plugin = main;
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Action action = event.getAction();

        if(event.hasItem()) {
            ItemStack item = event.getItem();

            if(item.getType().equals(Material.NETHER_STAR)) {
                Main.getInstance().getGameManager().openAchievements(player);
            } else if(item.getType() == Material.COMPASS) {
                if(Main.getInstance().getSpectator().getPlayers().contains(player)) {
                    Main.getInstance().getGameManager().openPlayerCompass(player);
                }
            }

            ItemStack ironHelmet = new ItemStack(Material.IRON_HELMET);
            ItemStack ironChestplate = new ItemStack(Material.IRON_CHESTPLATE);
            ItemStack ironLeggings = new ItemStack(Material.IRON_LEGGINGS);
            ItemStack ironBoots = new ItemStack(Material.IRON_BOOTS);

            if(item.equals(ironHelmet) || item.equals(ironChestplate) || item.equals(ironLeggings) || item.equals(ironBoots)) {
                if(player.getInventory().getHelmet() != null &&  player.getInventory().getChestplate() != null && player.getInventory().getLeggings() != null && player.getInventory().getBoots() != null) {
                    if(player.getInventory().getHelmet().equals(ironHelmet) && player.getInventory().getChestplate().equals(ironChestplate) && player.getInventory().getLeggings().equals(ironLeggings) && player.getInventory().getBoots().equals(ironBoots)) {
                        Achievements.addAchievement(player, Achievements.EQUIPED);
                    }
                }
            }
        }

        if(action == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();

            if(clickedBlock.getType() == Material.CHEST) {
                if(!Main.getInstance().getSpectator().getPlayers().contains(player) && (Main.getInstance().getCurrentState() == Gamestate.PROTECTION || Main.getInstance().getCurrentState() == Gamestate.INGAME || Main.getInstance().getCurrentState() == Gamestate.DEATHMATCH)) {
                    Main.getInstance().getGameManager().openLootChest(player, (Chest) clickedBlock.getState());
                } else {
                    event.setCancelled(true);
                }
            }
        }

        if(action == Action.LEFT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();

            if(Main.getInstance().getCurrentState() == Gamestate.PROTECTION || Main.getInstance().getCurrentState() == Gamestate.INGAME || Main.getInstance().getCurrentState() == Gamestate.DEATHMATCH) {
                if(!Main.getInstance().getSpectator().getPlayers().contains(player)) {
                    if (clickedBlock != null && (clickedBlock.getType() == Material.LEAVES || clickedBlock.getType() == Material.LEAVES_2 || clickedBlock.getType() == Material.WEB || clickedBlock.getType() == Material.LONG_GRASS ||clickedBlock.getType() == Material.CAKE_BLOCK)) {
                        return;
                    } else {
                        event.setCancelled(true);
                    }
                } else {
                    event.setCancelled(true);
                }
            } else {
                event.setCancelled(true);
            }
        }

        if(Main.getInstance().allowedBuilders.contains(player)) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onPlayerInteractOnEntity(PlayerInteractAtEntityEvent event) {
        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        if(entity.getType() == EntityType.ZOMBIE) {
            if(!Main.getInstance().getSpectator().getPlayers().contains(player)) {
                if (Main.getInstance().getGameManager().isValidPlayerZombie((LivingEntity) entity)) {
                    Main.getInstance().getGameManager().openPlayerZombieInventory(player, (LivingEntity) entity);
                }
            }
        }
    }

    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Location location = event.getBlockPlaced().getLocation();
        Block placedBlock = event.getBlockPlaced();

        if(placedBlock.getType() == Material.TNT) {
            event.setBuild(false);

            ItemStack tntItem = player.getInventory().getItemInHand();
            tntItem.setAmount(tntItem.getAmount()-1);
            player.getInventory().setItemInHand(tntItem);

            TNTPrimed primedTNT = (TNTPrimed) player.getWorld().spawnEntity(location, EntityType.PRIMED_TNT);
        } else if(placedBlock.getType() == Material.WEB || placedBlock.getType() == Material.CAKE_BLOCK) {
        } else {
            event.setCancelled(true);
        }

        if(Main.getInstance().allowedBuilders.contains(player)) {
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if(!(Main.getInstance().getCurrentState() == Gamestate.PROTECTION || Main.getInstance().getCurrentState() == Gamestate.INGAME || Main.getInstance().getCurrentState() == Gamestate.DEATHMATCH) || Main.getInstance().getSpectator().getPlayers().contains(event.getPlayer())) {
            event.setCancelled(true);
        }
    }

     @EventHandler
     public void onPlayerPickupItem(PlayerPickupItemEvent event) {
         if(!(Main.getInstance().getCurrentState() == Gamestate.PROTECTION || Main.getInstance().getCurrentState() == Gamestate.INGAME || Main.getInstance().getCurrentState() == Gamestate.DEATHMATCH) || Main.getInstance().getSpectator().getPlayers().contains(event.getPlayer())) {
             event.setCancelled(true);
         }
     }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if(Main.getInstance().getCurrentState() == Gamestate.PREPARATION) {
            if(!event.getFrom().toVector().equals(event.getTo().toVector())) {
                Vector oldPosition = event.getFrom().toVector();

                event.getPlayer().teleport(event.getFrom());
            }
        }

        if(Main.getInstance().getCurrentState() == Gamestate.PROTECTION || Main.getInstance().getCurrentState() == Gamestate.INGAME || Main.getInstance().getCurrentState() == Gamestate.DEATHMATCH) {
            Location playerLocation = player.getLocation();
            Player closestPlayer = player;
            double closestDistance = 1000;

            for(Player livingPlayer : Main.getInstance().getLivingPlayers()) {
                if(livingPlayer != player) {
                    double distance = playerLocation.distance(livingPlayer.getLocation());

                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestPlayer = livingPlayer;
                    }
                }
            }

            closestPlayer.setCompassTarget(player.getLocation());
            player.setCompassTarget(closestPlayer.getLocation());
        }
    }

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        if(Main.getInstance().getCurrentState() != Gamestate.INGAME && Main.getInstance().getCurrentState() != Gamestate.DEATHMATCH) {
            event.setCancelled(true);
        }

        if(event.getEntityType() == EntityType.PLAYER) {
            if(Main.getInstance().getSpectator().getPlayers().contains((Player) event.getEntity())) {
                event.setCancelled(true);
            }
        }

        if(event.getEntityType() == EntityType.ZOMBIE) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityDamageEntity(EntityDamageByEntityEvent event) {
        if(event.getDamager().getType() == EntityType.PLAYER) {
            Player hittingEntity = (Player) event.getDamager();

            if(Main.getInstance().getSpectator().getPlayers().contains(hittingEntity)) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        if(!(Main.getInstance().getCurrentState() == Gamestate.PROTECTION || Main.getInstance().getCurrentState() == Gamestate.INGAME || Main.getInstance().getCurrentState() == Gamestate.DEATHMATCH) || Main.getInstance().getSpectator().getPlayers().contains(event.getEntity())) {
            event.setCancelled(true);
        }
    }
}
