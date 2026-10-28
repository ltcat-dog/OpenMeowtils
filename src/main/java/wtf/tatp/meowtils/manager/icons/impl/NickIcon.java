package wtf.tatp.meowtils.manager.icons.impl;

import com.mojang.authlib.GameProfile;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.icons.IconProvider;
import wtf.tatp.meowtils.module.hypixel.Denicker;
import wtf.tatp.meowtils.util.PlayerUtil;

public class NickIcon implements IconProvider {
    @Override
    public String getSuffix(GameProfile profile, boolean tablist, boolean nametag) {
        if (!Module.get(Denicker.class).enabled) {
            return "";
        } else if (!Module.get(Denicker.class).icon) {
            return "";
        } else {
            return PlayerUtil.isNicked(profile) ? Denicker.getIcon() : "";
        }
    }
}
