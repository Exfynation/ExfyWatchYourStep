package de.exfy.watchyourstep.tdm;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class TDMChatListener implements Listener {


    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        TDMPlayer tdmPlayer = TDMPlayer.wrap(e.getPlayer());
        e.setFormat(ChatColor.translateAlternateColorCodes('&', tdmPlayer.getTeam().getChatColor() + "") + "%s §a➟ §7%s");

        if (e.getPlayer().hasPermission("exfy.chatcolor")) {
            e.setMessage(ChatColor.translateAlternateColorCodes('&', e.getMessage()));
        }
    }

}
