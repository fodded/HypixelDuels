package me.fodded.duels;

import lombok.Getter;
import me.fodded.duels.data.Database;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;

public class Main extends JavaPlugin {

    @Getter
    private static Main plugin;

    @Getter
    public Connection connection;

    @Getter
    public Database database;

    @Override
    public void onEnable() {
        plugin = this;
        database = new Database();

        establishConnection();
    }

    @Override
    public void onDisable() {

    }

    public void establishConnection() {
        try {
            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + database.getFile());
            this.database.createTable();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
