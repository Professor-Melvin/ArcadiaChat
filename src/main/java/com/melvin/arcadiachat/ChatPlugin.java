package com.melvin.arcadiachat;

import com.melvin.arcadiachat.commands.ChatCommand;
import com.melvin.arcadiachat.listeners.Chat;
import com.melvin.utils.DiscordBot;
import com.melvin.utils.SQLite;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ChatPlugin extends JavaPlugin {
    public static Plugin Plugin;

    public static List<UUID> deafenedPlayers = new ArrayList<UUID>();
    public static List<UUID> usersInStaffChat = new ArrayList<UUID>();

    public static net.milkbowl.vault.chat.Chat VaultChat = null;

    @Override
    public void onEnable() {
        Plugin = this;

        saveDefaultConfig();

        SQLite.Instance = new SQLite(getDataFolder().getAbsolutePath(), getConfig().getString("SQLite.FileName", "ArcadiaChat.sqlite"));
        try {
            //NjExMzA4NDM1OTE2NTg3MTk0.XVR7XQ.NglbbKnDsvX2q17NLj-BPcm708U = Bot in Melvin's Dev Discord Server
            DiscordBot.Instance = new DiscordBot(getConfig().getString("Discord.BotKey", null), getConfig().getString("Discord.Channel", "minecraft-chat"));
        }catch (Exception ex){
            //TODO Log
            Bukkit.getPluginManager().disablePlugin(this);
        }

        //Vault Chat Colours
        refreshVault();

        //listeners
        Bukkit.getPluginManager().registerEvents(new Chat(), this);

        //commands
        this.getCommand("ArcadiaChat").setExecutor(new ChatCommand());
        this.getCommand("ArcadiaChat").setTabCompleter(new ChatCommand());

        DiscordBot.Instance.SendRawMessage("Server Chat Connected!");
    }

    public static void refreshVault() {
        RegisteredServiceProvider<net.milkbowl.vault.chat.Chat> rsp = ChatPlugin.Plugin.getServer().getServicesManager().getRegistration(net.milkbowl.vault.chat.Chat.class);
        net.milkbowl.vault.chat.Chat vaultChat = null;
        try {
            vaultChat = rsp.getProvider();
            if(vaultChat != null && vaultChat != VaultChat){
                ChatPlugin.Plugin.getLogger().info("New Vault Chat implementation registered: " + (vaultChat == null ? "null" : vaultChat.getName()));
                VaultChat = vaultChat;
            }
        }catch(Exception ex){
            //TODO Log
        }
    }

    @Override
    public void onDisable() {
        DiscordBot.Instance.SendRawMessage("Server Chat Disconnected!");
        DiscordBot.Instance.CloseConnection();
        DiscordBot.Instance = null;
        SQLite.Instance = null;
    }

    public static class ArcadiaPermissions{
        public static final String All = "ArcadiaChat.*";
        public static final String StaffChat = "ArcadiaChat.StaffChat";
        public static final String DiscordChat = "ArcadiaChat.DiscordChat";
        public static final String DiscordRegister = "ArcadiaChat.DiscordRegister";
        public static final String Nickname = "ArcadiaChat.Nickname";
    }
}
