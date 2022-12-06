//package rewards.rewards;
//
//import org.bukkit.Bukkit;
//import org.bukkit.command.Command;
//import org.bukkit.command.CommandExecutor;
//import org.bukkit.command.CommandSender;
//import org.bukkit.entity.Player;
//import org.bukkit.event.Listener;
//import org.jetbrains.annotations.NotNull;
//
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.Collections;
//import java.util.List;
//
//public class TabComplete implements onTabCompleteExecutor {
//    public List<String> onTabComplete(@NotNull CommandSender sender, Command command, @NotNull String alias, @NotNull String[] args) {
//        if (!command.getName().equalsIgnoreCase("kit")) return super.onTabComplete(sender, command, alias, args);
//        ArrayList<String> playerNames = new ArrayList<>();
//        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
//            playerNames.add(player.getDisplayName());
//        }
//        playerNames.add("プレイヤー名");
//
//        if (args.length == 1) {
//            if (args[0].length() == 0) {
//                return Arrays.asList("help", "balance", "transfer", "seven", "twoup", "battle", "sign", "store", "logs", "p");
//            } else {
//                //入力されている文字列と先頭一致
//                if ("help".startsWith(args[0])) {
//                    return Collections.singletonList("help");
//                } else if ("balance".startsWith(args[0])) {
//                    return Collections.singletonList("balance");
//                } else if ("transfer".startsWith(args[0])) {
//                    return Collections.singletonList("transfer");
//                } else if ("seven".startsWith(args[0])) {
//                    return Collections.singletonList("seven");
//                } else if ("twoup".startsWith(args[0])) {
//                    return Collections.singletonList("twoup");
//                } else if ("battle".startsWith(args[0])) {
//                    return Collections.singletonList("battle");
//                } else if ("sign".startsWith(args[0])) {
//                    return Collections.singletonList("sign");
//                } else if ("store".startsWith(args[0])) {
//                    return Collections.singletonList("store");
//                } else if ("logs".startsWith(args[0])) {
//                    return Collections.singletonList("logs");
//                } else if ("p".startsWith(args[0])) {
//                    return Collections.singletonList("p");
//                }
//            }
//        }
//    }
//}
