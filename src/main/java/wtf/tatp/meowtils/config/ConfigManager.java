package wtf.tatp.meowtils.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.ModuleManager;

public class ConfigManager {
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final long SAVE_DELAY = 1000L;
    private static File configFile;
    private static boolean loading = false;
    private static long lastSave = 0L;
    private static boolean queuedSave = false;
    private static final Object SAVE_LOCK = new Object();
    public static GuiConfig guiConfig = new GuiConfig();

    public static void init(File file) {
        configFile = file;
    }

    public static void load() {
        loading = true;
        if (configFile == null) {
            throw new IllegalStateException("Config file is not set");
        } else if (!configFile.exists()) {
            save();
            Meowtils.warn("Created new config file");
        } else {
            try (FileReader reader = new FileReader(configFile)) {
                label178: {
                    JsonObject root = (JsonObject)gson.fromJson(reader, JsonObject.class);
                    if (root != null) {
                        if (root.has("gui")) {
                            guiConfig = (GuiConfig)gson.fromJson(root.get("gui"), GuiConfig.class);
                        }

                        Iterator var3 = ModuleManager.getModules().iterator();

                        while (true) {
                            if (!var3.hasNext()) {
                                break label178;
                            }

                            Module m = (Module)var3.next();
                            if (root.has(m.getName())) {
                                JsonObject mObj = root.getAsJsonObject(m.getName());

                                for (Field field : getAllFields(m.getClass())) {
                                    if (!Modifier.isStatic(field.getModifiers()) && field.isAnnotationPresent(Config.class) && mObj.has(field.getName())) {
                                        try {
                                            Object value = gson.fromJson(mObj.get(field.getName()), field.getType());
                                            field.set(m, value);
                                            if (field.getName().equals("enabled")) {
                                                boolean enabled = (Boolean)field.get(m);
                                                m.setState(enabled);
                                            }
                                        } catch (Exception var20) {
                                            var20.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    }

                    save();
                    loading = false;
                    Meowtils.error("Config is null or missing, creating new");
                    return;
                }
            } catch (Exception var23) {
                var23.printStackTrace();
            }

            loading = false;
        }
    }

    public static void save() {
        if (configFile != null) {
            if (!loading) {
                synchronized (SAVE_LOCK) {
                    queuedSave = true;
                    if (System.currentTimeMillis() - lastSave >= 1000L) {
                        saveFields();
                        queuedSave = false;
                    }
                }
            }
        }
    }

    private static void saveFields() {
        lastSave = System.currentTimeMillis();
        JsonObject root = new JsonObject();
        root.add("gui", gson.toJsonTree(guiConfig));

        for (Module m : ModuleManager.getModules()) {
            JsonObject mObj = new JsonObject();

            for (Field field : getAllFields(m.getClass())) {
                if (!Modifier.isStatic(field.getModifiers()) && !Modifier.isTransient(field.getModifiers()) && field.isAnnotationPresent(Config.class)) {
                    field.setAccessible(true);

                    try {
                        Object value = field.get(m);
                        mObj.add(field.getName(), gson.toJsonTree(value));
                    } catch (Exception var17) {
                        var17.printStackTrace();
                    }
                }
            }

            root.add(m.getName(), mObj);
        }

        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(root, writer);
        } catch (Exception var19) {
            var19.printStackTrace();
        }
    }

    private static List<Field> getAllFields(Class<?> type) {
        List<Field> fields = new ArrayList<>(Arrays.asList(type.getDeclaredFields()));
        Class<?> parent = type.getSuperclass();
        if (parent != null && parent != Object.class) {
            fields.addAll(Arrays.asList(parent.getDeclaredFields()));
        }

        return fields;
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null && mc.theWorld != null && event.getPhase() == ClientTickEvent.Phase.POST) {
            if (configFile != null) {
                synchronized (SAVE_LOCK) {
                    if (queuedSave) {
                        if (System.currentTimeMillis() - lastSave >= 1000L) {
                            saveFields();
                            queuedSave = false;
                        }
                    }
                }
            }
        }
    }
}
