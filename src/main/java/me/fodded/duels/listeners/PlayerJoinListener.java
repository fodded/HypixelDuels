package me.fodded.duels.listeners;

import me.fodded.duels.Main;
import me.fodded.duels.manager.LobbyManager;
import me.fodded.duels.manager.PlayerManager;
import me.fodded.duels.manager.game.GameManager;
import me.fodded.duels.manager.tasks.UpdateScoreboardTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinListener implements Listener {

    private UpdateScoreboardTask updateScoreboardTask;

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PlayerManager playerManager = PlayerManager.getPlayerManager(player);
        playerManager.teleport(LobbyManager.lobbyLocation);
        playerManager.resetPlayer();

        this.updateScoreboardTask = new UpdateScoreboardTask(player);
        this.updateScoreboardTask.runTaskAsynchronously(Main.getPlugin());
    }

    @EventHandler
    public void onQuitEvent(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        PlayerManager playerManager = PlayerManager.getPlayerManager(player);
        GameManager gameManager = PlayerManager.currentGames.get(playerManager);
        if(gameManager != null) {
            gameManager.getPlayers().remove(playerManager);
            gameManager.leaveGame(playerManager);
        }
    }
}