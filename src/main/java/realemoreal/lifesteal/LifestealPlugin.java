package realemoreal.lifesteal;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

public class LifestealPlugin extends JavaPlugin implements Listener {

    // 2.0 health points = 1 full heart
    private final double HEALTH_PER_KILL = 2.0; 
    private final double HEALTH_LOST_ON_DEATH = 2.0;
    
    // 100.0 health points equals 50 hearts max
    private final double MAX_ALLOWED_HEALTH = 100.0; 

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("1.12.2 Lifesteal Plugin (50 Max Hearts) Enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Lifesteal Plugin Disabled.");
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        Player killer = victim.getKiller();

        // 1. Handle the Victim (Lose a heart)
        double victimMaxHealth = victim.getMaxHealth();
        double newVictimHealth = victimMaxHealth - HEALTH_LOST_ON_DEATH;

        if (newVictimHealth < 2.0) {
            victim.setMaxHealth(2.0);
            victim.sendMessage(ChatColor.RED + "You are down to your very last heart!");
        } else {
            victim.setMaxHealth(newVictimHealth);
            victim.sendMessage(ChatColor.RED + "You lost a heart! Your max health is now " + (newVictimHealth / 2) + " hearts.");
        }

        // 2. Handle the Killer (Gain a heart)
        if (killer != null && !killer.equals(victim)) {
            double killerMaxHealth = killer.getMaxHealth();
            double newKillerHealth = killerMaxHealth + HEALTH_PER_KILL;

            if (newKillerHealth <= MAX_ALLOWED_HEALTH) {
                killer.setMaxHealth(newKillerHealth);
                killer.setHealth(Math.min(killer.getHealth() + HEALTH_PER_KILL, newKillerHealth));
                killer.sendMessage(ChatColor.GREEN + "You stole a heart! Your max health is now " + (newKillerHealth / 2) + " hearts.");
            } else {
                killer.sendMessage(ChatColor.GOLD + "You hit the maximum limit of 50 hearts!");
            }
        }
    }
}
