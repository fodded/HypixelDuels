package me.fodded.duels.manager;

import lombok.Getter;
import me.fodded.duels.data.PlayerData;
import me.fodded.duels.manager.game.GameManager;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

@Getter
public class PlayerManager {

    public static HashMap<PlayerManager, GameManager> currentGames = new HashMap<>();
    private static List<PlayerManager> playerManagerList = new ArrayList<>();
    private PlayerData playerData;
    private Player player;

    public PlayerManager(Player player) {
        playerData = new PlayerData(player);
        playerData.loadData(playerData);
        this.player = player;
        playerManagerList.add(this);
    }

    public void teleport(Location location) {
        player.teleport(location);
    }

    public void resetPlayer() {
        player.setGameMode(GameMode.ADVENTURE);
        player.setMaxHealth(20.0);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setExhaustion(0.0f);
        player.setExp(0.0f);
        player.setLevel(0);
        player.setAllowFlight(false);
        player.setFireTicks(0);
        player.closeInventory();
        player.spigot().setCollidesWithEntities(true);

        for (PotionEffect pe : player.getActivePotionEffects()) {
            player.removePotionEffect(pe.getType());
        }

        player.getInventory().clear();
        player.getInventory().setArmorContents(new ItemStack[4]);
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
        return playerManagerList.stream().anyMatch(playerManager -> playerManager.playerData.getUuid().equals(player.getUniqueId()));
    }
}
