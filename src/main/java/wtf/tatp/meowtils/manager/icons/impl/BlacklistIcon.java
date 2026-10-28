package wtf.tatp.meowtils.manager.icons.impl;

import com.mojang.authlib.GameProfile;
import net.minecraft.util.EnumChatFormatting;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.icons.IconProvider;
import wtf.tatp.meowtils.manager.lists.BlacklistManager;
import wtf.tatp.meowtils.manager.lists.SafelistManager;
import wtf.tatp.meowtils.module.meowtils.Icons;

public class BlacklistIcon implements IconProvider {
    private static final String ICON = EnumChatFormatting.BOLD + "⚠ " + EnumChatFormatting.RESET;
    private static final String LIGHT_PURPLE_ICON = EnumChatFormatting.LIGHT_PURPLE.toString() + EnumChatFormatting.BOLD + "⚠ " + EnumChatFormatting.RESET;

    @Override
    public String getPrefix(GameProfile profile, boolean tablist, boolean nametag) {
        if (!Module.get(Icons.class).blacklistIcon) {
            return "";
        } else if (nametag && !Icons.displayInNametag()) {
            return "";
        } else if (tablist && !Icons.displayInTab()) {
            return "";
        } else {
            boolean blacklisted = BlacklistManager.isBlacklisted(profile.getId().toString()) || BlacklistManager.isBlacklisted(profile.getName());
            boolean safelisted = SafelistManager.isSafelisted(profile.getId().toString()) || SafelistManager.isSafelisted(profile.getName());
            String entry = BlacklistManager.getEntry(profile.getId().toString());
            if (entry == null) {
                entry = BlacklistManager.getEntry(profile.getName());
            }

            EnumChatFormatting color = BlacklistManager.getReasonColor(entry);
            if (!blacklisted) {
                return "";
            } else {
                return safelisted ? LIGHT_PURPLE_ICON : color.toString() + ICON;
            }
        }
    }
}
