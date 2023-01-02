package me.fodded.duels.data;

import lombok.Getter;
import me.fodded.duels.Main;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class ConfigHandler {

    private static @Getter ConfigHandler configHandler = new ConfigHandler();

    private @Getter FileConfiguration gameInfo;
    private @Getter File gameInfoFile;

    private ConfigHandler() {
        this.gameInfoFile = new File(Main.getPlugin().getDataFolder(), "gameInfo.yml");
        if (!this.gameInfoFile.exists()) {
            try {
                this.gameInfoFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.gameInfo = YamlConfiguration.loadConfiguration(this.gameInfoFile);
    }

    public void saveGameInfo() {
        try {
            this.gameInfo.save(this.gameInfoFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
