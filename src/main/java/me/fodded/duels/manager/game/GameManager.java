package me.fodded.duels.manager.game;

import com.google.common.collect.ImmutableList;
import lombok.Getter;
import me.fodded.duels.Main;
import me.fodded.duels.data.ConfigHandler;
import me.fodded.duels.manager.PlayerManager;
import me.fodded.duels.manager.tasks.GameGoingTask;
import me.fodded.duels.utils.BukkitUtils;
import me.fodded.duels.utils.ChatUtil;
import org.bukkit.*;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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

    private GameGoingTask gameGoingTask;

    public void switchGameState(GameState gameState) {
        this.gameState = gameState;
        switch(gameState) {
            case STARTING:
                this.gameGoingTask = new GameGoingTask(this);
                this.gameGoingTask.runTaskTimer(Main.getPlugin(), 0, 20);
                break;
            case ACTIVE:
                for(int i = 0; i < players.size(); i++) {
                    players.get(i).teleport(spawnPoints.get(i));
                    giveKit(players.get(i));
                }
                break;
            case END:
                this.gameGoingTask.cancel();
                break;
        }
    }

    public void giveKit(PlayerManager playerManager) {
        playerManager.getPlayer().getInventory().addItem(new ItemStack(Material.DIAMOND_SWORD));
        playerManager.getPlayer().updateInventory();
    }

    public void joinGame(PlayerManager playerManager) {
        if(PlayerManager.currentGames.get(playerManager) != null) {
            GameManager gameManager = PlayerManager.currentGames.get(playerManager);
            gameManager.leaveGame(playerManager);
        }

        players.add(playerManager);
        PlayerManager.currentGames.put(playerManager, this);

        playerManager.resetPlayer();
        playerManager.getPlayer().setGameMode(GameMode.SURVIVAL);
        playerManager.teleport(spawnPoints.get(players.size()-1));

        sendMessage(playerManager.getPlayerData().getName() + " &ehas joined (&b" + players.size() + "&e/&b2&e)");

        if(players.size() >= 2) {
            switchGameState(GameState.STARTING);
        }
    }
    public void leaveGame(PlayerManager playerManager) {
        players.remove(playerManager);
    }

    protected void sendMessage(String message) {
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
