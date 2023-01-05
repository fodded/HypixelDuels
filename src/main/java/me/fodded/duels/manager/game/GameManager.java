package me.fodded.duels.manager.game;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import me.fodded.duels.Main;
import me.fodded.duels.data.ConfigHandler;
import me.fodded.duels.data.Messages;
import me.fodded.duels.manager.LobbyManager;
import me.fodded.duels.manager.PlayerManager;
import me.fodded.duels.manager.tasks.GameGoingTask;
import me.fodded.duels.manager.tasks.UpdateScoreboardTask;
import me.fodded.duels.utils.BukkitUtils;
import me.fodded.duels.utils.ChatUtil;
import me.fodded.duels.utils.TitleUtils;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Getter
public class GameManager {
    private String name;
    private World world;
    private GameType gameType;
    private GameMap gameMap;
    private GameState gameState;
    private List<Location> spawnPoints = new ArrayList<>();
    private List<PlayerManager> players = new ArrayList<>();
    public static List<GameManager> games = new ArrayList<>();

    public GameManager(String name) {
        FileConfiguration config = ConfigHandler.getConfigHandler().getGameInfo();

        this.name = name;
        this.gameType = GameType.valueOf(config.getString(name + ".gameType"));

        File gameMapsFolder = new File(
                Main.getPlugin().getDataFolder(),
                "gameMaps"
        );

        this.gameMap = new GameMap(gameMapsFolder, name, true);
        this.gameState = GameState.QUEUE;

        this.world = gameMap.getWorld();
        this.world.getEntities().forEach(Entity::remove);

        createSpawnPoints(config);
        GameManager.games.add(this);
    }

    public GameGoingTask gameGoingTask;

    public void switchGameState(GameState gameState) {
        this.gameState = gameState;
        switch(gameState) {
            case STARTING:
                this.gameGoingTask = new GameGoingTask(this);
                this.gameGoingTask.runTaskTimer(Main.getPlugin(), 0, 20);
                break;
            case ACTIVE:
                for(int i = 0; i < players.size(); i++) {
                    UpdateScoreboardTask.createHealthTab(players.get(i).getPlayer());
                    players.get(i).teleport(spawnPoints.get(i));
                    giveKit(players.get(i));
                }
                break;
            case END:
                this.gameGoingTask.cancel();
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        for(PlayerManager playerManager : getPlayers()) {
                            leaveGame(playerManager);
                            playerManager.teleport(LobbyManager.lobbyLocation);
                            playerManager.resetPlayer();
                        }
                        gameMap.unload(true);
                        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
                            new GameManager(name);
                        }, 5);
                    }
                }.runTaskLater(Main.getPlugin(), 20 * 10);
                break;
        }
    }

    public void giveKit(PlayerManager playerManager) {
        playerManager.getPlayer().getInventory().addItem(getEnchantedItem(new ItemStack(Material.DIAMOND_SWORD), Enchantment.DAMAGE_ALL, 3));
        playerManager.getPlayer().getInventory().addItem(new ItemStack(Material.FISHING_ROD));
        playerManager.getPlayer().getInventory().addItem(new ItemStack(Material.WOOD, 64));
        playerManager.getPlayer().getInventory().addItem(new ItemStack(Material.WATER_BUCKET));
        playerManager.getPlayer().getInventory().addItem(new ItemStack(Material.GOLDEN_APPLE, 6));
        playerManager.getPlayer().getInventory().addItem(getGoldenHead());
        playerManager.getPlayer().getInventory().addItem(new ItemStack(Material.LAVA_BUCKET));
        playerManager.getPlayer().getInventory().addItem(getEnchantedItem(new ItemStack(Material.BOW), Enchantment.ARROW_DAMAGE, 2));
        playerManager.getPlayer().getInventory().addItem(new ItemStack(Material.DIAMOND_AXE));

        playerManager.getPlayer().getInventory().addItem(new ItemStack(Material.WOOD, 64));
        playerManager.getPlayer().getInventory().addItem(new ItemStack(Material.LAVA_BUCKET));
        playerManager.getPlayer().getInventory().addItem(new ItemStack(Material.WATER_BUCKET));
        playerManager.getPlayer().getInventory().addItem(new ItemStack(Material.ARROW, 16));

        playerManager.getPlayer().getInventory().setItem(39, getEnchantedItem(new ItemStack(Material.DIAMOND_HELMET), Enchantment.PROTECTION_ENVIRONMENTAL, 2));
        playerManager.getPlayer().getInventory().setItem(38, getEnchantedItem(new ItemStack(Material.DIAMOND_CHESTPLATE), Enchantment.PROTECTION_PROJECTILE, 2));
        playerManager.getPlayer().getInventory().setItem(37, getEnchantedItem(new ItemStack(Material.DIAMOND_LEGGINGS), Enchantment.PROTECTION_ENVIRONMENTAL, 2));
        playerManager.getPlayer().getInventory().setItem(36, getEnchantedItem(new ItemStack(Material.DIAMOND_BOOTS), Enchantment.PROTECTION_ENVIRONMENTAL, 2));

        playerManager.getPlayer().updateInventory();
    }

    private ItemStack getGoldenHead() {
        ItemStack itemStack = new ItemStack(Material.SKULL_ITEM, 3, (short) 3);
        SkullMeta itemMeta = (SkullMeta) itemStack.getItemMeta();
        itemMeta.setOwner("_GoldenHead");
        itemMeta.setDisplayName(ChatUtil.format("&6Golden Head"));
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    private ItemStack getEnchantedItem(ItemStack itemStack, Enchantment enchantment, int level) {
        ItemMeta itemMeta = itemStack.getItemMeta();
        itemMeta.addEnchant(enchantment, level, true);
        itemStack.setItemMeta(itemMeta);
        return itemStack;
    }

    public void joinGame(PlayerManager playerManager) {
        if(PlayerManager.currentGames.get(playerManager) != null) {
            GameManager gameManager = PlayerManager.currentGames.get(playerManager);
            gameManager.leaveGame(playerManager);
            gameManager.getPlayers().remove(playerManager);
        }

        players.add(playerManager);
        PlayerManager.currentGames.put(playerManager, this);

        playerManager.teleport(spawnPoints.get(players.size()-1));
        playerManager.resetPlayer();
        playerManager.getPlayer().setGameMode(GameMode.SURVIVAL);

        sendMessage(playerManager.getPlayerData().getPrefix() + playerManager.getPlayerData().getDisplayName() + " &ehas joined (&b" + players.size() + "&e/&b2&e)");

        if(players.size() >= 2) {
            switchGameState(GameState.STARTING);
        }
    }
    public void leaveGame(PlayerManager playerManager) {
        String message = playerManager.getPlayerData().getPrefix() + playerManager.getPlayerData().getDisplayName() + " &eleft the game";
        boolean flag = !getGameState().equals(GameState.STARTING) && !getGameState().equals(GameState.QUEUE);

        if(!getGameState().equals(GameState.END)) {
            sendMessage(!flag ? message + " &e(&b" + getPlayers().size() + "&e/&b2&e)" : message + ".");
        }

        PlayerManager.currentGames.put(playerManager, null);

        if(players.size() == 1 && getGameState().equals(GameState.STARTING)) {
            gameGoingTask.cancel();
            gameGoingTask = null;
            switchGameState(GameState.QUEUE);
            return;
        }

        if(players.size() == 1 && flag && !getGameState().equals(GameState.END)) {
            killPlayer(playerManager, null);
        }
    }

    public void killPlayer(PlayerManager playerManager, Player killer) {
        String textToOutput = playerManager.getPlayerData().getPrefix() + playerManager.getPlayerData().getDisplayName() + " &ewas killed by ";
        if(killer == null) {
            textToOutput = playerManager.getPlayerData().getPrefix() + playerManager.getPlayerData().getDisplayName() + " &edied.";
        } else {
            PlayerManager killerManager = PlayerManager.getPlayerManager(killer);
            textToOutput += killerManager.getPlayerData().getPrefix() + killerManager.getPlayerData().getDisplayName();
        }

        PlayerManager winnerManager = getWinner(playerManager);
        winnerManager.getPlayerData().setWins(winnerManager.getPlayerData().getWins()+1);
        winnerManager.getPlayerData().addStreak(1);
        winnerManager.getPlayerData().uploadData(winnerManager.getPlayerData());

        sendMessage(textToOutput);
        playerManager.getPlayerData().setLosses(playerManager.getPlayerData().getLosses()+1);
        playerManager.getPlayerData().addStreak(-1);
        playerManager.getPlayerData().uploadData(playerManager.getPlayerData());

        sendTitles(playerManager);
    }

    private PlayerManager getWinner(PlayerManager playerManager) {
        for(PlayerManager winner : getPlayers()) {
            if(winner != playerManager) {
                return winner;
            }
        }
        return null;
    }

    public void sendTitles(PlayerManager loser) {
        PlayerManager winner = null;
        for(PlayerManager playerManager : getPlayers()) {
            if(!playerManager.getPlayer().getUniqueId().equals(loser.getPlayer().getUniqueId())) {
                winner = playerManager;
                break;
            }
        }

        TitleUtils.sendTitle(loser.getPlayer(), Messages.titleUpLose, Messages.titleDownLose.replace("{player}", winner.getPlayerData().getPrefix() + winner.getPlayerData().getDisplayName()), 0, 60, 20);
        TitleUtils.sendTitle(winner.getPlayer(), Messages.titleUpWin, Messages.titleDownWin.replace("{player}",winner.getPlayerData().getPrefix() + winner.getPlayerData().getDisplayName()), 0, 60, 20);
    }

    public void switchToSpectator(PlayerManager playerManager) {
        playerManager.getPlayer().setGameMode(GameMode.SPECTATOR);
        playerManager.getPlayer().spigot().respawn();
        playerManager.teleport(spawnPoints.get(0));
    }

    public void sendMessage(String message) {
        for(PlayerManager playerManager : getPlayers()) {
            playerManager.getPlayer().sendMessage(ChatUtil.format(message));
        }
    }

    private void createSpawnPoints(FileConfiguration config) {
        for (String point : config.getStringList(this.name + ".spawnLocations")) {
            try {
                Location location = BukkitUtils.deserializeLocation(point, this.world);
                spawnPoints.add(location);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static GameManager getRandomGame(PlayerManager playerManager) {
        List<GameManager> games = listServers().stream()
                .filter(game -> game.getGameState().equals(GameState.QUEUE)
                        && PlayerManager.currentGames.get(playerManager) != game
                        && game.getPlayers().size() != 2).collect(Collectors.toList());

        Collections.sort(games, (s1, s2) -> Integer.compare(
                s2.getPlayers().size(),
                s1.getPlayers().size()
        ));

        GameManager game = games.stream().findFirst().orElse(null);

        if (game != null && game.getPlayers().size() == 0) {
            game = games.get(ThreadLocalRandom.current().nextInt(games.size()));
        }

        if(game == null) {
            game = createNewGame();
        }

        return game;
    }

    private static GameManager createNewGame() {
        List<String> possibleGames = new ArrayList<>();
        for (String gameName : ConfigHandler.getConfigHandler().getGameInfo().getConfigurationSection("").getKeys(false)) {
            possibleGames.add(gameName);
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(possibleGames.size());
        String randomGameName = possibleGames.get(randomIndex);
        return new GameManager(randomGameName);
    }

    private static Collection<GameManager> listServers() {
        return ImmutableList.copyOf(games);
    }

    public static GameManager getGame(String gameName) {
        for(GameManager gameManager : games) {
            if(gameManager.getWorld().getName().contains(gameName)) {
                return gameManager;
            }
        }
        return null;
    }
}
