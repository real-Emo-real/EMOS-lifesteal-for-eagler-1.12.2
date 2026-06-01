package com.realemoreal.lifesteal;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class LifestealPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        // Registers the death events so the server listens for player kills
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("EMOS Lifesteal has been enabled successfully!");
    }

    @Override
    public void onDisable() {
        getLogger().info("EMOS Lifesteal has been disabled.");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // Make sure a real player did the killing
        if (killer != null) {
            
            // In Minecraft 1.12.2, 2.0 HP equals 1 full heart container
            double victimMaxHealth = victim.getMaxHealth();
            double killerMaxHealth = killer.getMaxHealth();

            // Check if victim has more than 1 heart left before stealing
            if (victimMaxHealth > 2.0) {
                // Take 1 heart from the victim
                victim.setMaxHealth(victimMaxHealth - 2.0);
                
                // Give 1 heart to the killer
                killer.setMaxHealth(killerMaxHealth + 2.0);
                killer.setHealth(killer.getHealth() + 2.0); // Instantly heals the new heart slot
                
                killer.sendMessage("§aYou stole a heart from " + victim.getName() + "!");
                victim.sendMessage("§cYou lost a heart to " + killer.getName() + "!");
            } else {
                // If they are on their last heart and die, they run out of hearts
                victim.kickPlayer("§cYou have run out of hearts!");
                // Server bans the player upon reaching 0 hearts
                getServer().getBannedPlayers().addBan(victim.getName(), "§cEliminated! Out of hearts.", null, null);
            }
        }
    }
}
