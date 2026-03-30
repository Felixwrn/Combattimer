package de.Combattimer.combat;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class Main extends JavaPlugin implements Listener {

    private final HashMap<UUID, Long> combatTimer = new HashMap<>();

    @Override
    public void onEnable() {
        // Config laden oder erstellen
        saveDefaultConfig();

        // Listener registrieren
        Bukkit.getPluginManager().registerEvents(this, this);

        // Scheduler für Actionbar-Timer
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
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(color(msg)));
                }
            }
        }, 0L, 20L); // 20 ticks = 1 Sekunde
    }

    // Spieler in den Kampf setzen
    @EventHandler
    public void onFight(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player attacker = (Player) event.getDamager();

            long endTime = System.currentTimeMillis() + (getConfig().getInt("combat-time") * 1000);

            combatTimer.put(victim.getUniqueId(), endTime);
            combatTimer.put(attacker.getUniqueId(), endTime);

            String startMsg = color(getConfig().getString("messages.combat-start"));
            victim.sendMessage(startMsg);
            attacker.sendMessage(startMsg);
        }
    }

    // Blockiert Commands im Kampf
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

    // Prüfen ob Spieler im Kampf ist
    private boolean isInCombat(Player player) {
        if (!combatTimer.containsKey(player.getUniqueId())) return false;

        long end = combatTimer.get(player.getUniqueId());
        if (System.currentTimeMillis() > end) {
            combatTimer.remove(player.getUniqueId());
            return false;
        }

        return true;
    }

    // Farbcode-Helper
    private String color(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }
}
