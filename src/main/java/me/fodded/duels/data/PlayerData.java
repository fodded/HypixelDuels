package me.fodded.duels.data;

import lombok.Getter;
import lombok.Setter;
import me.fodded.duels.Main;
import org.bukkit.entity.Player;

import java.lang.reflect.Field;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

@Getter @Setter
public class PlayerData {

    private String name, displayName, prefix;
    private UUID uuid;
    private Integer wins = 0, losses = 0, streak = 0;
    private boolean vanish = false, players = false;

    public PlayerData(Player player) {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
        this.displayName = player.getName();
        this.prefix = "&7";
    }

    public void addStreak(Integer streak) {
        if(getStreak() > 0 && streak < 0) { // streak: 1 and when you lose its gonna be -1
            setStreak(-1);
            return;
        }
        if(getStreak() < 0 && streak > 0) { // streak: -1 and you win its gonna be 1
            setStreak(1);
            return;
        }

        setStreak(getStreak()+streak); // if both statements above didn't work
    }

    public void loadData(PlayerData playerData) {
        try {
            String query = "INSERT INTO `duels` (";
            String values = "";

            PreparedStatement preparedStatement = Main.getPlugin().getConnection().prepareStatement(
                    "SELECT * FROM `duels` WHERE `uuid` = ?"
            );
            preparedStatement.setObject(1, playerData.getUuid().toString());
            ResultSet resultSet = preparedStatement.executeQuery();
            boolean hasData = resultSet.next();

            for (Field field : getClass().getDeclaredFields()) {
                field.setAccessible(true);

                query += "`" + field.getName() + "`, ";
                values += field.get(playerData) + ",";

                if(field.getName().equalsIgnoreCase("uuid")) {
                    continue;
                }

                if(hasData) {
                    if(field.getType().equals(boolean.class)) {
                       field.set(playerData, resultSet.getBoolean(field.getName()));
                       continue;
                    }
                    field.set(playerData, resultSet.getObject(field.getName()));
                }
            }

            if(!hasData) {
                createPlayer(query, values);
            }

            preparedStatement.close();
        } catch (SQLException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private static void createPlayer(String query, String values) throws SQLException {
        query = query.substring(0, query.length()-2) + ") VALUES (";
        values = values.substring(0, values.length()-1);

        for(int count = 0; count < values.split(",").length; count++) {
            query += "?,";
        }

        query = query.substring(0, query.length()-1) + ")";
        PreparedStatement preparedStatement = Main.getPlugin().getConnection().prepareStatement(query);

        for(int count = 0; count < values.split(",").length; count++) {
            preparedStatement.setObject(count + 1, values.split(",")[count]);
        }

        preparedStatement.executeUpdate();
        preparedStatement.close();
    }

    public void uploadData(PlayerData playerData) {
        String query = "UPDATE `duels` SET ";
        try {
            for (Field field : getClass().getDeclaredFields()) {
                field.setAccessible(true);
                if (field.getName().equalsIgnoreCase("uuid")) {
                    continue;
                }

                query += field.getName() + "='" + field.get(playerData) + "',";
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        query = query.substring(0, query.length()-1) + " WHERE `uuid` = ?";

        Database database = Main.getPlugin().getDatabaseInstance();
        database.prepareStatement(database.prepareStatement(query, playerData.getUuid()));
    }
}
