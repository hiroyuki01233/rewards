package rewards.rewards;

import org.bukkit.ChatColor;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;

import java.io.File;
import java.io.IOException;
import java.util.*;

public final class Rewards extends JavaPlugin implements Listener {

    public static HashMap<Player, Integer> playerResults = new HashMap<>();
    public static HashMap<Player, Integer> playerJumpCounts = new HashMap<>();
    public static HashMap<Player, BossBar> playerJumpBossBars = new HashMap<>();

    public static HashMap<Player, Boolean> playerEnableList = new HashMap<>();
    public static HashMap<Player, Integer> playerEnableTime = new HashMap<>();
    public static ArrayList<Player> timePlayers = new ArrayList<>();

    static String winnerJump = "d8277aca-a431-4112-b9b6-3c08a782d9a2";
    ArrayList<String> participantsJump = new ArrayList<>(Collections.singletonList(""));
    ArrayList<String> woodcutterPlayers = new ArrayList<>(Collections.singletonList("d8277aca-a431-4112-b9b6-3c08a782d9a2"));
    ArrayList<String> digPlayers = new ArrayList<>(Arrays.asList("d8277aca-a431-4112-b9b6-3c08a782d9a2","aada9a01-2bca-4abe-b940-08da0102370e"));

    private File customConfigFile;
    private FileConfiguration customConfig;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("rewards started");
        new TimeManager().runTaskTimer(this, 0L, 40L);
        createCustomConfig();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public FileConfiguration getCustomConfig() {
        return this.customConfig;
    }

    private void createCustomConfig() {
        customConfigFile = new File(getDataFolder(), "custom.yml");
        if (!customConfigFile.exists()) {
            customConfigFile.getParentFile().mkdirs();
            saveResource("custom.yml", false);
        }

        customConfig = new YamlConfiguration();
        try {
            customConfig.load(customConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        /* User Edit:
            Instead of the above Try/Catch, you can also use
            YamlConfiguration.loadConfiguration(customConfigFile)
        */
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (!playerEnableList.containsKey(p) || !playerEnableList.get(p)) return;
        if (p.isSneaking() && event.getAction() == Action.LEFT_CLICK_AIR){
            if (!playerJumpCounts.containsKey(p) || !playerJumpBossBars.containsKey(p)) return;
            if (!participantsJump.contains(p.getUniqueId().toString()) && !winnerJump.equals(p.getUniqueId().toString())) return;

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

            if (p.getUniqueId().toString().equals(winnerJump)){
                amount = 3.0;
            }

            playerJumpCounts.replace(p, playerJumpCounts.get(p) - 1);
            playerJumpBossBars.get(p).setTitle(ChatColor.YELLOW+"ジャンプ可能回数 : "+ChatColor.AQUA+playerJumpCounts.get(p));
            playerJumpBossBars.get(p).setProgress(playerJumpCounts.get(p) / amount);
            playerJumpBossBars.get(p).setVisible(true);
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player p = event.getPlayer();
        if (!(participantsJump.contains(p.getUniqueId().toString()) || winnerJump.equals(p.getUniqueId().toString()) || woodcutterPlayers.contains(p.getUniqueId().toString()) || digPlayers.contains(p.getUniqueId().toString()))) return;
        if (!playerEnableList.containsKey(p)) playerEnableList.put(p, true);

        if (playerEnableTime.containsKey(p)){
            if (playerEnableTime.get(p) == 8){
                Boolean now = playerEnableList.get(p);
                p.sendMessage(":Your reward functions have been changed to "+(now ? ChatColor.BLUE : ChatColor.RED)+!now);
                playerEnableList.replace(p, !now);
                playerEnableTime.replace(p, 1);
            }else{
                playerEnableTime.replace(p,playerEnableTime.get(p)+1);
            }
        } else{
            playerEnableTime.put(p, 1);
            timePlayers.add(p);
            new SwichTimeManager().runTaskTimer(this, 0L, 20L);
        }

    }


    public boolean judgeExists(ArrayList<Block> blocks, ArrayList<Block> blocks2, Block b){
        for (Block block : blocks) {
            if (block.equals(b)) {
                return true;
            }
        }
        for (Block block : blocks2) {
            if (block.equals(b)) {
                return true;
            }
        }
        return false;
    }

    public ArrayList<Block> searchAroundBlock(Block b){
        ArrayList<Block> searchList = new ArrayList<>();
        ArrayList<Block> result = new ArrayList<>();
        searchList.add(b);

        for (int i = 0; i < 15; i++){
            if (searchList.size() == 0) break;
            Block originBlock = searchList.get(0);
            if (originBlock.getWorld().getBlockAt(originBlock.getLocation().add(0,1,0)).getType().equals(originBlock.getType()) && !judgeExists(result, searchList, originBlock.getWorld().getBlockAt(originBlock.getLocation().add(0,1,0)))) searchList.add(originBlock.getWorld().getBlockAt(originBlock.getLocation().add(0,1,0)));
            if (originBlock.getWorld().getBlockAt(originBlock.getLocation().add(1,0,0)).getType().equals(originBlock.getType()) && !judgeExists(result, searchList, originBlock.getWorld().getBlockAt(originBlock.getLocation().add(1,0,0)))) searchList.add(originBlock.getWorld().getBlockAt(originBlock.getLocation().add(1,0,0)));
            if (originBlock.getWorld().getBlockAt(originBlock.getLocation().add(-1,0,0)).getType().equals(originBlock.getType()) && !judgeExists(result, searchList, originBlock.getWorld().getBlockAt(originBlock.getLocation().add(-1,0,0)))) searchList.add(originBlock.getWorld().getBlockAt(originBlock.getLocation().add(-1,0,0)));
            if (originBlock.getWorld().getBlockAt(originBlock.getLocation().add(0,0,1)).getType().equals(originBlock.getType()) && !judgeExists(result, searchList, originBlock.getWorld().getBlockAt(originBlock.getLocation().add(0,0,1)))) searchList.add(originBlock.getWorld().getBlockAt(originBlock.getLocation().add(0,0,1)));
            if (originBlock.getWorld().getBlockAt(originBlock.getLocation().add(0,0,-1)).getType().equals(originBlock.getType()) && !judgeExists(result, searchList, originBlock.getWorld().getBlockAt(originBlock.getLocation().add(0,0,-1)))) searchList.add(originBlock.getWorld().getBlockAt(originBlock.getLocation().add(0,0,-1)));
            result.add(originBlock);
            searchList.remove(0);
        }

        return result;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e){
        Player p = e.getPlayer();
        if (!playerEnableList.containsKey(p) || !playerEnableList.get(p)) return;

        if (woodcutterPlayers.contains(p.getUniqueId().toString()) &&e.getBlock().getType().toString().contains("_LOG") && p.getInventory().getItemInMainHand().getType().toString().contains("_AXE")){
            ArrayList<Block> blocks =  searchAroundBlock(e.getBlock());
            for (Block block : blocks) {
                block.breakNaturally(p.getInventory().getItemInMainHand());
            }
        }else if (digPlayers.contains(p.getUniqueId().toString()) && p.getInventory().getItemInMainHand().getType().toString().contains("_PICKAXE")){
            int[][] arr = {{0,0}, {1,0},{-1,0},{0,1},{1,1},{-1,1},{0,-1},{1,-1},{-1,-1}};
            if (p.getLocation().getDirection().getY() < -0.5){
                for (int i = -1; i < 2; i++){
                    for (int ii = -1; ii < 2; ii++){
                        Block breakBlock = e.getBlock().getWorld().getBlockAt(e.getBlock().getLocation().add(i,0,ii));
                        breakBlock.breakNaturally(p.getInventory().getItemInMainHand());
                    }
                }
            }else if (p.getLocation().getDirection().getX() < 0.5 && p.getLocation().getDirection().getX() > -0.5){
                for (int[] loc : arr){
                    Block breakBlock = e.getBlock().getWorld().getBlockAt(e.getBlock().getLocation().add(loc[0],loc[1],0));
                    breakBlock.breakNaturally(p.getInventory().getItemInMainHand());
                }
            }else {
                for (int[] loc : arr){
                    Block breakBlock = e.getBlock().getWorld().getBlockAt(e.getBlock().getLocation().add(0,loc[1],loc[0]));
                    breakBlock.breakNaturally(p.getInventory().getItemInMainHand());
                }
            }
        }
    }

}
