package me.fodded.duels.listeners;

import me.fodded.duels.manager.PlayerManager;
import me.fodded.duels.utils.ChatUtil;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class PlayerClickListener implements Listener {

    private static HashMap<UUID, Long> cooldown = new HashMap<>();

    @EventHandler
    public void onItemConsumeEvent(PlayerItemConsumeEvent event) {
        if(event.getItem().getType().equals(Material.GOLDEN_APPLE)) {
            event.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 5 * 20, 2));
        }
    }

    @EventHandler
    public void onInteractEvent(PlayerInteractEvent event) {
        if(event.getItem() == null) {
            return;
        }

        Player player = event.getPlayer();

        if(event.getItem().getType().equals(Material.SKULL_ITEM)) {
            event.setCancelled(true);

            if(isCooldown(player.getUniqueId())) {
                player.sendMessage(ChatUtil.format("&cPlease wait a bit before doing this!"));
                return;
            }

            applyGHeadEffects(event, player);
            return;
        }

        if(event.getItem().getType().getId() != 351) {
            return;
        }

        ItemStack itemStack = event.getItem();
        ItemMeta itemMeta = itemStack.getItemMeta();

        if(!itemMeta.getDisplayName().contains("Players")) {
            return;
        }

        PlayerManager playerManager = PlayerManager.getPlayerManager(player);

        if(isCooldown(player.getUniqueId())) {
            player.sendMessage(ChatUtil.format("&cYou must wait &e3s &cbetween uses!"));
        } else {
            playerManager.getPlayerData().setPlayers(!playerManager.getPlayerData().isPlayers());
            playerManager.addLobbyItems();
            playerManager.hidePlayers();
            cooldown.put(player.getUniqueId(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(3));
        }
    }

    private static void applyGHeadEffects(PlayerInteractEvent event, Player player) {
        player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 10, 0));
        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 20 * 5, 2));
        player.addPotionEffect(new PotionEffect(PotionEffectType.ABSORPTION, 20 * 120, 0));

        ItemStack itemStack = event.getItem();
        itemStack.setAmount(itemStack.getAmount() - 1);
        player.getInventory().setItem(player.getInventory().getHeldItemSlot(), itemStack);
    }

    private boolean isCooldown(UUID uuid) {
        long start = cooldown.containsKey(uuid) ? cooldown.get(uuid) : 0;
        if (start > System.currentTimeMillis()) {
            double time = (start - System.currentTimeMillis()) / 1000.0;
            if (time > 0.1) {
                return true;
            }
        }
        cooldown.put(uuid, System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(3));
        return false;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if(PlayerManager.currentGames.get(PlayerManager.getPlayerManager(player)) == null) {
            event.setCancelled(true);
            return;
        }

        if(event.getSlot() <= 39 && event.getSlot() >= 36) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        event.setCancelled(true);
    }
}
