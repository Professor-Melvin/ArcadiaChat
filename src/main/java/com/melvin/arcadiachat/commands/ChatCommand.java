package com.melvin.arcadiachat.commands;

import com.melvin.arcadiachat.ChatPlugin;
import com.melvin.utils.DiscordBot;
import com.melvin.utils.SQLite;
import com.melvin.utils.SQLiteModels.ArcadiaPlayer;
import com.melvin.utils.SQLiteModels.PlayerNickname;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChatCommand  implements CommandExecutor, TabExecutor {
    private Player player;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if(sender instanceof Player){
            player = (Player)sender;
            if(label.toLowerCase().equals("ChatPlugin") || label.toLowerCase().equals("ac") || label.toLowerCase().equals("chat")){
                if(args.length > 0){
                    if(args[0].toLowerCase().equals("nickname")){
                        if(player.hasPermission(ChatPlugin.ArcadiaPermissions.Nickname)) {
                            return Nickname(args);
                        }
                    }else if(args[0].toLowerCase().equals("register") || args[0].toLowerCase().equals("unregister")){
                        if(player.hasPermission(ChatPlugin.ArcadiaPermissions.DiscordRegister)) {
                            return RegisterDiscord(args);
                        }
                    }else if(args[0].toLowerCase().equals("staff")){
                        if(player.hasPermission(ChatPlugin.ArcadiaPermissions.StaffChat)) {
                            return Staff();
                        }
                    }
                }
            }
        }else{
            sender.sendMessage("You must be a player to use this command");
            return true;
        }
        return false;
    }

    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args){
        List<String> reply = new ArrayList<String>();
        if(label.toLowerCase().equals("ChatPlugin") || label.toLowerCase().equals("ac") || label.toLowerCase().equals("chat")){
            if(args.length == 1){
                if(sender.hasPermission(ChatPlugin.ArcadiaPermissions.Nickname)) {
                    reply.add("nickname");
                }
                if(sender.hasPermission(ChatPlugin.ArcadiaPermissions.StaffChat)) {
                    reply.add("staff");
                }
                if(sender.hasPermission(ChatPlugin.ArcadiaPermissions.DiscordRegister)) {
                    reply.add("register");
                    reply.add("unregister");
                }
            }else if(args.length == 2){
                switch(args[0].toLowerCase()){
                    case "nickname":
                        if(sender.hasPermission(ChatPlugin.ArcadiaPermissions.Nickname)) {
                            reply.add("add");
                            reply.add("remove");
                            reply.add("list");
                            reply.add("deafen");
                        }
                        break;
                    case "register":
                        if(sender.hasPermission(ChatPlugin.ArcadiaPermissions.DiscordRegister)) {
                            reply.add("<discord name>");
                        }
                        break;
                }
            }else if(args.length == 3){
                if ("nickname".equals(args[0].toLowerCase())) {
                    switch(args[1].toLowerCase()){
                        case "add":
                        case "remove":
                            if(sender.hasPermission(ChatPlugin.ArcadiaPermissions.Nickname)) {
                                reply.add("<nickname>");
                            }
                            break;
                    }
                }
            }
        }
        Collections.sort(reply);
        return reply;
    }

    private boolean Staff(){
        if(ChatPlugin.usersInStaffChat.stream().anyMatch(u -> u == player.getUniqueId())){
            ChatPlugin.usersInStaffChat.remove(player.getUniqueId());
            player.sendMessage(ChatColor.DARK_GREEN + "No longer in staff chat" + ChatColor.RESET);
            return true;
        }else{
            ChatPlugin.usersInStaffChat.add(player.getUniqueId());
            player.sendMessage(ChatColor.DARK_GREEN + "Now in staff chat" + ChatColor.RESET);
            return true;
        }
        //return false;
    }

    private boolean RegisterDiscord(String[] args){
        boolean reply = false;
        if((args[0].equals("register"))){
            if(args.length >= 2) {
                String discordID = DiscordBot.Instance.TryGetDiscordID(args[1]);
                if (discordID == null) {
                    player.sendMessage(ChatColor.RED + "This user is not registered on the discord server, or does not have permission for the Minecraft chat channel" + ChatColor.RESET);
                    reply = true;
                }

                ArcadiaPlayer acPLayer = SQLite.Instance.GetPlayer(player.getUniqueId());
                if (acPLayer == null) {
                    reply = SQLite.Instance.PutPlayer(player.getUniqueId(), discordID);
                    player.sendMessage(ChatColor.DARK_GREEN + "Discord account registered" + ChatColor.RESET);
                } else {
                    player.sendMessage(ChatColor.RED + "Discord account already registered" + ChatColor.RESET);
                    reply = true;
                }
            }
        }else if((args[0].equals("unregister"))){
            if(SQLite.Instance.RemovePlayer(player.getUniqueId())){
                player.sendMessage(ChatColor.DARK_GREEN + "Discord account unregistered" + ChatColor.RESET);
            }else{
                player.sendMessage(ChatColor.RED + "Failed to unregister discord account" + ChatColor.RESET);
            }
            reply = true;
        }
        return reply;
    }

    private boolean Nickname(String[] args){
        if(args.length >= 2){
            if(args[1].toLowerCase().equals("add")){
                if(args.length >= 3) {
                    return addNickname(args[2]);
                }
            }else if(args[1].toLowerCase().equals("remove")){
                if(args.length >= 3) {
                    return removeNickname(args[2]);
                }
            }else if(args[1].toLowerCase().equals("deafen")){
                return deafenNicknames();
            }else if(args[1].toLowerCase().equals("list")){
                return listNicknames();
            }
        }
        return false;
    }

    private boolean addNickname(String nickName){
        PlayerNickname name = SQLite.Instance.GetNickname(player.getUniqueId(), nickName);
        if(name == null){
            boolean reply = false;
            reply =  SQLite.Instance.PutNickname(player.getUniqueId(), nickName);
            player.sendMessage(ChatColor.DARK_GREEN + "Nickname registered" + ChatColor.RESET);
            return reply;
        }else{
            player.sendMessage(ChatColor.DARK_GREEN + "Nickname already registered" + ChatColor.RESET);
            return true;
        }
    }

    private boolean removeNickname(String nickName){
        PlayerNickname name = SQLite.Instance.GetNickname(player.getUniqueId(), nickName);
        if(name == null){
            player.sendMessage(ChatColor.DARK_GREEN + "Nickname never registered" + ChatColor.RESET);
            return true;
        }else{
            boolean reply = false;
            reply =  SQLite.Instance.RemoveNickname(player.getUniqueId(), nickName);
            player.sendMessage(ChatColor.DARK_GREEN + "Nickname unregistered" + ChatColor.RESET);
            return reply;
        }
    }

    private boolean listNicknames(){
        List<PlayerNickname> names = SQLite.Instance.GetNicknames(player.getUniqueId());
        if(names.size() > 0) {
            StringBuilder message = new StringBuilder(ChatColor.DARK_GREEN + "Current Nicknames:\n" + ChatColor.RESET + ChatColor.ITALIC);

            for (PlayerNickname playerNickname : names) {
                String name = playerNickname.Nickname;
                message.append(name).append(", ");
            }

            player.sendMessage(message.substring(0, message.length() - 2) + ChatColor.RESET);
        }else{
            player.sendMessage(ChatColor.RED + "No nicknames are currently registered" + ChatColor.RESET);
        }
        return true;
    }

    private boolean deafenNicknames(){
        if(ChatPlugin.deafenedPlayers.stream().anyMatch(u -> u.equals(player.getUniqueId()))){
            ChatPlugin.deafenedPlayers.remove(player.getUniqueId());
            player.sendMessage(ChatColor.DARK_GREEN +"You are no longer deafened"  + ChatColor.RESET);
        }else{
            ChatPlugin.deafenedPlayers.add(player.getUniqueId());
            player.sendMessage(ChatColor.DARK_GREEN +"You are now deafened" + ChatColor.RESET);
        }
        return true;
    }

}