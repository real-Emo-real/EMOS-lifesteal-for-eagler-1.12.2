package com.realemoreal.lifesteal;

import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class LifestealPlugin extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
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

        if (killer != null) {
            double victimMaxHealth = victim.getMaxHealth();
            double killerMaxHealth = killer.getMaxHealth();

            if (victimMaxHealth > 2.0) {
                victim.setMaxHealth(victimMaxHealth - 2.0);
                killer.setMaxHealth(killerMaxHealth + 2.0);
                killer.setHealth(killer.getHealth() + 2.0);
                
                killer.sendMessage("§aYou stole a heart from " + victim.getName() + "!");
                victim.sendMessage("§cYou lost a heart to " + killer.getName() + "!");
            } else {
                victim.kickPlayer("§cYou have run out of hearts!");
                
                // This is the fixed line using the standard BanList, which works everywhere
                Bukkit.getBanList(BanList.Type.NAME).addBan(victim.getName(), "§cEliminated! Out of hearts.", null, null);
            }
        }
    }
}
