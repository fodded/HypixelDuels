package me.fodded.duels.listeners;

import me.fodded.duels.data.PlayerData;
import me.fodded.duels.manager.PlayerManager;
import me.fodded.duels.manager.game.GameManager;
import me.fodded.duels.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncPlayerChatListener implements Listener {

    @EventHandler(priority = EventPriority.HIGHEST)
    public void AsyncPlayerChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        PlayerData playerData = PlayerManager.getPlayerManager(player).getPlayerData();

        String color = playerData.getPrefix().startsWith("&7") ? "&7" : "&f";
        String prefix = playerData.getPrefix();

        GameManager gameManager = PlayerManager.currentGames.get(PlayerManager.getPlayerManager(player));
        if(gameManager != null) {
            for(PlayerManager playerManager : gameManager.getPlayers()) {
                playerManager.getPlayer().sendMessage(ChatUtil.format(prefix + player.getName() + color + ": " + event.getMessage()));
            }
            return;
        }

        for(Player p : Bukkit.getOnlinePlayers()) {
            if(p.getWorld() == player.getWorld()) {
                p.sendMessage(ChatUtil.format(prefix + player.getName() + color + ": " + event.getMessage()));
            }
        }
    }

}