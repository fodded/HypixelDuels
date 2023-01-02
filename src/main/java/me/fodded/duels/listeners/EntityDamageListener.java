package me.fodded.duels.listeners;

import me.fodded.duels.manager.LobbyManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

public class EntityDamageListener implements Listener {

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent event) {
        if(!(event.getEntity() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getEntity();
        if(player.getLocation().getWorld() == LobbyManager.lobbyLocation.getWorld()) {
            event.setCancelled(true);
        }
    }

}
