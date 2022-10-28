package rewards.rewards;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;

public class TimeManager extends BukkitRunnable {

    @Override
    public void run() {

        HashMap<Player, Integer> playerJumpCounts = Rewards.playerJumpCounts;
        HashMap<Player, BossBar> playerJumpBossBars = Rewards.playerJumpBossBars;

        String winner = Rewards.winner;

        for (Player p : Bukkit.getOnlinePlayers()){
            if (!playerJumpBossBars.containsKey(p)){
                int amount = 1;

                if (p.getUniqueId().toString().equals(winner)){
                    amount = 3;
                }
                BossBar bar = Bukkit.createBossBar(ChatColor.YELLOW+"ジャンプ可能回数 : "+ChatColor.AQUA+amount, BarColor.BLUE, BarStyle.SEGMENTED_20);
                bar.setProgress(1f);
                bar.setVisible(false);
                bar.addPlayer(p);
                playerJumpCounts.put(p, amount);
                playerJumpBossBars.put(p, bar);
            }
        }

        for (Player p : playerJumpCounts.keySet()){
            double amount = 1.0;

            if (p.getUniqueId().toString().equals(winner)){
                amount = 3.0;
            }

            if (playerJumpCounts.get(p) != (int) amount){
                playerJumpCounts.replace(p, playerJumpCounts.get(p) + 1);
                playerJumpBossBars.get(p).setTitle(ChatColor.YELLOW+"ジャンプ可能回数 : "+ChatColor.AQUA+playerJumpCounts.get(p));
                playerJumpBossBars.get(p).setProgress(playerJumpCounts.get(p) / amount);
            }else{
                playerJumpBossBars.get(p).setVisible(false);
            }
        }
    }
}