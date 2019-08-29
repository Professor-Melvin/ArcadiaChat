package com.melvin.utils.SQLiteModels;

import java.util.UUID;

public class PlayerNickname {
    public UUID MinecraftID;
    public String Nickname;

    public PlayerNickname(UUID UUID, String Nickname){
        MinecraftID = UUID;
        this.Nickname = Nickname;
    }
    public PlayerNickname(String Uuid, String Nickname){
        MinecraftID = UUID.fromString(Uuid);
        this.Nickname = Nickname;
    }
}
