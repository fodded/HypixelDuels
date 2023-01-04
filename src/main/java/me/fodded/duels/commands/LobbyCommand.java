package me.fodded.duels.commands;

import me.fodded.duels.Main;
import me.fodded.duels.manager.LobbyManager;
import me.fodded.duels.manager.PlayerManager;
import me.fodded.duels.manager.game.GameManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.entity.Player;

import java.util.Arrays;

public class LobbyCommand extends Command {

    public LobbyCommand() {
        super("lobby");
        setAliases(Arrays.asList("l", "hub", "h", "leave"));

        try {
            SimpleCommandMap simpleCommandMap = (SimpleCommandMap) Bukkit.getServer().getClass().getDeclaredMethod("getCommandMap").invoke(Bukkit.getServer());
            simpleCommandMap.register(this.getName(), "duels", this);
        } catch (ReflectiveOperationException ex) {
            Main.getPlugin().getLogger().severe("Could not register command: " + ex);
        }
    }

    @Override
    public boolean execute(CommandSender sender, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;

            PlayerManager playerManager = PlayerManager.getPlayerManager(player);
            GameManager gameManager = PlayerManager.currentGames.get(playerManager);
            if(gameManager != null) {
                gameManager.getPlayers().remove(playerManager);
                gameManager.leaveGame(playerManager);
            }

            playerManager.resetPlayer();
            playerManager.teleport(LobbyManager.lobbyLocation);
        }
        return false;
    }
}

