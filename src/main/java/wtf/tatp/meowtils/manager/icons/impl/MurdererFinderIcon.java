package wtf.tatp.meowtils.manager.icons.impl;

import com.mojang.authlib.GameProfile;
import java.util.UUID;
import net.minecraft.util.EnumChatFormatting;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.icons.IconProvider;
import wtf.tatp.meowtils.manager.session.MurderMystery;
import wtf.tatp.meowtils.module.hypixel.MurdererFinder;

public class MurdererFinderIcon implements IconProvider {
    private static final String MURDERER_ICON = EnumChatFormatting.DARK_RED.toString() + EnumChatFormatting.BOLD + " !!" + EnumChatFormatting.RESET;
    private static final String DETECTIVE_ICON = EnumChatFormatting.GOLD + " ➹" + EnumChatFormatting.RESET;

    @Override
    public String getSuffix(GameProfile profile, boolean tablist, boolean nametag) {
        MurdererFinder m = Module.get(MurdererFinder.class);
        if (!m.enabled) {
            return "";
        } else if (!m.nameIcons) {
            return "";
        } else if (MurderMystery.ALL.isNotActive()) {
            return "";
        } else {
            UUID uuid = profile.getId();
            return MurdererFinder.isMurderer(uuid) ? MURDERER_ICON : (MurdererFinder.isDetective(uuid) ? DETECTIVE_ICON : "");
        }
    }
}
