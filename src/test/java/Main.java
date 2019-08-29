import com.melvin.utils.DiscordBot;
import com.melvin.utils.SQLite;

public class Main {
    public static void main(String[] args){

        SQLite.Instance = new SQLite("C:\\Users\\darra\\OneDrive\\Documents\\Java\\Spigot\\Plugins\\ArcadiaChat","ArcadiaChat.sqlite");

        try {
            DiscordBot.Instance = new DiscordBot("NjExMzA4NDM1OTE2NTg3MTk0.XVR7XQ.NglbbKnDsvX2q17NLj-BPcm708U", "minecraft-chat");
        }catch (Exception ex){
            //TODO Log
        }

        DiscordBot.Instance.SendMessage("Professor_Melvin", "**TEST**");
    }
}
