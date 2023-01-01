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

    private String name;
    private UUID uuid;
    private Integer wins, losses, streak;

    public PlayerData(Player player) {
        this.uuid = player.getUniqueId();
    }

    public void loadData(PlayerData playerData) {
        try {
            for (Field field : getClass().getFields()) {
                field.setAccessible(true);
                if (field.getName().equalsIgnoreCase("uuid")) {
                    continue;
                }

                PreparedStatement preparedStatement = Main.getPlugin().getConnection().prepareStatement("SELECT * FROM duels WHERE uuid=" + uuid);
                ResultSet resultSet = preparedStatement.executeQuery();
                field.set(playerData, resultSet.getObject(field.getName()));
            }
        } catch (SQLException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public void uploadData(PlayerData playerData) throws IllegalAccessException {
        String query = "UPDATE duels SET ";
        for(Field field : getClass().getFields()) {
            field.setAccessible(true);
            if(field.getName().equalsIgnoreCase("uuid")) {
                continue;
            }

            query += field.getName() + "='" + field.get(playerData) + "',";
        }

        query = query.substring(0, query.length()-1) + " WHERE uuid='" + uuid + "'";
        Database.prepareStatement(query);
    }
}
