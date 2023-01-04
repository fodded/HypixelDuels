package me.fodded.duels.data;

import me.fodded.duels.manager.PlayerManager;
import me.fodded.duels.manager.game.GameManager;
import me.fodded.duels.manager.tasks.GameGoingTask;
import me.fodded.duels.utils.ChatUtil;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Messages {

    public static final String gameStartingMessage = "&eThe game starts in &c{count} &eseconds!";
    public static final String titleUpWin = "&6&lVICTORY!";
    public static final String titleDownWin = "{player} &fwon the duel!";
    public static final String titleUpLose = "&c&lGAME OVER!!";
    public static final String titleDownLose = "{player} &fwon the duel!";

    public static List<String> getScoreboard(PlayerManager playerManager, String directory) {
        switch (directory) {
            case "scoreboard.lobby":
                return Arrays.asList(
                        ChatUtil.format("&7" + new SimpleDateFormat("MM/dd/yy").format(System.currentTimeMillis()) + " &7Main"),
                        ChatUtil.format(""),
                        ChatUtil.format("&bYou can challenge"),
                        ChatUtil.format("&bother players by"),
                        ChatUtil.format("&btyping &e/duel <name>"),
                        ChatUtil.format(" "),
                        ChatUtil.format("&fCurrent Winstreak: &a" + playerManager.getPlayerData().getStreak()),
                        ChatUtil.format("  "),
                        ChatUtil.format("&fWins: &a" + playerManager.getPlayerData().getWins()),
                        ChatUtil.format("&fLoses: &a" + playerManager.getPlayerData().getLosses()),
                        ChatUtil.format("   "),
                        ChatUtil.format("&erankedsw.xyz"));
            case "scoreboard.waiting-lobby":
                GameGoingTask gameGoingTask = PlayerManager.currentGames.get(playerManager).gameGoingTask;
                return Arrays.asList(
                        ChatUtil.format("&7" + new SimpleDateFormat("MM/dd/yy").format(System.currentTimeMillis()) + " &7Main"),
                        ChatUtil.format(""),
                        ChatUtil.format("&fMap: &a" + PlayerManager.currentGames.get(playerManager).getName()),
                        ChatUtil.format("&fPlayers: &a" + PlayerManager.currentGames.get(playerManager).getPlayers().size() +  "/2"),
                        ChatUtil.format(" "),
                        ChatUtil.format(gameGoingTask == null ? "&fWaiting..." : "&fStarting in &a" + gameGoingTask.timeLeft + "&as"),
                        ChatUtil.format("  "),
                        ChatUtil.format("&fMode: &aUHC Duel"),
                        ChatUtil.format("&fVersion: &7v1.0"),
                        ChatUtil.format("   "),
                        ChatUtil.format("&erankedsw.xyz"));
            case "scoreboard.ingame":
                return Arrays.asList(
                        ChatUtil.format("&7" + new SimpleDateFormat("MM/dd/yy").format(System.currentTimeMillis()) + " &7Main"),
                        ChatUtil.format(""),
                        ChatUtil.format("&fTime Left: &a" + getTimeLeft(PlayerManager.currentGames.get(playerManager))),
                        ChatUtil.format(" "),
                        ChatUtil.format("&f&lOpponent:"),
                        ChatUtil.format(getOpponentAsString(playerManager)),
                        ChatUtil.format("  "),
                        ChatUtil.format("&fMode: &aUHC Duel"),
                        ChatUtil.format("&fWinstreak: &a" + playerManager.getPlayerData().getStreak()),
                        ChatUtil.format("   "),
                        ChatUtil.format("&erankedsw.xyz"));
        }

        return null;
    }

    private static String getOpponentAsString(PlayerManager playerManager) {
        PlayerManager opponent = null;
        for(PlayerManager player : PlayerManager.currentGames.get(playerManager).getPlayers()) {
            if(player.getPlayer().getUniqueId() != playerManager.getPlayer().getUniqueId()) {
                opponent = player;
            }
        }

        if(opponent == null) {
            return "&7null";
        }

        return opponent.getPlayerData().getPrefix() + opponent.getPlayerData().getDisplayName() + " &a" + (int) opponent.getPlayer().getHealth() + "&c❤";

    }

    private static String getTimeLeft(GameManager gameManager) {
        String textToOutput = "";

        long minute = TimeUnit.SECONDS.toMinutes(gameManager.gameGoingTask.timeLeft)
                        - TimeUnit.HOURS.toMinutes(TimeUnit.SECONDS.toHours(gameManager.gameGoingTask.timeLeft));

        long second = TimeUnit.SECONDS.toSeconds(gameManager.gameGoingTask.timeLeft)
                - TimeUnit.MINUTES.toSeconds(TimeUnit.SECONDS.toMinutes(gameManager.gameGoingTask.timeLeft));

        String secondString = String.valueOf(second);
        if(second < 10) {
            secondString = "0" + secondString;
        }

        textToOutput = minute + ":" + secondString;

        if(gameManager.gameGoingTask.timeLeft < 0) {
            textToOutput = "";
        }

        return textToOutput;
    }
}
