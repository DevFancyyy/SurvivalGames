package de.fancy.survivalgames.listener;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();

        if(player.hasPermission("rank.premium")) {
            String premiumMessage = formatSpecialMessage(event.getMessage());
            event.setMessage(premiumMessage);
        }

        event.setFormat(player.getDisplayName() + "§7: " + event.getMessage());
    }

    private String formatSpecialMessage(String message) {
        String[] messageArray = message.split(" ");
        String formattedMessage = "";

        for(int i = 0; i < messageArray.length; i++) {
            if (messageArray[i].equalsIgnoreCase("gg")) {
                formattedMessage = formattedMessage + "§kgg§r §7" + messageArray[i] + " §7§kgg§r §7";
            } else {
                formattedMessage = formattedMessage + messageArray[i] + " ";
            }
        }

        return formattedMessage;
    }

}
