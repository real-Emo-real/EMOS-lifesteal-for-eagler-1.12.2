package com.realemoreal.lifesteal;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LifestealPlugin extends JavaPlugin implements Listener {

    private ItemStack heartItem;
    private ItemStack reviveBeacon;
    private final String menuTitle = "§0Select Player to Revive";

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        createHeartItem();
        createReviveBeacon();
        getLogger().info("EMOS Lifesteal with fixed recipe has started!");
    }

    @Override
    public void onDisable() {
        getLogger().info("EMOS Lifesteal has shut down.");
    }

    private void createHeartItem() {
        heartItem = new ItemStack(Material.getMaterial("INK_SACK"), 1, (short) 1);
        ItemMeta meta = heartItem.getItemMeta();
        meta.setDisplayName("§c§lExtra Heart Container");
        meta.setLore(Arrays.asList("§7Right-click to permanently add", "§71 heart container to your health!"));
        heartItem.setItemMeta(meta);
    }

    private void createReviveBeacon() {
        reviveBeacon = new ItemStack(Material.BEACON);
        ItemMeta meta = reviveBeacon.getItemMeta();
        meta.setDisplayName("§b§lBeacon of Life");
        meta.setLore(Arrays.asList("§7Place this block and right-click", "§7it to select a player to revive!"));
        reviveBeacon.setItemMeta(meta);

        NamespacedKey key = new NamespacedKey(this, "beacon_of_life");
        ShapedRecipe recipe = new ShapedRecipe(key, reviveBeacon);
        
        // This creates your layout: 4 diamonds in corners, 1 nether star center, redstone in rest
        recipe.shape(
            "DRD", 
            "RNR", 
            "DRD"
        );
        
        recipe.setIngredient('D', Material.DIAMOND);
        recipe.setIngredient('N', Material.NETHER_STAR);
        recipe.setIngredient('R', Material.REDSTONE);
        Bukkit.addRecipe(recipe);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("heart")) {
            if (args.length == 0) {
                sender.sendMessage("§cUsage: /heart withdraw | /heart give <player> <item/beacon> | /heart set <player> <hearts>");
                return true;
            }

            if (args[0].equalsIgnoreCase("withdraw")) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("§cOnly players can withdraw hearts!");
                    return true;
                }
                Player player = (Player) sender;
                double maxHealth = player.getMaxHealth();

                if (maxHealth <= 20.0) {
                    player.sendMessage("§cYou cannot withdraw any more hearts! You are at baseline health.");
                    return true;
                }

                player.setMaxHealth(maxHealth - 2.0);
                player.getInventory().addItem(heartItem.clone());
                player.sendMessage("§aSuccessfully withdrew 1 heart container into your inventory!");
                return true;
            }

            if (args[0].equalsIgnoreCase("give")) {
                if (!sender.hasPermission("lifesteal.admin") && !sender.isOp()) {
                    sender.sendMessage("§cYou do not have permission to use this command.");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /heart give <player> <item/beacon>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found.");
                    return true;
                }

                if (args[2].equalsIgnoreCase("beacon")) {
                    target.getInventory().addItem(reviveBeacon.clone());
                    sender.sendMessage("§aGave 1 Beacon of Life to " + target.getName());
                } else if (args[2].equalsIgnoreCase("item")) {
                    target.getInventory().addItem(heartItem.clone());
                    sender.sendMessage("§aGave 1 Extra Heart Container to " + target.getName());
                } else {
                    sender.sendMessage("§cInvalid type! Use 'item' or 'beacon'.");
                }
                return true;
            }

            if (args[0].equalsIgnoreCase("set")) {
                if (!sender.hasPermission("lifesteal.admin") && !sender.isOp()) {
                    sender.sendMessage("§cYou do not have permission to use this command.");
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /heart set <player> <amount of hearts>");
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    sender.sendMessage("§cPlayer not found.");
                    return true;
                }

                try {
                    int hearts = Integer.parseInt(args[2]);
                    double targetHP = hearts * 2.0;
                    target.setMaxHealth(targetHP);
                    target.setHealth(targetHP);
                    sender.sendMessage("§aSet " + target.getName() + "'s maximum health to " + hearts + " hearts!");
                    target.sendMessage("§aAn administrator set your maximum health to " + hearts + " hearts.");
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cPlease provide a valid whole number for hearts.");
                }
                return true;
            }
        }
        return false;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        if (killer != null) {
            double victimMaxHealth = victim.getMaxHealth();
            if (victimMaxHealth > 2.0) {
                victim.setMaxHealth(victimMaxHealth - 2.0);
                victim.getWorld().dropItemNaturally(victim.getLocation(), heartItem.clone());
                killer.sendMessage("§aYou defeated " + victim.getName() + "! A heart item dropped on the ground.");
                victim.sendMessage("§cYou lost a heart to " + killer.getName() + "!");
            } else {
                victim.kickPlayer("§cYou have run out of hearts!");
                Bukkit.getBanList(BanList.Type.NAME).addBan(victim.getName(), "§cEliminated! Out of hearts.", null, null);
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item != null && item.hasItemMeta() && "§c§lExtra Heart Container".equals(item.getItemMeta().getDisplayName())) {
            if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                event.setCancelled(true);
                player.setMaxHealth(player.getMaxHealth() + 2.0);
                player.setHealth(player.getHealth() + 2.0);
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    player.getInventory().setItemInMainHand(null);
                }
                player.sendMessage("§aYou consumed a heart container! Your max health has increased.");
                return;
            }
        }

        if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block clickedBlock = event.getClickedBlock();
            if (clickedBlock != null && clickedBlock.getType() == Material.BEACON) {
                openReviveMenu(player);
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item != null && item.hasItemMeta() && "§b§lBeacon of Life".equals(item.getItemMeta().getDisplayName())) {
            if (Bukkit.getBannedPlayers().isEmpty()) {
                event.getPlayer().sendMessage("§cThere are no eliminated players to revive right now!");
                event.setCancelled(true);
            }
        }
    }

    private void openReviveMenu(Player player) {
        List<OfflinePlayer> bannedPlayers = new ArrayList<>(Bukkit.getBannedPlayers());
        if (bannedPlayers.isEmpty()) {
            player.sendMessage("§cThere are no eliminated players to revive right now!");
            return;
        }

        int size = ((bannedPlayers.size() / 9) + 1) * 9;
        if (size > 54) size = 54;
        Inventory inv = Bukkit.createInventory(null, size, menuTitle);

        for (OfflinePlayer banned : bannedPlayers) {
            ItemStack skull = new ItemStack(Material.getMaterial("SKULL_ITEM"), 1, (short) 3);
            SkullMeta meta = (SkullMeta) skull.getItemMeta();
            meta.setOwningPlayer(banned);
            meta.setDisplayName("§e" + banned.getName());
            meta.setLore(Arrays.asList("§7Click to sacrifice this beacon", "§7and bring " + banned.getName() + " back."));
            skull.setItemMeta(meta);
            inv.addItem(skull);
        }
        player.openInventory(inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!menuTitle.equals(event.getView().getTitle())) return;
        event.setCancelled(true);
        if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) return;
        
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        
        if (clickedItem.hasItemMeta()) {
            String targetName = clickedItem.getItemMeta().getDisplayName().substring(2);
            Bukkit.getBanList(BanList.Type.NAME).pardon(targetName);
            
            Block targetBlock = player.getTargetBlock((java.util.Set<Material>) null, 5);
            if (targetBlock != null && targetBlock.getType() == Material.BEACON) {
                targetBlock.setType(Material.AIR);
                targetBlock.getWorld().playEffect(targetBlock.getLocation(), org.bukkit.Effect.MOBSPAWNER_FLAMES, 0);
            }

            player.closeInventory();
            Bukkit.broadcastMessage("§a§l" + player.getName() + " has sacrificed a Beacon of Life to bring " + targetName + " back to life!");
        }
    }
}
