package rewards.rewards;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.boss.BossBar;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
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
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.function.Supplier;

public final class Rewards extends JavaPlugin implements Listener {

    public static HashMap<Player, Integer> playerResults = new HashMap<>();
    public static HashMap<Player, Integer> playerJumpCounts = new HashMap<>();
    public static HashMap<Player, BossBar> playerJumpBossBars = new HashMap<>();

    public static HashMap<Player, Boolean> playerEnableList = new HashMap<>();
    public static HashMap<Player, Integer> playerEnableTime = new HashMap<>();
    public static ArrayList<Player> timePlayers = new ArrayList<>();

    private File configFile;
    private FileConfiguration config;

    static int jumpCountNormal;
    static int jumpCountGreat;
    private List<String> jumpNormalPlayers;
    static List<String> jumpGreatPlayers;
    private List<String> breakPlayers;
    private List<String> breakTreePlayers;

    private int breakTreeMax;

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(this, this);
        new TimeManager().runTaskTimer(this, 0L, 40L);
        setAllConfig();
        getLogger().info("rewards started");
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public FileConfiguration getCustomConfig() {
        return this.config;
    }

    public void setAllConfig() {
        createCustomConfig();
        setConfig();
    }

    public void setConfig() {
        jumpCountNormal = config.getInt("jump.jump_count_normal");
        jumpCountGreat = config.getInt("jump.jump_count_great");
        jumpNormalPlayers = config.getStringList("jump.jump_normal_players");
        jumpGreatPlayers = config.getStringList("jump.jump_great_players");
        breakPlayers = config.getStringList("break.break_players");
        breakTreePlayers = config.getStringList("break_tree.break_tree_players");
        breakTreeMax = config.getInt("break_tree.break_tree_max");
    }

    private void createCustomConfig() {
        configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            configFile.getParentFile().mkdirs();
            saveResource("config.yml", false);
        }

        config = new YamlConfiguration();
        try {
            config.load(configFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player p = event.getPlayer();
        if (!playerEnableList.containsKey(p) || !playerEnableList.get(p)) return;
        if (p.isSneaking() && event.getAction() == Action.LEFT_CLICK_AIR) {
            if (!playerJumpCounts.containsKey(p) || !playerJumpBossBars.containsKey(p)) return;
            if (!jumpNormalPlayers.contains(p.getUniqueId().toString()) && !jumpGreatPlayers.contains(p.getUniqueId().toString()))
                return;

            if (playerJumpCounts.get(p) == 0) {
                p.damage(0.1);
                return;
            }

            double x = p.getLocation().getDirection().getX();
            double y = p.getLocation().getDirection().getY();
            double z = p.getLocation().getDirection().getZ();

            p.setVelocity(new Vector(x, y, z));
            p.getWorld().spawnParticle(Particle.EXPLOSION_LARGE, p.getLocation(), 1);
            p.getWorld().playSound(p, Sound.ENTITY_PLAYER_LEVELUP, 0.3f, 1f);

            double amount = jumpCountNormal;

            if (jumpGreatPlayers.contains(p.getUniqueId().toString())) {
                amount = jumpCountGreat;
            }

            playerJumpCounts.replace(p, playerJumpCounts.get(p) - 1);
            playerJumpBossBars.get(p).setTitle(ChatColor.YELLOW + "ジャンプ可能回数 : " + ChatColor.AQUA + playerJumpCounts.get(p));
            playerJumpBossBars.get(p).setProgress(playerJumpCounts.get(p) / amount);
            playerJumpBossBars.get(p).setVisible(true);
        }
    }

    @EventHandler
    public void onPlayerToggleSneak(PlayerToggleSneakEvent event) {
        Player p = event.getPlayer();
        if (!(jumpNormalPlayers.contains(p.getUniqueId().toString()) || jumpGreatPlayers.contains(p.getUniqueId().toString()) || breakPlayers.contains(p.getUniqueId().toString()) || breakTreePlayers.contains(p.getUniqueId().toString())))
            return;
        if (!playerEnableList.containsKey(p)) playerEnableList.put(p, true);

        if (playerEnableTime.containsKey(p)) {
            if (playerEnableTime.get(p) == 8) {
                Boolean now = playerEnableList.get(p);
                p.sendMessage(":Your reward functions have been changed to " + (now ? ChatColor.BLUE : ChatColor.RED) + !now);
                playerEnableList.replace(p, !now);
                playerEnableTime.replace(p, 1);
            } else {
                playerEnableTime.replace(p, playerEnableTime.get(p) + 1);
            }
        } else {
            playerEnableTime.put(p, 1);
            timePlayers.add(p);
            new SwichTimeManager().runTaskTimer(this, 0L, 20L);
        }

    }


    public boolean judgeExists(ArrayList<Block> blocks, ArrayList<Block> blocks2, Block b) {
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

    public ArrayList<Block> searchAroundBlock(Block b) {
        ArrayList<Block> searchList = new ArrayList<>();
        ArrayList<Block> result = new ArrayList<>();
        searchList.add(b);

        for (int i = 0; i < breakTreeMax; i++) {
            if (searchList.size() == 0) break;
            Block originBlock = searchList.get(0);
            if (originBlock.getWorld().getBlockAt(originBlock.getLocation().add(0, 1, 0)).getType().equals(originBlock.getType()) && !judgeExists(result, searchList, originBlock.getWorld().getBlockAt(originBlock.getLocation().add(0, 1, 0))))
                searchList.add(originBlock.getWorld().getBlockAt(originBlock.getLocation().add(0, 1, 0)));
            if (originBlock.getWorld().getBlockAt(originBlock.getLocation().add(1, 0, 0)).getType().equals(originBlock.getType()) && !judgeExists(result, searchList, originBlock.getWorld().getBlockAt(originBlock.getLocation().add(1, 0, 0))))
                searchList.add(originBlock.getWorld().getBlockAt(originBlock.getLocation().add(1, 0, 0)));
            if (originBlock.getWorld().getBlockAt(originBlock.getLocation().add(-1, 0, 0)).getType().equals(originBlock.getType()) && !judgeExists(result, searchList, originBlock.getWorld().getBlockAt(originBlock.getLocation().add(-1, 0, 0))))
                searchList.add(originBlock.getWorld().getBlockAt(originBlock.getLocation().add(-1, 0, 0)));
            if (originBlock.getWorld().getBlockAt(originBlock.getLocation().add(0, 0, 1)).getType().equals(originBlock.getType()) && !judgeExists(result, searchList, originBlock.getWorld().getBlockAt(originBlock.getLocation().add(0, 0, 1))))
                searchList.add(originBlock.getWorld().getBlockAt(originBlock.getLocation().add(0, 0, 1)));
            if (originBlock.getWorld().getBlockAt(originBlock.getLocation().add(0, 0, -1)).getType().equals(originBlock.getType()) && !judgeExists(result, searchList, originBlock.getWorld().getBlockAt(originBlock.getLocation().add(0, 0, -1))))
                searchList.add(originBlock.getWorld().getBlockAt(originBlock.getLocation().add(0, 0, -1)));
            result.add(originBlock);
            searchList.remove(0);
        }

        return result;
    }

    @EventHandler
    public void onBreak(BlockBreakEvent e) {
        Player p = e.getPlayer();
        if (!playerEnableList.containsKey(p) || !playerEnableList.get(p)) return;

        if (breakPlayers.contains(p.getUniqueId().toString()) && e.getBlock().getType().toString().contains("_LOG") && p.getInventory().getItemInMainHand().getType().toString().contains("_AXE")) {
            ArrayList<Block> blocks = searchAroundBlock(e.getBlock());
            for (Block block : blocks) {
                block.breakNaturally(p.getInventory().getItemInMainHand());
            }
        } else if (breakTreePlayers.contains(p.getUniqueId().toString()) && p.getInventory().getItemInMainHand().getType().toString().contains("_PICKAXE")) {
            int[][] arr = {{0, 0}, {1, 0}, {-1, 0}, {0, 1}, {1, 1}, {-1, 1}, {0, -1}, {1, -1}, {-1, -1}};
            if (p.getLocation().getDirection().getY() < -0.5) {
                for (int i = -1; i < 2; i++) {
                    for (int ii = -1; ii < 2; ii++) {
                        Block breakBlock = e.getBlock().getWorld().getBlockAt(e.getBlock().getLocation().add(i, 0, ii));
                        breakBlock.breakNaturally(p.getInventory().getItemInMainHand());
                    }
                }
            } else if (p.getLocation().getDirection().getX() < 0.5 && p.getLocation().getDirection().getX() > -0.5) {
                for (int[] loc : arr) {
                    Block breakBlock = e.getBlock().getWorld().getBlockAt(e.getBlock().getLocation().add(loc[0], loc[1], 0));
                    breakBlock.breakNaturally(p.getInventory().getItemInMainHand());
                }
            } else {
                for (int[] loc : arr) {
                    Block breakBlock = e.getBlock().getWorld().getBlockAt(e.getBlock().getLocation().add(0, loc[1], loc[0]));
                    breakBlock.breakNaturally(p.getInventory().getItemInMainHand());
                }
            }
        }
    }

    public OfflinePlayer getPlayer(String name){
        OfflinePlayer player = Arrays.stream(Bukkit.getOfflinePlayers())
                .filter(offlinePlayer -> name.equals(offlinePlayer.getName()))
                .findFirst()
                .orElse(null);
        return player;
    }

    public void jumpReload(){
        for (Player player : playerJumpCounts.keySet()){
            playerJumpBossBars.get(player).removeAll();
        }
        playerJumpCounts.clear();
        playerJumpBossBars.clear();
    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException | NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player) {
            Player p = (Player) sender;

            if (args[0].equals("test")){
            }

            if (p.isOp()) {
                if (args[0].equals("reload")) {
                    setAllConfig();
                    jumpReload();

                } else if (args[0].equals("jump")) {
                    String type = args[1];
                    if (Arrays.asList("add", "remove").contains(args[2])) {
                        List<String> jumpPlayers = "normal".equals(type) ? jumpNormalPlayers : jumpGreatPlayers;
                        if (args[2].equals("add")) {
                            OfflinePlayer player = getPlayer(args[3]);
                            if (Objects.nonNull(player)) {
                                if (jumpPlayers.contains(player.getUniqueId().toString())) {
                                    p.sendMessage("player exists already");
                                } else {
                                    jumpPlayers.add(player.getUniqueId().toString());
                                    config.set("jump.jump_" + type + "_players", jumpPlayers);
                                    try {
                                        config.save(configFile);
                                        setAllConfig();
                                        jumpReload();
                                        p.sendMessage("player has been added");
                                    } catch (IOException e) {
                                        p.sendMessage("something wrong");
                                        throw new RuntimeException(e);
                                    }
                                }
                            } else {
                                p.sendMessage("nothing user");
                            }
                        } else if (args[2].equals("remove")) {
                            OfflinePlayer player = getPlayer(args[3]);
                            if (Objects.nonNull(player) && jumpPlayers.contains(player.getUniqueId().toString())) {
                                jumpPlayers.remove(player.getUniqueId().toString());
                                config.set("jump.jump_" + type + "_players", jumpPlayers);
                                try {
                                    config.save(configFile);
                                    setAllConfig();
                                    jumpReload();
                                    p.sendMessage("remove complete");
                                } catch (IOException e) {
                                    p.sendMessage("something wrong");
                                    throw new RuntimeException(e);
                                }
                            } else {
                                p.sendMessage("nothing user");
                            }
                        }
                    } else {
                        if (!isInteger(args[2])) {
                            p.sendMessage("invalid arg");
                            return true;
                        }
                        config.set("jump.jump_count_" + type, Integer.parseInt(args[2]));
                        try {
                            config.save(configFile);
                            setAllConfig();
                            jumpReload();
                            p.sendMessage("count has been set");
                        } catch (IOException e) {
                            p.sendMessage("something wrong");
                            throw new RuntimeException(e);
                        }
                    }
                } else if (args[0].equals("break")) {
                    if (!Arrays.asList("add", "remove").contains(args[1])) return true;
                    OfflinePlayer player = getPlayer(args[2]);
                    if (args[1].equals("add")) {
                        if (Objects.nonNull(player)) {
                            if (breakPlayers.contains(player.getUniqueId().toString())) {
                                p.sendMessage("player exists already");
                            } else {
                                breakPlayers.add(player.getUniqueId().toString());
                                config.set("break.break_players", breakPlayers);
                                try {
                                    config.save(configFile);
                                    setAllConfig();
                                    p.sendMessage("player has been added");
                                } catch (IOException e) {
                                    p.sendMessage("something wrong");
                                    throw new RuntimeException(e);
                                }
                            }
                        }
                    } else {
                        if (Objects.nonNull(player) && breakPlayers.contains(player.getUniqueId().toString())) {
                            breakPlayers.remove(player.getUniqueId().toString());
                            config.set("break.break_players", breakPlayers);
                            try {
                                config.save(configFile);
                                setAllConfig();
                                jumpReload();
                                p.sendMessage("remove complete");
                            } catch (IOException e) {
                                p.sendMessage("something wrong");
                                throw new RuntimeException(e);
                            }
                        } else {
                            p.sendMessage("nothing user");
                        }
                    }
                } else if (args[0].equals("breakTree")) {
                    if (Arrays.asList("add", "remove").contains(args[1])) {
                        OfflinePlayer player = getPlayer(args[2]);
                        if (args[1].equals("add")) {
                            if (Objects.nonNull(player)) {
                                if (breakTreePlayers.contains(player.getUniqueId().toString())) {
                                    p.sendMessage("player exists already");
                                } else {
                                    breakTreePlayers.add(player.getUniqueId().toString());
                                    config.set("break_tree.break_tree_players", breakTreePlayers);
                                    try {
                                        config.save(configFile);
                                        setAllConfig();
                                        p.sendMessage("player has been added");
                                    } catch (IOException e) {
                                        p.sendMessage("something wrong");
                                        throw new RuntimeException(e);
                                    }
                                }
                            }
                        } else {
                            if (Objects.nonNull(player) && breakTreePlayers.contains(player.getUniqueId().toString())) {
                                breakTreePlayers.remove(player.getUniqueId().toString());
                                config.set("break_tree.break_tree_players", breakTreePlayers);
                                try {
                                    config.save(configFile);
                                    setAllConfig();
                                    jumpReload();
                                    p.sendMessage("remove complete");
                                } catch (IOException e) {
                                    p.sendMessage("something wrong");
                                    throw new RuntimeException(e);
                                }
                            } else {
                                p.sendMessage("nothing user");
                            }
                        }
                    } else if (isInteger(args[1])) {
                        config.set("break_tree.break_tree_max", Integer.parseInt(args[1]));
                        try {
                            config.save(configFile);
                            setAllConfig();
                            jumpReload();
                            p.sendMessage("set complete");
                        } catch (IOException e) {
                            p.sendMessage("something wrong");
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String alias, @NotNull String[] args) {
        if (!command.getName().equalsIgnoreCase("rewards")) return super.onTabComplete(sender, command, alias, args);

        if (sender.isOp()) {

            ArrayList<String> playerNames = new ArrayList<>();
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                playerNames.add(player.getDisplayName());
            }
            playerNames.add("プレイヤー名");

            if (args.length == 1) {
                if (args[0].length() == 0) {
                    return Arrays.asList("reload", "jump", "break", "breakTree");
                } else {
                    if ("reload".startsWith(args[0])) {
                        return Collections.singletonList("reload");
                    } else if ("jump".startsWith(args[0])) {
                        return Collections.singletonList("jump");
                    } else if ("break".startsWith(args[0])) {
                        return Collections.singletonList("break");
                    } else if ("breakTree".startsWith(args[0])) {
                        return Collections.singletonList("breakTree");
                    }
                }
            }

            if (args.length == 2) {
                if (args[0].equals("jump")) {
                    if (args[1].length() == 0) {
                        return Arrays.asList("great", "normal");
                    } else {
                        if ("great".startsWith(args[1])) {
                            return Collections.singletonList("great");
                        } else if ("normal".startsWith(args[1])) {
                            return Collections.singletonList("normal");
                        }
                    }
                } else if (args[0].equals("break")) {
                    return playerNames;
                } else if (args[0].equals("breakTree")) {
                    return Arrays.asList("add", "remove", "set");
                }
            }

            if (args.length == 3) {
                if (args[0].equals("jump")) {
                    if (args[2].length() == 0) {
                        return Arrays.asList("{count}", "add", "remove");
                    } else {
                        return playerNames;
                    }
                } else if (args[0].equals("breakTree")) {
                    if (args[1].equals("add") || args[1].equals("remove")) {
                        return playerNames;
                    } else if (args[1].equals("set")) {
                        return Collections.singletonList("{max block}");
                    }
                }
            }

            if (args.length == 4) {
                if (args[0].equals("jump")) {
                    return playerNames;
                }
            }
        }

        return null;
    }

}
