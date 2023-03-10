package me.fodded.duels.data;

import lombok.Getter;
import me.fodded.duels.Main;

import java.io.File;
import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ForkJoinPool;

public class Database {

    @Getter
    private File file;

    public Database() {
        this.file = new File("plugins/HypixelDuels/data.db");
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                Main.getPlugin().getLogger().severe("Couldn't create database file");
            }
        }
    }

    public void createTable() {
        try {
            Main.getPlugin().getConnection().createStatement().execute(
                    "CREATE TABLE IF NOT EXISTS duels (" +
                            "uuid VARCHAR(36) NOT NULL," +
                            "name VARCHAR(32) NOT NULL," +
                            "displayName VARCHAR(32) NOT NULL," +
                            "prefix VARCHAR(32) NOT NULL," +
                            "wins INTEGER DEFAULT 0," +
                            "losses INTEGER DEFAULT 0," +
                            "streak INTEGER DEFAULT 0," +
                            "players BOOLEAN NOT NULL," +
                            "vanish BOOLEAN NOT NULL," +
                            "PRIMARY KEY(uuid));"
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void prepareStatement(String query) {
        ForkJoinPool.commonPool().submit(() -> {
            try {
                PreparedStatement ps = Main.getPlugin().getConnection().prepareStatement(query);
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public void prepareStatement(PreparedStatement preparedStatement) {
        ForkJoinPool.commonPool().submit(() -> {
            try {
                preparedStatement.executeUpdate();
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        });
    }

    public static PreparedStatement prepareStatement(String query, Object... vars) {
        try {
            PreparedStatement ps = Main.getPlugin().getConnection().prepareStatement(query);
            for (int i = 0; i < vars.length; i++) {
                ps.setObject(i + 1, vars[i]);
            }
            return ps;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    public Boolean tableExists(String tableName) throws SQLException {
        String query = "SELECT name FROM sqlite_master WHERE type='table' AND name='" + tableName + "';";
        PreparedStatement preparedStatement = Main.getPlugin().getConnection().prepareStatement(query);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return true;
        }
        return false;
    }
}

