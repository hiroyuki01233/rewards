//package rewards.rewards;
//
//import com.sun.jndi.rmi.registry.ReferenceWrapper_Stub;
//import org.bukkit.command.Command;
//import org.bukkit.command.CommandExecutor;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//import org.bukkit.event.Listener;
//import org.jetbrains.annotations.NotNull;
//
//public class Commands implements CommandExecutor {
//    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
//        if (sender instanceof Player) {
//            Player p = (Player) sender;
//
//            if (args[0].equals("reload")){
//                Rewards.setAllConfig();
//                for (Player player : Rewards.playerJumpCounts.keySet()){
//                    Rewards.playerJumpBossBars.get(player).removeAll();
//                }
//                Rewards.playerJumpCounts.clear();
//                Rewards.playerJumpBossBars.clear();
//            }
//        }
//        return true;
//    }
//}
