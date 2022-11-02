package rewards.rewards;

import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;

public class SwichTimeManager extends BukkitRunnable {
    private int nowTime;

    public ArrayList<Player> timePlayers = Rewards.timePlayers;
    public static HashMap<Player, Integer> playerEnableTime = Rewards.playerEnableTime;

    public SwichTimeManager() {
        this.nowTime = 0;
    }

    @Override
    public void run() {
        if (nowTime > 2){
            playerEnableTime.remove(timePlayers.get(0));
            timePlayers.remove(0);

            cancel();
            return;
        }
        nowTime++;
    }
}
