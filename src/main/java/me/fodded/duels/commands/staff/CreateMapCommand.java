package me.fodded.duels.commands.staff;

import me.fodded.duels.Main;
import me.fodded.duels.data.ConfigHandler;
import me.fodded.duels.manager.game.GameManager;
import me.fodded.duels.manager.game.GameType;
import me.fodded.duels.utils.BukkitUtils;
import me.fodded.duels.utils.ChatUtil;
import me.fodded.duels.utils.FileUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class CreateMapCommand extends Command {

    private static HashMap<Location, UUID> spawnLocations = new HashMap<>();
    public CreateMapCommand() {
        super("createmap");

        try {
            SimpleCommandMap simpleCommandMap = (SimpleCommandMap) Bukkit.getServer().getClass().getDeclaredMethod("getCommandMap").invoke(Bukkit.getServer());
            simpleCommandMap.register(this.getName(), "duels", this);
        } catch (ReflectiveOperationException ex) {
            Main.getPlugin().getLogger().severe("Could not register command: " + ex);
        }
    }

    private static File sourceWorldFolder, activeWorldFolder;

    @Override
    public boolean execute(CommandSender commandSender, String s, String[] args) {
        if (!(commandSender instanceof Player)) return true;

        Player player = (Player) commandSender;

        if (!player.hasPermission("duels.createmap")) {
            player.sendMessage(Main.getPlugin().getNoPermissions());
            return true;
        }

        if (args.length != 1) {
            player.sendMessage(ChatUtil.format("&cUse /createmap <TYPE>"));
            return true;
        }

        if (GameManager.getGame(player.getWorld().getName()) != null) {
            player.sendMessage(ChatUtil.format("&cThis world already has an arena"));
            return true;
        }

        if(args[0].equalsIgnoreCase("here")) {
            Location location = player.getWorld().getBlockAt(player.getLocation()).getLocation();
            location.setPitch(0);
            location.setYaw(player.getLocation().getYaw());
            location.add(0.5, 0, 0.5);

            this.spawnLocations.put(location, player.getUniqueId());
            player.sendMessage(ChatUtil.format("&aNew location for spawn was set to " + location));
            return true;
        }

        GameType gameType = GameType.valueOf(args[0]);
        FileConfiguration config = ConfigHandler.getConfigHandler().getGameInfo();

        List<String> spawnLocations = new ArrayList<>();
        for(Map.Entry location : this.spawnLocations.entrySet()) {
            if(this.spawnLocations.get(location.getKey()).equals(player.getUniqueId())) {
                spawnLocations.add(BukkitUtils.serializeLocation((Location) location.getKey()));
            }
        }

        config.set(player.getWorld().getName() + ".gameType", gameType.toString());
        config.set(player.getWorld().getName() + ".spawnLocations", spawnLocations);
        ConfigHandler.getConfigHandler().saveGameInfo();

        copyWorld(player);
        new GameManager(player.getWorld().getName());

        player.sendMessage(ChatUtil.format("&aThe game was successfully created!"));
        return false;
    }

    private static void copyWorld(Player player) {
        File gameMapsFolder = new File(
                Main.getPlugin().getDataFolder(),
                "gameMaps"
        );

        if(!gameMapsFolder.exists()) {
            gameMapsFolder.mkdir();
        }

        sourceWorldFolder = new File(
                Bukkit.getServer().getWorldContainer(),
                player.getWorld().getName()
        );

        activeWorldFolder = new File(
                Bukkit.getWorldContainer().getParentFile(),
                sourceWorldFolder.getName()
        );

        try {
            FileUtil.copy(sourceWorldFolder, activeWorldFolder);
        } catch (IOException e) {
            Bukkit.getLogger().severe("Failed to copy " + sourceWorldFolder);
            e.printStackTrace();
        }

        spawnLocations.clear();
    }
}
