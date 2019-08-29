package com.melvin.utils.SQLiteModels;

import java.util.UUID;

public class ArcadiaPlayer {
    public UUID MinecraftID;
    public String DiscordID;

    public ArcadiaPlayer(UUID UUID, String DiscordID){
        MinecraftID = UUID;
        this.DiscordID = DiscordID;
    }
    public ArcadiaPlayer(String Uuid, String DiscordID){
        MinecraftID = UUID.fromString(Uuid);
        this.DiscordID = DiscordID;
    }
}
