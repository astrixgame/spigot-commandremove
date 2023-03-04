package cz.astrixgame.commandremover;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerCommandSendEvent;
import org.bukkit.help.HelpTopic;
import org.bukkit.plugin.java.JavaPlugin;

public final class CommandRemover extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler(priority= EventPriority.MONITOR)
    public void onCommandSend(PlayerCommandSendEvent e) {
        if(e.getPlayer().isOp()) {
            return;
        }
        for(String cmd : this.getConfig().getStringList("removed-commands")) {
            e.getCommands().remove(cmd);
        }
    }

    @EventHandler(priority=EventPriority.LOWEST,ignoreCancelled=true)
    public void onCommandProcess(PlayerCommandPreprocessEvent e) {
        Player p = e.getPlayer();
        String command = e.getMessage().split(" ")[0];
        HelpTopic htopic = Bukkit.getServer().getHelpMap().getHelpTopic(command);
        if(htopic != null) {
            if(p.isOp()) {
                return;
            }
            String msg = getFirstWord(e.getMessage().toLowerCase());
            PluginCommand cmd = Bukkit.getPluginCommand(msg);
            if(cmd != null) {
                if(cmd.getAliases().isEmpty()) {
                    if(isRemoved(p, msg)) {
                        e.setCancelled(true);
                    }
                } else {
                    for(String aliases : cmd.getAliases()) {
                        if(msg.matches(aliases)) {
                            if(isRemoved(p, msg)) {
                                e.setCancelled(true);
                                break;
                            }
                        }
                    }
                }
                return;
            }
            if(isRemoved(p, msg)) {
                p.sendMessage(this.getConfig().getString("nocommand"));
                e.setCancelled(true);
            }
        } else {
            p.sendMessage(this.getConfig().getString("nocommand"));
            e.setCancelled(true);
        }
    }

    private boolean isRemoved(CommandSender player, String message) {
        String firstWord = getFirstWord(message).replaceFirst("/", "");
        if(this.getConfig().getStringList("removed-commands").contains(firstWord)) {
            return true;
        }
        return false;
    }

    private String getFirstWord(String text) {
        if(text.indexOf(' ') > -1)
            return text.substring(0, text.indexOf(' '));
        return text;
    }
}
