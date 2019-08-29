package com.melvin.arcadiachat.listeners;

import com.melvin.arcadiachat.ChatPlugin;
import com.melvin.utils.DiscordBot;
import com.melvin.utils.SQLite;
import com.melvin.utils.SQLiteModels.PlayerNickname;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.event.server.ServiceRegisterEvent;
import org.bukkit.event.server.ServiceUnregisterEvent;

import java.util.Collection;
import java.util.List;

public class Chat implements Listener {

    private static final String NAME_PLACEHOLDER = "{name}";
    private static final String DISPLAYNAME_PLACEHOLDER = "{displayname}";
    private static final String MESSAGE_PLACEHOLDER = "{message}";
    private static final String PREFIX_PLACEHOLDER = "{prefix}";
    private static final String SUFFIX_PLACEHOLDER = "{suffix}";
    private static final String DEFAULT_FORMAT = "<" + PREFIX_PLACEHOLDER + NAME_PLACEHOLDER + SUFFIX_PLACEHOLDER + "> " + MESSAGE_PLACEHOLDER;

    private static String format = colorize(ChatPlugin.Plugin.getConfig().getString("format", DEFAULT_FORMAT).replace(DISPLAYNAME_PLACEHOLDER, "%1$s").replace(MESSAGE_PLACEHOLDER, "%2$s"));

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChatLow(AsyncPlayerChatEvent e) {
        if(format != null && !format.equals("")){
            e.setFormat(format);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChatHigh(AsyncPlayerChatEvent event) {

        String message = event.getMessage();
        Player sender = event.getPlayer();

        if(ChatPlugin.usersInStaffChat.stream().anyMatch(u -> u == sender.getUniqueId())){
            Bukkit.getOnlinePlayers().stream().filter(p -> ((Player) p).hasPermission(ChatPlugin.ArcadiaPermissions.StaffChat)).forEach(p -> {

                String prefix = null;
                String suffix = null;
                if (ChatPlugin.VaultChat != null && format.contains(PREFIX_PLACEHOLDER)) {
                    prefix = colorize(ChatPlugin.VaultChat.getPlayerPrefix(sender));
                }
                if (ChatPlugin.VaultChat != null && format.contains(SUFFIX_PLACEHOLDER)) {
                    suffix = colorize(ChatPlugin.VaultChat.getPlayerSuffix(sender));
                }
                if(prefix != null){
                    if(suffix == null){
                        suffix = "" + ChatColor.RESET;
                    }
                    ((Player) p).sendMessage(ChatColor.RED + "STAFF: " + ChatColor.WHITE + "<" + prefix + sender.getDisplayName()+ suffix + "> " + message);
                }else{
                    ((Player) p).sendMessage(ChatColor.RED + "STAFF: " + ChatColor.WHITE + "<" + sender.getDisplayName()+"> " + message);
                }
            });

            event.setCancelled(true);
            return;
        }

        //Send to Discord
        if(!message.toLowerCase().startsWith("discord:")) {
            if(sender.hasPermission(ChatPlugin.ArcadiaPermissions.DiscordChat)){
                DiscordBot.Instance.SendMessage(sender.getDisplayName(), message);
            }
        }

        //Insert colours
        String format = event.getFormat();
        if (ChatPlugin.VaultChat != null && format.contains(PREFIX_PLACEHOLDER)) {
            format = format.replace(PREFIX_PLACEHOLDER, colorize(ChatPlugin.VaultChat.getPlayerPrefix(sender)));
        }
        if (ChatPlugin.VaultChat != null && format.contains(SUFFIX_PLACEHOLDER)) {
            format = format.replace(SUFFIX_PLACEHOLDER, colorize(ChatPlugin.VaultChat.getPlayerSuffix(sender)));
        }
        format = format.replace(NAME_PLACEHOLDER, sender.getName());
        event.setFormat(format);

        //Check for ping
        Bukkit.getOnlinePlayers().stream().filter(p -> ((Player) p).hasPermission(ChatPlugin.ArcadiaPermissions.Nickname)).forEach(p -> {
            if (ChatPlugin.deafenedPlayers.stream().noneMatch(dp -> dp == p.getUniqueId())){
                List<PlayerNickname> names = SQLite.Instance.GetNicknames(p.getUniqueId());
                for (PlayerNickname name : names) {
                    if (message.toLowerCase().contains(name.Nickname.toLowerCase())) {
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 10, (float) 0.7);
                        break;
                    }
                }
            }
        });
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e){
        Player p = e.getEntity();
        if(p.hasPermission(ChatPlugin.ArcadiaPermissions.DiscordChat)){
            DiscordBot.Instance.SendRawMessage(e.getDeathMessage());
        }
    }

    @EventHandler
    public void onServiceChange(ServiceRegisterEvent e) {
        if (e.getProvider().getService() == net.milkbowl.vault.chat.Chat.class) {
            ChatPlugin.refreshVault();
        }
    }

    @EventHandler
    public void onServiceChange(ServiceUnregisterEvent e) {
        if (e.getProvider().getService() == net.milkbowl.vault.chat.Chat.class) {
            ChatPlugin.refreshVault();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent e){
        Player p = e.getPlayer();
        if(p.hasPermission(ChatPlugin.ArcadiaPermissions.DiscordChat)){
            DiscordBot.Instance.SendRawMessage(p.getDisplayName() + " joined the game");
            //DiscordBot.Instance.SendRawMessage(e.getJoinMessage());
        }
    }

    @EventHandler
    public void onPlayerLeave(PlayerQuitEvent e){
        Player p = e.getPlayer();
        if(p.hasPermission(ChatPlugin.ArcadiaPermissions.DiscordChat)){
            DiscordBot.Instance.SendRawMessage(p.getDisplayName() + " left the game");
            //DiscordBot.Instance.SendRawMessage(e.getQuitMessage());
        }
    }

    @EventHandler
    public void playerExcutesCommand(PlayerCommandPreprocessEvent e){
        String fullcmd = e.getMessage();

        String cmd = fullcmd.substring(0, fullcmd.indexOf(" "));
        String message = fullcmd.substring(fullcmd.indexOf(" "));

        if(cmd.toLowerCase().equals("/me")) {
            Player p = e.getPlayer();
            if (p.hasPermission(ChatPlugin.ArcadiaPermissions.DiscordChat)) {
                DiscordBot.Instance.SendRawMessage("* *" + p.getDisplayName() + "* " + message);
            }
        }
    }


    private static String colorize(String s) {
        return s == null ? null : ChatColor.translateAlternateColorCodes('&', s);
    }
}
