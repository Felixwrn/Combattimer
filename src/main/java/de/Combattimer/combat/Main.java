package de.combattimer.combat;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class CombatTagPlugin extends JavaPlugin implements Listener {
    import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class CombatTagPlugin extends JavaPlugin implements Listener {

    private HashMap<UUID, Long> combatTimer = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);

        // Actionbar Task
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (UUID uuid : combatTimer.keySet()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player == null) continue;

                long timeLeft = (combatTimer.get(uuid) - System.currentTimeMillis()) / 1000;

                if (timeLeft <= 0) {
                    combatTimer.remove(uuid);
                    player.sendMessage(color(getConfig().getString("messages.combat-end")));
                    continue;
                }

                if (getConfig().getBoolean("actionbar.enabled")) {
                    String msg = getConfig().getString("actionbar.text")
                            .replace("{time}", String.valueOf(timeLeft));
                    player.sendActionBar(color(msg));
                }
            }
        }, 0L, 20L);
    }

    @EventHandler
    public void onFight(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player attacker = (Player) event.getDamager();

            long time = System.currentTimeMillis() + (getConfig().getInt("combat-time") * 1000);

            combatTimer.put(victim.getUniqueId(), time);
            combatTimer.put(attacker.getUniqueId(), time);

            victim.sendMessage(color(getConfig().getString("messages.combat-start")));
            attacker.sendMessage(color(getConfig().getString("messages.combat-start")));
        }
    }

    @EventHandler
    public void onCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();

        String cmd = event.getMessage().toLowerCase().split(" ")[0].replace("/", "");

        if (getConfig().getStringList("blocked-commands").contains(cmd)) {
            if (isInCombat(player)) {
                event.setCancelled(true);
                player.sendMessage(color(getConfig().getString("messages.no-command")));
            }
        }
    }

    private boolean isInCombat(Player player) {
        if (!combatTimer.containsKey(player.getUniqueId())) return false;

        long end = combatTimer.get(player.getUniqueId());

        if (System.currentTimeMillis() > end) {
            combatTimer.remove(player.getUniqueId());
            return false;
        }

        return true;
    }

    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
}
