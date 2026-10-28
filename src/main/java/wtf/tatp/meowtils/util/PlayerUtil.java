package wtf.tatp.meowtils.util;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;

public class PlayerUtil {
    public static boolean isNicked(GameProfile profile) {
        if (profile == null) {
            return false;
        } else {
            UUID uuid = profile.getId();
            return uuid == null ? false : uuid.version() == 1;
        }
    }

    public static GameProfile getProfile(String name) {
        for (NetworkPlayerInfo info : Minecraft.getMinecraft().getNetHandler().getPlayerInfoMap()) {
            if (info.getGameProfile().getName().equalsIgnoreCase(name)) {
                return info.getGameProfile();
            }
        }

        return null;
    }
}
