package me.fodded.duels.manager.tasks;

import me.fodded.duels.Main;
import me.fodded.duels.data.Messages;
import me.fodded.duels.data.PlayerData;
import me.fodded.duels.manager.PlayerManager;
import me.fodded.duels.manager.game.GameManager;
import me.fodded.duels.manager.game.GameState;
import me.fodded.duels.utils.ChatUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.*;

public class UpdateScoreboardTask extends BukkitRunnable {

    public static Map<UUID, UpdateScoreboardTask> task = new HashMap<>();
    public static Map<UUID, Scoreboard> scoreboards = new HashMap<>();
    private final Player player;
    private int taskId;

    public UpdateScoreboardTask(Player player) {
        this.player = player;
        if(!task.containsKey(player.getUniqueId())) {
            initializeScoreboard(player, "scoreboard.lobby");
            task.put(player.getUniqueId(), this);
        }
    }

    @Override
    public void run() {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(Main.getPlugin(), () -> {
            if(!player.isOnline()) {
                task.remove(player.getUniqueId());
                Bukkit.getScheduler().cancelTask(taskId);
                return;
            }

            updateScoreboards(player);
        }, 0L, 10L);
    }

    private boolean hasScoreTaken(Objective o, int score) {
        for (String s : o.getScoreboard().getEntries()) {
            if(o.getScore(s).getScore() == score) return true;
        }
        return false;
    }

    private String getEntryFromScore(Objective o, int score) {
        if(o == null) return null;
        if(!hasScoreTaken(o, score)) return null;
        for (String s : o.getScoreboard().getEntries()) {
            if(o.getScore(s).getScore() == score) return o.getScore(s).getEntry();
        }
        return null;
    }

    private void replaceScore(Objective o, int score, String name) {
        if(hasScoreTaken(o, score)) {
            if(getEntryFromScore(o, score).equalsIgnoreCase(name)) return;
            if(!(getEntryFromScore(o, score).equalsIgnoreCase(name))) o.getScoreboard().resetScores(getEntryFromScore(o, score));
        }
        o.getScore(name).setScore(score);
    }

    public void refreshScoreboard(Player player, String directory) {
        if(!scoreboards.containsKey(player.getUniqueId()) || player.getScoreboard() == null) {
            initializeScoreboard(player, directory);
            return;
        }

        Scoreboard scoreboard = scoreboards.get(player.getUniqueId());
        Objective o = scoreboard.getObjective("scoreboard");

        Team lines = scoreboard.getTeam("lines");

        if(lines == null) {
            initializeScoreboard(player, directory);
            return;
        }

        lines.addEntry(ChatColor.BLACK + "" + ChatColor.WHITE);
        lines.setPrefix(ChatColor.RED + "[team]");
        lines.setSuffix(ChatColor.RED + "[team]");

        int index = 0;
        List<String> scores = getScoreboardStuff(directory, player);
        for(String string : scores) {
            replaceScore(o, ++index, string);
        }

        updateHealth(scoreboard, directory, player);
    }

    public void resetScoreboard(Player player) {
        if(!scoreboards.containsKey(player.getUniqueId())) {
            initializeScoreboard(player, "scoreboard.lobby");
        }

        Scoreboard scoreboard = scoreboards.get(player.getUniqueId());
        if(scoreboard != null) {
            for(Team team :scoreboard.getTeams()) {
                team.unregister();
            }
            scoreboard.getEntries().forEach(scoreboard::resetScores);
            scoreboard.clearSlot(DisplaySlot.SIDEBAR);
        }

        if(scoreboard.getTeam("prefix") != null) {
            scoreboard.getTeam("prefix").removeEntry(player.getName());
        }
    }

    private void initializeScoreboard(Player player, String directory) {
        Scoreboard scoreboard = Bukkit.getScoreboardManager().getNewScoreboard();
        Objective o = scoreboard.registerNewObjective("scoreboard", "dummy");

        Set<String> e = scoreboard.getEntries();
        for (String entry : e) {
            scoreboard.resetScores(entry);
        }

        o.getScoreboard().clearSlot(DisplaySlot.SIDEBAR);

        o.setDisplayName(ChatUtil.format("&e&lDUELS"));
        o.setDisplaySlot(DisplaySlot.SIDEBAR);

        Team lines = scoreboard.registerNewTeam("lines");
        lines.addEntry(ChatColor.BLACK + "" + ChatColor.WHITE);
        lines.setPrefix(ChatColor.RED + "[team]");
        lines.setSuffix(ChatColor.RED + "[team]");

        int index = 0;
        List<String> scores = getScoreboardStuff(directory, player);
        for(String string : scores) {
            o.getScore(string).setScore(++index);
        }

        player.setScoreboard(scoreboard);
        scoreboards.put(player.getUniqueId(), scoreboard);
    }

    public static void createHealthTab(Player player) {
        Scoreboard scoreboard = scoreboards.get(player.getUniqueId());

        for(Player p : Bukkit.getOnlinePlayers()) {
            int hp = (int) p.getHealth();

            Objective healthPL = scoreboard.getObjective("healthPL");
            if (healthPL == null) {
                healthPL = scoreboard.registerNewObjective("healthPL", "dummy");
            }

            healthPL.getScore(p.getName()).setScore(hp);
            healthPL.setDisplaySlot(DisplaySlot.PLAYER_LIST);

            Objective healthBN = scoreboard.getObjective("healthBN");
            if (healthBN == null) {
                healthBN = scoreboard.registerNewObjective("healthBN", "health");
            }
            healthBN.setDisplayName(ChatColor.RED + "❤");
            healthBN.getScore(p.getName()).setScore(hp);
            healthBN.setDisplaySlot(DisplaySlot.BELOW_NAME);

            setIngamePrefix(scoreboard, p, player);
        }
    }

    private static void setIngamePrefix(Scoreboard scoreboard, Player p, Player player) {
        Team team = scoreboard.getTeam(p.getUniqueId().toString().replace("-", "").substring(0, 16));

        String prefix = "&a";
        if (team == null) {
            team = scoreboard.registerNewTeam(p.getUniqueId().toString().replace("-", "").substring(0, 16));
            team.setPrefix(p == player ? ChatUtil.format(prefix) : ChatUtil.format("&c"));
            team.addEntry(p.getName());
        }

        if(team != null) {
            team.setPrefix(p == player ? ChatUtil.format(prefix) : ChatUtil.format("&c"));
        }
    }

    private void updateHealth(Scoreboard scoreboard, String directory, Player player) {
        if(directory != "scoreboard.ingame") {
            Objective healthBN = scoreboard.getObjective("healthBN");
            if(healthBN != null) {
                healthBN.unregister();
            }

            Objective healthPL = scoreboard.getObjective("healthPL");
            if(healthPL != null) {
                healthPL.unregister();
            }
            return;
        }

        for(Player p : Bukkit.getOnlinePlayers()) {
            int hp = (int) p.getHealth();

            Objective healthPL = scoreboard.getObjective("healthPL");
            if (healthPL != null) {
                healthPL.getScore(p.getName()).setScore(hp);
            }

            Objective healthBN = scoreboard.getObjective("healthBN");
            if (healthBN != null) {
                healthBN.getScore(p.getName()).setScore(hp);
            }

            if(directory != "scoreboard.ingame") {
                GameManager game = PlayerManager.currentGames.get(PlayerManager.getPlayerManager(player));
                if(game != null) {
                    if(game.getGameState().equals(GameState.QUEUE)) {
                        return;
                    }
                    Team team = scoreboard.getTeam(p.getUniqueId().toString().replace("-", "").substring(0, 16));
                    if (team != null) {
                        team.unregister();
                    }
                }
            }
        }
    }

    private void resetHealth(Player player, String directory) {
        Scoreboard scoreboard = scoreboards.get(player.getUniqueId());

        Objective healthPL = scoreboard.getObjective("healthPL");
        Objective healthBN = scoreboard.getObjective("healthBN");

        if(healthBN != null) healthBN.unregister();
        if(healthPL != null) healthPL.unregister();

        scoreboard.clearSlot(DisplaySlot.BELOW_NAME);

        for(Player p : Bukkit.getOnlinePlayers()) {
            if(p.getWorld() != player.getWorld()) {
                continue;
            }

            PlayerData playerData = PlayerManager.getPlayerManager(player).getPlayerData();
            String prefix = playerData.getPrefix() != null ? playerData.getPrefix() : "&7";

            Team team = scoreboard.getTeam(p.getUniqueId().toString().replace("-", "").substring(0, 16));
            if (team == null) {
                team = scoreboard.registerNewTeam(p.getUniqueId().toString().replace("-", "").substring(0, 16));

                team.setPrefix(ChatUtil.format(playerData.getPrefix() != null ? playerData.getPrefix() : "&7"));
                team.addEntry(p.getName());
            }

            if (team != null) {
                team.setPrefix(ChatUtil.format(prefix));
            }
        }
    }

    private List<String> getScoreboardStuff(String directory, Player player) {
        PlayerManager playerManager = PlayerManager.getPlayerManager(player);
        List<String> scoreboardStuff = Messages.getScoreboard(playerManager, directory);
        Collections.reverse(scoreboardStuff);
        return scoreboardStuff;
    }

    public void updateScoreboards(Player player) {
        GameManager game = PlayerManager.currentGames.get(PlayerManager.getPlayerManager(player));

        if(game != null) {
            if(!game.getGameState().equals(GameState.QUEUE)) {
                refreshScoreboard(player, "scoreboard.ingame");
            } else {
                refreshScoreboard(player, "scoreboard.waiting-lobby");
                resetHealth(player, "scoreboard.waiting-lobby");
            }
            return;
        }

        refreshScoreboard(player, "scoreboard.lobby");
        resetHealth(player, "scoreboard.lobby");
    }
}
