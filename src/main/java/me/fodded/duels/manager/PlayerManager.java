package me.fodded.duels.manager;

import me.fodded.duels.data.PlayerData;
import me.fodded.duels.manager.game.GameManager;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerManager {

    public static HashMap<PlayerManager, GameManager> currentGames = new HashMap<>();
    private static List<PlayerManager> playerManagerList = new ArrayList<>();
    private PlayerData playerData;

    public PlayerManager(Player player) {
        playerData = new PlayerData(player);
        playerData.loadData(playerData);
    }

    public static PlayerManager getPlayerManager(Player player) {
        if(!isInList(player)) {
            return new PlayerManager(player);
        }

        return playerManagerList.stream()
                .filter(playerManager -> player.getUniqueId().equals(playerManager.playerData.getUuid()))
                .findAny().orElse(null);
    }

    private static boolean isInList(Player player) {
        return playerManagerList.stream().anyMatch(playerManager -> playerManager.playerData.getUuid() == player.getUniqueId());
    }
}
