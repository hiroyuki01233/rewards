package rewards.rewards;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.UUID;

public final class Rewards extends JavaPlugin implements Listener {

    public static HashMap<Player, Integer> playerResults = new HashMap<>();
    public static HashMap<Player, Integer> playerJumpCounts = new HashMap<>();
    public static HashMap<Player, BossBar> playerJumpBossBars = new HashMap<>();

    static String winner = "d8277aca-a431-4112-b9b6-3c08a782d9a2";
    ArrayList<String> participants = new ArrayList<>(Arrays.asList("",""));

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("rewards started");
        new TimeManager().runTaskTimer(this, 0L, 40L);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (p.isSneaking() && event.getAction() == Action.LEFT_CLICK_AIR){
            if (!playerJumpCounts.containsKey(p) || !playerJumpBossBars.containsKey(p)) return;
            if (!participants.contains(p.getUniqueId().toString()) && !winner.equals(p.getUniqueId().toString())) return;

            if (playerJumpCounts.get(p) == 0){
                p.damage(0.1);
                return;
            }

            double x = p.getLocation().getDirection().getX();
            double y = p.getLocation().getDirection().getY();
            double z = p.getLocation().getDirection().getZ();

            p.setVelocity(new Vector(x,y,z));
            p.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, p.getLocation(),1);
            p.getWorld().playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 0.3f,1f);

            double amount = 1.0;

            if (p.getUniqueId().toString().equals(winner)){
                amount = 3.0;
            }

            playerJumpCounts.replace(p, playerJumpCounts.get(p) - 1);
            playerJumpBossBars.get(p).setTitle(ChatColor.YELLOW+"ジャンプ可能回数 : "+ChatColor.AQUA+playerJumpCounts.get(p));
            playerJumpBossBars.get(p).setProgress(playerJumpCounts.get(p) / amount);
            playerJumpBossBars.get(p).setVisible(true);
        }
    }
}
