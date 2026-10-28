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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.stats.StatsContainer;

public class UrchinManager {
    private static final File FILE = Meowtils.MEOWTILS_URCHIN_TAGS;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Type TYPE = (new TypeToken<Map<String, UrchinManager.Entry>>() {}).getType();
    private static Map<String, UrchinManager.Entry> taggedPlayers = new HashMap<>();

    public static void put(String name, String uuid, List<StatsContainer.UrchinTag> tags) {
        if (name != null && !name.isEmpty()) {
            UrchinManager.Entry entry = new UrchinManager.Entry();
            entry.name = name;
            entry.uuid = uuid;
            entry.tags = tags;
            taggedPlayers.entrySet().removeIf(e -> uuid != null && !uuid.isEmpty() && uuid.equalsIgnoreCase(e.getValue().uuid));
            taggedPlayers.put(name.toLowerCase(), entry);
            saveTags();
        }
    }

    public static void remove(String name, String uuid) {
        if (name != null && !name.isEmpty()) {
            taggedPlayers.remove(name.toLowerCase());
        }

        if (uuid != null && !uuid.isEmpty()) {
            taggedPlayers.entrySet().removeIf(e -> uuid.equalsIgnoreCase(e.getValue().uuid));
        }

        saveTags();
    }

    public static UrchinManager.Entry get(String name) {
        return name != null && !name.isEmpty() ? taggedPlayers.get(name.toLowerCase()) : null;
    }

    private static void loadTags() {
        if (FILE.exists()) {
            try (Reader reader = new FileReader(FILE)) {
                Map<String, UrchinManager.Entry> loaded = (Map<String, UrchinManager.Entry>)gson.fromJson(reader, TYPE);
                if (loaded != null) {
                    taggedPlayers = loaded;
                }
            } catch (Exception var13) {
                var13.printStackTrace();
            }
        }
    }

    private static void saveTags() {
        try (Writer writer = new FileWriter(FILE)) {
            gson.toJson(taggedPlayers, writer);
        } catch (IOException var13) {
            var13.printStackTrace();
        }
    }

    static {
        loadTags();
    }

    public static class Entry {
        public String name;
        public String uuid;
        public List<StatsContainer.UrchinTag> tags;
    }
}
