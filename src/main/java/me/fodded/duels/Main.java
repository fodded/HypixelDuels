package me.fodded.duels;

import lombok.Getter;
import me.fodded.duels.commands.LobbyCommand;
import me.fodded.duels.commands.PlayCommand;
import me.fodded.duels.commands.staff.CreateMapCommand;
import me.fodded.duels.data.ConfigHandler;
import me.fodded.duels.data.Database;
import me.fodded.duels.listeners.EntityDamageListener;
import me.fodded.duels.listeners.PlayerJoinListener;
import me.fodded.duels.listeners.WorldBasicListener;
import me.fodded.duels.manager.LobbyManager;
import me.fodded.duels.manager.PlayerManager;
import me.fodded.duels.manager.game.GameManager;
import me.fodded.duels.manager.game.GameMap;
import me.fodded.duels.manager.tasks.KeepDayTask;
import me.fodded.duels.utils.ChatUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;

public class Main extends JavaPlugin {

    @Getter
    private static Main plugin;

    @Getter
    public Connection connection;

    @Getter
    public Database databaseInstance;

    @Getter
    public String noPermissions = ChatUtil.format("&cYou do not have permissions to use this command!");

    private KeepDayTask keepDayTask;

    @Override
    public void onEnable() {
        plugin = this;
        databaseInstance = new Database();

        new LobbyCommand();
        new PlayCommand();
        new CreateMapCommand();

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new WorldBasicListener(), this);
        getServer().getPluginManager().registerEvents(new EntityDamageListener(), this);

        this.keepDayTask = new KeepDayTask(plugin);
        this.keepDayTask.runTaskTimer(this, 0L, 100L);

        loadMaps();
        establishConnection();
    }

    @Override
    public void onDisable() {
        for(GameManager gameManager : GameManager.games) {
            for(PlayerManager playerManager : gameManager.getPlayers()) {
                gameManager.leaveGame(playerManager);
                playerManager.teleport(LobbyManager.lobbyLocation);
            }
            getLogger().info(gameManager.getGameMap().getWorldName() + " was successfully unloaded");
            gameManager.getGameMap().unload(true);
        }
    }

    private void loadMaps() {
        if(ConfigHandler.getConfigHandler().getGameInfo().getConfigurationSection("games") == null) {
            getLogger().warning("No games have been created");
            return;
        }
        FileConfiguration config = ConfigHandler.getConfigHandler().getGameInfo();
        for (String gameName : config.getConfigurationSection("games").getKeys(false)) {
            new GameManager(gameName);
        }

    }

    private void establishConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + databaseInstance.getFile());
            this.databaseInstance.createTable();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
