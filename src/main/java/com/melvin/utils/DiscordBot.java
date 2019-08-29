package com.melvin.utils;

import com.melvin.arcadiachat.ChatPlugin;
import com.melvin.utils.SQLiteModels.ArcadiaPlayer;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.*;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;
import org.apache.commons.lang.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import javax.security.auth.login.LoginException;
import java.util.List;

public class DiscordBot  {

    public static DiscordBot Instance;
    public String defaultChannelName;

    private JDA Jda;

    public DiscordBot(String token, String ChannelName) throws LoginException, InterruptedException {
        defaultChannelName = ChannelName;
        JDABuilder jBuilder = new JDABuilder(AccountType.BOT);
        jBuilder.setToken(token);
        jBuilder.addEventListener(new DiscordListener());
        Jda = jBuilder.buildBlocking();
    }

    public void SendMessage(String Auther, String msg){
        List<TextChannel> channels = Jda.getTextChannelsByName(defaultChannelName,true);
        if(channels.size() > 0){
            TextChannel channel = channels.get(0);
            SendRawMessage(channel, "<" + Auther + "> " + msg);
        }
    }

    public void SendMessage(String ChannelName, String Auther, String msg){
        List<TextChannel> channels = Jda.getTextChannelsByName(ChannelName,true);
        if(channels.size() > 0){
            TextChannel channel = channels.get(0);
            SendRawMessage(channel, "<" + Auther + "> " + msg);
        }
    }

    public void SendRawMessage(String msg){
        List<TextChannel> channels = Jda.getTextChannelsByName(defaultChannelName,true);
        if(channels.size() > 0){
            TextChannel channel = channels.get(0);
            SendRawMessage(channel, msg);
        }
    }

    public void SendRawMessage(String ChannelName, String Auther, String msg){
        List<TextChannel> channels = Jda.getTextChannelsByName(ChannelName,true);
        if(channels.size() > 0){
            TextChannel channel = channels.get(0);
            SendRawMessage(channel, "<" + Auther + "> " + msg);
        }
    }

    private void SendRawMessage(TextChannel ch, String msg){
        ch.sendMessage(msg).queue();
    }

    public String TryGetDiscordID(String Name){
        List<TextChannel> channels = Jda.getTextChannelsByName(defaultChannelName,true);
        if(channels.size() > 0){
            TextChannel channel = channels.get(0);
            List<Member> discordMs = channel.getGuild().getMembersByName(Name,true);
            if(discordMs.size() > 0){
                Member discordM = discordMs.get(0);
                return discordM.getUser().getId();
            }
        }
        return null;
    }

    public void CloseConnection(){
        Jda.shutdown();
        Jda = null;
    }

    public class DiscordListener extends ListenerAdapter{

        @Override
        public void onMessageReceived (MessageReceivedEvent messsageEvent){
            if(!messsageEvent.getAuthor().isBot() && messsageEvent.getChannelType() == ChannelType.TEXT){
                String ChannelName = messsageEvent.getTextChannel().getName().toLowerCase();
                if(ChannelName.equals(DiscordBot.Instance.defaultChannelName)){
                    messsageEvent.getMessage().delete().queue();
                    User discordUser = messsageEvent.getAuthor();
                    ArcadiaPlayer arcadiaPlayer = SQLite.Instance.GetPlayer(discordUser.getId());
                    if(arcadiaPlayer == null){
                        //TODO Delete message
                        discordUser.openPrivateChannel().queue((channel) ->
                        {
                            channel.sendMessage("Your message was not sent to the Minecraft server as you have not registered though the ArcadaChat plugin yet\nTo do so, please use the following command on the server: /chat register <discord name>").queue();
                        });
                    }else{
                        //TODO Support filtering banned words?

                        //TODO Filter unsupported discord messages

                        try {
                            OfflinePlayer p = Bukkit.getOfflinePlayer(arcadiaPlayer.MinecraftID);
                            Player player = p.getPlayer();
                            if (p != null) {

                                if(messsageEvent.getMessage().getContentStripped().startsWith("!")){
                                    if(messsageEvent.getMessage().getContentStripped().startsWith("!players")){

                                        StringBuilder playersList = new StringBuilder();

                                        for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                                            playersList.append(onlinePlayer.getName()).append(", ");
                                        }
                                        if(playersList.length() > 2) {
                                            discordUser.openPrivateChannel().queue((channel) ->
                                            {
                                                channel.sendMessage("Players currently online:\n" + playersList.substring(0, playersList.length() - 2)).queue();
                                            });
                                        }else{
                                            discordUser.openPrivateChannel().queue((channel) ->
                                            {
                                                channel.sendMessage("0 players currently online").queue();
                                            });
                                        }
                                    }
                                }else {

                                    String text = EmojiUtils.removeEmoji(messsageEvent.getMessage().getContentStripped());
                                    if(text.isEmpty() || StringUtils.isBlank(text)){
                                        return;
                                    }

                                    String auther = null;
                                    if(player != null){
                                        auther = player.getDisplayName();
                                    }else{
                                        auther = p.getName();
                                    }

                                    String prefix = null;
                                    String suffix = null;

                                    if(ChatPlugin.VaultChat != null){
                                        if (player == null) {
                                            prefix = colorize(ChatPlugin.VaultChat.getPlayerPrefix("world", p));
                                            suffix = colorize(ChatPlugin.VaultChat.getPlayerSuffix("world", p));
                                        } else {
                                            prefix = colorize(ChatPlugin.VaultChat.getPlayerPrefix(player));
                                            suffix = colorize(ChatPlugin.VaultChat.getPlayerSuffix(player));
                                        }

                                        if(prefix == null || prefix.equals("")){
                                            prefix = ChatColor.RESET + "";
                                        }
                                        if(suffix == null || suffix.equals("")){
                                            suffix = ChatColor.RESET + "";
                                        }
                                    }

                                    String discordMessage = "<" + auther + "> " + text;
                                    String minecraftMessage = "Discord: <" + prefix + auther + suffix + "> " + text;

                                    DiscordBot.Instance.SendRawMessage(messsageEvent.getTextChannel(), discordMessage);
                                    Bukkit.broadcastMessage(minecraftMessage);
                                }
                            } else {
                                //TODO Tell discord user we couldn't find their player??
                            }
                        }catch(Exception ex){
                            //TODO Handle
                        }
                    }
                }
            }
        }

        private String colorize(String s) {
            return s == null ? null : ChatColor.translateAlternateColorCodes('&', s);
        }
    }

}
