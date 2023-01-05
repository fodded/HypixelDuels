package me.fodded.duels.manager;

import lombok.Getter;
import me.fodded.duels.Main;
import me.fodded.duels.data.PlayerData;
import me.fodded.duels.manager.game.GameManager;
import me.fodded.duels.utils.ChatUtil;
import me.fodded.duels.utils.TitleUtils;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.Arrays;
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

        hidePlayers();

        if(PlayerManager.currentGames.get(this) == null) {
            addLobbyItems();
        }
    }

    private static HashMap<Player, BukkitTask> runnable = new HashMap<>();
    private BukkitTask task = null;

    public void addLobbyItems() {
        ItemStack players = new ItemStack(
                351, 1, playerData.isPlayers() ? (short) 10 : (short) 8
        );

        ItemMeta players_meta = players.getItemMeta();
        players_meta.setDisplayName(ChatUtil.format("&fPlayers: " + (playerData.isPlayers() ? "&aVisible" : "&cHidden") + " &7(Right Click)"));
        players_meta.setLore(Arrays.asList(ChatUtil.format("&7Right-click to toggle player visibility!")));
        players.setItemMeta(players_meta);

        player.getInventory().setItem(8, players);
    }

    private void updateVanish() {
        if(playerData.isVanish()) {
            if(runnable.get(player) != null) {
                runnable.get(player).cancel();
            }
            task = new BukkitRunnable() {
                @Override
                public void run() {
                    if (!player.isOnline()) {
                        cancel();
                        return;
                    }
                    if (PlayerManager.currentGames.get(this) != null) {
                        return;
                    }
                    if(!playerData.isVanish()) {
                        cancel();
                        return;
                    }
                    TitleUtils.sendActionTitle(player, ChatUtil.format("&fYou are currently &cVANISHED&7!"));
                    runnable.put(player, task);
                }

            }.runTaskTimer(Main.getPlugin(), 0L, 20L);
        }
    }

    public void hidePlayers() {
        updateVanish();
        for(Player players : Bukkit.getOnlinePlayers()) {
            if(players == player) {
                continue;
            }

            PlayerData targetData = PlayerManager.getPlayerManager(players).getPlayerData();

            GameManager game = PlayerManager.currentGames.get(PlayerManager.getPlayerManager(players));
            GameManager playerGame = PlayerManager.currentGames.get(this);

            if(playerGame != null && game != null) {
                if (game == playerGame) {
                    player.showPlayer(players);
                    players.showPlayer(player);
                    continue;
                }
            } else {
                if (players.getWorld() == player.getWorld()) {
                    if (playerData.isPlayers() && !targetData.isVanish()) {
                        player.showPlayer(players);
                    } else if (!playerData.isPlayers() || targetData.isVanish()) {
                        player.hidePlayer(players);
                    }

                    if (targetData.isPlayers() && !playerData.isVanish()) {
                        players.showPlayer(player);
                    } else if (!targetData.isPlayers() || playerData.isVanish()) {
                        players.hidePlayer(player);
                    }
                    continue;
                }

            }
            player.hidePlayer(players);
            players.hidePlayer(player);
        }
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
