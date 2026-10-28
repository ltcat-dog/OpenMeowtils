package wtf.tatp.meowtils.manager.lists;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;
import wtf.tatp.meowtils.Meowtils;

public class FriendlistManager {
    private static final File FILE = Meowtils.MEOWTILS_FRIENDLIST;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TYPE = (new TypeToken<Set<String>>() {}).getType();
    private static Set<String> friendlist = new HashSet<>();

    public static void add(String uuidOrName) {
        friendlist.add(uuidOrName);
        saveFriendlist();
    }

    public static void remove(String uuidOrName) {
        friendlist.remove(uuidOrName);
        saveFriendlist();
    }

    public static boolean isFriendlisted(String uuidOrName) {
        return friendlist.contains(uuidOrName);
    }

    private static void loadFriendlist() {
        if (FILE.exists()) {
            try (Reader reader = new FileReader(FILE)) {
                friendlist = (Set<String>)gson.fromJson(reader, TYPE);
                if (friendlist == null) {
                    friendlist = new HashSet<>();
                }
            } catch (Exception var13) {
                var13.printStackTrace();
            }
        }
    }

    private static void saveFriendlist() {
        try (Writer writer = new FileWriter(FILE)) {
            gson.toJson(friendlist, writer);
        } catch (IOException var13) {
            var13.printStackTrace();
        }
    }

    static {
        loadFriendlist();
    }
}
