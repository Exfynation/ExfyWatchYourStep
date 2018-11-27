package de.exfy.watchyourstep.ffa;

import com.github.cheesesoftware.PowerfulPermsAPI.PermissionPlayer;
import de.exfy.core.modules.Permissions;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class FFAChatListener implements Listener {


    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        PermissionPlayer permissionPlayer = Permissions.getPowerfulPerms().getPermissionManager().getPermissionPlayer(e.getPlayer().getUniqueId());

        e.setFormat(ChatColor.translateAlternateColorCodes('&',permissionPlayer.getPrefix()) + "%s §a➟ §7%s");

        if (e.getPlayer().hasPermission("exfy.chatcolor")) {
            e.setMessage(ChatColor.translateAlternateColorCodes('&', e.getMessage()));
        }
    }

}
