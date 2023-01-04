package me.fodded.duels.manager.tasks;

import me.fodded.duels.data.Messages;
import me.fodded.duels.manager.PlayerManager;
import me.fodded.duels.manager.game.GameManager;
import me.fodded.duels.manager.game.GameState;
import me.fodded.duels.utils.ChatUtil;
import me.fodded.duels.utils.TitleUtils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class GameGoingTask extends BukkitRunnable {

    private GameManager gameManager;
    public int timeLeft = 6;

    public GameGoingTask(GameManager gameManager) {
        this.gameManager = gameManager;
    }

    @Override
    public void run() {
        if(--timeLeft <= 0) {
            if(gameManager.getGameState().equals(GameState.STARTING)) {
                gameManager.switchGameState(GameState.ACTIVE);
                timeLeft = 8 * 60;
                return;
            }
            if(gameManager.getGameState().equals(GameState.ACTIVE)) {
                gameManager.switchGameState(GameState.END);
                timeLeft = 0;
            }
        }

        if(!gameManager.getGameState().equals(GameState.STARTING)) {
            return;
        }

        for(PlayerManager playerManager : gameManager.getPlayers()) {
            Player player = playerManager.getPlayer();
            player.playSound(player.getLocation(), Sound.NOTE_PLING, 0.3f, 2.0f);
            player.sendMessage(ChatUtil.format(Messages.gameStartingMessage.replace("{count}", timeLeft+"")));
            TitleUtils.sendTitle(player, ChatUtil.format("&c" + timeLeft), "", 0, 20, 0);
        }
    }
}