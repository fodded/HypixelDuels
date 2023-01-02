package me.fodded.duels.manager.tasks;

import me.fodded.duels.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.scheduler.BukkitRunnable;

public class KeepDayTask extends BukkitRunnable {

    private Main plugin;

    public KeepDayTask(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        Bukkit.getScheduler().scheduleSyncDelayedTask(Main.getPlugin(), () -> {
            for(World world : plugin.getServer().getWorlds()) {
                world.setTime(0L);
            }
        });
    }
}
