package com.melvin.utils;

import com.melvin.arcadiachat.ChatPlugin;
import com.melvin.utils.SQLiteModels.ArcadiaPlayer;
import com.melvin.utils.SQLiteModels.PlayerNickname;
import org.bukkit.Bukkit;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class SQLite {

    public static SQLite Instance;

    private String _ConnectionString;

    //private final Lock SQLiteLock = new ReentrantLock();

    public SQLite(String DirPath, String FileName){
        //check directory exists
        File directory=new File(DirPath);
        if(!directory.exists()){
            if(!directory.mkdirs()){
                Bukkit.getPluginManager().disablePlugin(ChatPlugin.Plugin);
                //TODO Log error
            }
        }


        _ConnectionString = "jdbc:sqlite:" + DirPath + "//" + FileName;

        //TODO Log
        ////SQLiteLock.lock();
        try(Connection db = Connect()){
            String sqlCreateCommand = "create table if not exists ArcadiaPlayers\n" +
                    "    (\n" +
                    "        MinecraftID UUID not null\n" +
                    "            constraint ArcadiaPlayers_pk\n" +
                    "                primary key,\n" +
                    "        DiscordID   String\n" +
                    "    );\n" +
                    "\n" +
                    "create unique index if not exists ArcadiaPlayers_DiscordID_uindex\n" +
                    "    on ArcadiaPlayers (DiscordID);\n" +
                    "\n" +
                    "create unique index if not exists ArcadiaPlayers_MinecraftID_uindex\n" +
                    "    on ArcadiaPlayers (MinecraftID);";

            Statement cmd = db.createStatement();
            cmd.execute(sqlCreateCommand);

            sqlCreateCommand = "\n" +
                    "create table if not exists  PlayerNicknames\n" +
                    "(\n" +
                    "    MinecraftID UUID   not null,\n" +
                    //"        references ArcadiaPlayers,\n" +
                    "    Nickname    string not null\n" +
                    ");\n" +
                    "\n" +
                    "create unique index if not exists  PlayerNicknames_Nickname_uindex\n" +
                    "    on PlayerNicknames (Nickname);";

            cmd.execute(sqlCreateCommand);
        }catch(Exception ex){
            //TODO Log
        }
        ////SQLiteLock.unlock();
    }

    private Connection Connect() {
        Connection conn = null;
        try {
            conn = DriverManager.getConnection(_ConnectionString);
        } catch (Exception e) {
            //TODO Log
        }
        return conn;
    }

    public List<PlayerNickname> GetNicknames(UUID MinecraftID){
        String sqlCommand = "SELECT * FROM PlayerNicknames WHERE MinecraftID = '" + MinecraftID + "';";

        List<PlayerNickname> reply = new ArrayList<PlayerNickname>();

        ////SQLiteLock.lock();
        try(Connection conn = Connect();
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sqlCommand)){
            while(rs.next()){
                reply.add(new PlayerNickname(rs.getString(1), rs.getString(2).toLowerCase()));
            }
        }catch(Exception ex){
            //TODO Log
            ex.printStackTrace();
        }
        //SQLiteLock.unlock();
        return  reply;
    }

    public PlayerNickname GetNickname(UUID MinecraftID, String Nickname){
        String sqlCommand = "SELECT * FROM PlayerNicknames WHERE MinecraftID = '" + MinecraftID + "' AND Nickname = '" + Nickname.toLowerCase() + "';";

        //SQLiteLock.lock();
        try(Connection conn = Connect();
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(sqlCommand)){
            while(rs.next()){
                return new PlayerNickname(rs.getString(1), rs.getString(2).toLowerCase());
            }
        }catch(Exception ex){
            //TODO Log
        }
        //SQLiteLock.unlock();
        return null;
    }

    public boolean PutNickname(PlayerNickname nickObj){
        return PutNickname(nickObj.MinecraftID, nickObj.Nickname.toLowerCase());
    }

    public boolean PutNickname(UUID MinecraftID, String Nickname){
        String sqlCommand = "INSERT INTO PlayerNicknames (minecraftid, nickname)" +
                " VALUES (" +
                "'" + MinecraftID + "'," +
                "'"+ Nickname.toLowerCase() + "');";

        //SQLiteLock.lock();
        try(Connection conn = Connect(); Statement stmt  = conn.createStatement()){
            stmt.executeUpdate(sqlCommand);
        }catch(Exception ex){
            //TODO Log
            //SQLiteLock.unlock();
            return false;
        }
        //SQLiteLock.unlock();
        return  true;
    }

    public boolean RemoveNickname(UUID MinecraftID, String Nickname){
        String sqlCommand = "DELETE FROM PlayerNicknames WHERE " + "MinecraftID = '" + MinecraftID + "' AND" + " Nickname = '"+ Nickname.toLowerCase() + "';";

        //SQLiteLock.lock();
        try(Connection conn = Connect();
            Statement stmt  = conn.createStatement();){
            stmt.executeUpdate(sqlCommand);
        }catch(Exception ex){
            //TODO Log
            //SQLiteLock.unlock();
            return false;
        }
        //SQLiteLock.unlock();
        return  true;
    }

    public ArcadiaPlayer GetPlayer(UUID MinecraftID){
        String sqlCommand = "SELECT * FROM ArcadiaPlayers WHERE MinecraftID = '" + MinecraftID + "';";
        return GetPlayerSQL(sqlCommand);
    }

    public ArcadiaPlayer GetPlayer(String DiscordID){
        String sqlCommand = "SELECT * FROM ArcadiaPlayers WHERE DiscordID = '" + DiscordID + "';";
        return GetPlayerSQL(sqlCommand);
    }

    private ArcadiaPlayer GetPlayerSQL(String SQL){
        ArcadiaPlayer reply = null;

        //SQLiteLock.lock();
        try(Connection conn = Connect();
            Statement stmt  = conn.createStatement();
            ResultSet rs    = stmt.executeQuery(SQL)){
            while(rs.next()){
                reply = new ArcadiaPlayer(rs.getString(1), rs.getString(2));
            }
        }catch(Exception ex){
            //TODO Log
        }
        //SQLiteLock.unlock();
        return  reply;
    }

    public boolean PutPlayer(ArcadiaPlayer player){
        return PutPlayer(player.MinecraftID,player.DiscordID);
    }

    public boolean PutPlayer(UUID MinecraftID, String DiscordID){
        String sqlCommand = "INSERT INTO ArcadiaPlayers (minecraftid, discordid)" +
                "VALUES (" +
                "'" + MinecraftID + "'," +
                "'"+ DiscordID + "');";

        //SQLiteLock.lock();
        try(Connection conn = Connect(); Statement stmt  = conn.createStatement();){
            stmt.executeUpdate(sqlCommand);
        }catch(Exception ex){
            //TODO Log
            //SQLiteLock.unlock();
            return false;
        }
        //SQLiteLock.unlock();
        return  true;
    }

    public boolean UpdatePlayer(ArcadiaPlayer player){
        return UpdatePlayer(player.MinecraftID,player.DiscordID);
    }

    public boolean UpdatePlayer(UUID MinecraftID, String DiscordID){
        String sqlCommand = "UPDATE ArcadiaPlayers SET (DiscordID = '"+ DiscordID + "') WHERE MinecraftID = '" + MinecraftID + "';";

        //SQLiteLock.lock();
        try(Connection conn = Connect();
            Statement stmt  = conn.createStatement();){
            stmt.executeUpdate(sqlCommand);
        }catch(Exception ex){
            //TODO Log
            //SQLiteLock.unlock();
            return false;
        }
        //SQLiteLock.unlock();
        return  true;
    }

    public boolean RemovePlayer(UUID MinecraftID){
        String sqlCommand = "DELETE FROM ArcadiaPlayers WHERE " + "MinecraftID = '" + MinecraftID + "';";

        //SQLiteLock.lock();
        try(Connection conn = Connect(); Statement stmt  = conn.createStatement();){
            stmt.executeUpdate(sqlCommand);
        }catch(Exception ex){
            //TODO Log
            //SQLiteLock.unlock();
            return false;
        }
        //SQLiteLock.unlock();
        return  true;
    }
}