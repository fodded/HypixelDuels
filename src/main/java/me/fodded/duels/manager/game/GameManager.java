package me.fodded.duels.manager.game;

import me.fodded.duels.manager.PlayerManager;
import org.bukkit.Location;

import java.util.ArrayList;
import java.util.List;

public abstract class GameManager {

    public String name;
    public Integer maxDuration;
    public List<Location> spawnPoints = new ArrayList<>();
    public List<PlayerManager> players = new ArrayList<>();

    public GameManager(String name, Integer maxDuration) {
        this.name = name;
        this.maxDuration = maxDuration;
    }

    public static void giveKits(PlayerManager playerManager) {

    }
    public void joinGame(PlayerManager playerManager) {

    }
    public void leaveGame(PlayerManager playerManager) {

    }
    public void removeGame(PlayerManager playerManager) {

    }
}
