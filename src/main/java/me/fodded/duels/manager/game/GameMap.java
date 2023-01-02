package me.fodded.duels.manager.game;

import lombok.Getter;
import me.fodded.duels.utils.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldCreator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GameMap {

    private static @Getter List<GameMap> maps = new ArrayList<>();

    private final File sourceWorldFolder;
    private File activeWorldFolder;

    private String worldName;
    private World bukkitWorld;

    public GameMap(File worldFolder, String worldName, boolean loadOnInit) {

        this.sourceWorldFolder = new File(
                worldFolder,
                worldName
        );

        maps.add(this);

        if(loadOnInit) load();
    }

    public boolean load() {
        if(isLoaded()) return true;

        this.worldName = sourceWorldFolder.getName() + "_active_" + System.currentTimeMillis();

        this.activeWorldFolder = new File(
                Bukkit.getWorldContainer().getParentFile(),
                worldName
        );

        try {
            FileUtil.copy(sourceWorldFolder, activeWorldFolder);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to load " + sourceWorldFolder);
            e.printStackTrace();
            return false;
        }

        this.bukkitWorld = Bukkit.createWorld(
                new WorldCreator(activeWorldFolder.getName())
        );

        if(bukkitWorld != null) this.bukkitWorld.setAutoSave(false);
        return isLoaded();
    }

    public void unload(boolean remove) {
        if(bukkitWorld != null) Bukkit.unloadWorld(bukkitWorld, false);
        if(activeWorldFolder != null) FileUtil.delete(activeWorldFolder);

        bukkitWorld = null;
        activeWorldFolder = null;

        if(remove)
            maps.remove(this);
    }

    public boolean isLoaded() {
        return getWorld() != null;
    }

    public World getWorld() {
        return bukkitWorld;
    }

    public String getWorldName() {
        return worldName;
    }
}
