package wtf.tatp.meowtils.util;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.player.EntityPlayer;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventManager;
import wtf.tatp.meowtils.event.api.EventTarget;

public class MojangNameToUUID {
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);

    public static void lookup(String name, Consumer<String> callback) {
        EXECUTOR.submit(() -> {
            String local = lookupLocal(name);
            String result = local != null ? local : lookupFromAPI(name);
            new MojangNameToUUID.MainThreadCallback(result, callback);
        });
    }

    private static String lookupLocal(String name) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld != null) {
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                GameProfile gameProfile = player.getGameProfile();
                if (gameProfile.getName().equalsIgnoreCase(name)) {
                    return gameProfile.getId().toString();
                }
            }
        }

        if (mc.getNetHandler() != null) {
            for (NetworkPlayerInfo info : mc.getNetHandler().getPlayerInfoMap()) {
                GameProfile gameProfile = info.getGameProfile();
                if (gameProfile.getName().equalsIgnoreCase(name)) {
                    return gameProfile.getId().toString();
                }
            }
        }

        return null;
    }

    private static String lookupFromAPI(String name) {
        HttpURLConnection connection = null;

        String var7;
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setReadTimeout(5000);
            connection.setConnectTimeout(5000);
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            if (connection.getResponseCode() != 200) {
                return null;
            }

            try (InputStreamReader reader = new InputStreamReader(connection.getInputStream())) {
                JsonObject response = new JsonParser().parse(reader).getAsJsonObject();
                String raw = response.get("id").getAsString();
                var7 = raw.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5");
            }
        } catch (Exception var27) {
            return null;
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return var7;
    }

    private static class MainThreadCallback {
        private final String uuid;
        private final Consumer<String> callback;

        public MainThreadCallback(String uuid, Consumer<String> callback) {
            this.uuid = uuid;
            this.callback = callback;
            EventManager.register(this);
        }

        @EventTarget
        public void onClientTick(ClientTickEvent event) {
            if (event.getPhase() == ClientTickEvent.Phase.PRE) {
                try {
                    if (Minecraft.getMinecraft().theWorld != null && Minecraft.getMinecraft().thePlayer != null) {
                        this.callback.accept(this.uuid);
                    }
                } finally {
                    EventManager.unregister(this);
                }
            }
        }
    }
}
