package me.fodded.duels.listeners;

import me.fodded.duels.manager.PlayerManager;
import me.fodded.duels.manager.game.GameManager;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

public class WorldBasicListener implements Listener {

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        PlayerManager playerManager = PlayerManager.getPlayerManager(event.getPlayer());
        GameManager gameManager = PlayerManager.currentGames.get(playerManager);

        if(gameManager == null) {
            event.setCancelled(true);
            return;
        }

        if(!event.getBlock().getType().equals(Material.WOOD)) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockIgnite(BlockIgniteEvent event) {
        if (event.getCause() == BlockIgniteEvent.IgniteCause.SPREAD){
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBlockBurn(BlockBurnEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onLeavesDecay(LeavesDecayEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        event.setCancelled(event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM);
    }

    @EventHandler
    public void onEntityExplode(EntityExplodeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onWeatherChange(WeatherChangeEvent event) {
        event.setCancelled(event.toWeatherState());
    }

    @EventHandler
    public void onBlockGrowEvent(BlockGrowEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onFoodLevelChange(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }
}
