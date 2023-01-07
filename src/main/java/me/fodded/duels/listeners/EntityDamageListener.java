package me.fodded.duels.listeners;

import me.fodded.duels.manager.LobbyManager;
import me.fodded.duels.manager.PlayerManager;
import me.fodded.duels.manager.game.GameManager;
import me.fodded.duels.manager.game.GameState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;

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

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        PlayerManager playerManager = PlayerManager.getPlayerManager(event.getEntity());
        GameManager gameManager = PlayerManager.currentGames.get(playerManager);

        if(gameManager == null) {
            return;
        }

        event.setDeathMessage(null);

        gameManager.killPlayer(playerManager, event.getEntity().getKiller());
        gameManager.switchToSpectator(playerManager);
        gameManager.switchGameState(GameState.END);
    }
}
