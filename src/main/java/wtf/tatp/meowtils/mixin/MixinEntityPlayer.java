package wtf.tatp.meowtils.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wtf.tatp.meowtils.manager.icons.IconManager;

@Mixin({EntityPlayer.class})
public class MixinEntityPlayer {
    @Inject(
        method = {"getDisplayName"},
        at = {@At("RETURN")},
        cancellable = true
    )
    private void meowtils$getDisplayName(CallbackInfoReturnable<IChatComponent> cir) {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null && mc.theWorld != null) {
            EntityPlayer player = (EntityPlayer)(Object)this;
            String prefix = IconManager.buildPrefix(player.getGameProfile(), false, true);
            String suffix = IconManager.buildSuffix(player.getGameProfile(), false, true);
            if (prefix != null && !prefix.isEmpty() || suffix != null && !suffix.isEmpty()) {
                IChatComponent originalComponent = (IChatComponent)cir.getReturnValue();
                IChatComponent modifiedComponent = new ChatComponentText("");
                if (prefix != null && !prefix.isEmpty()) {
                    modifiedComponent.appendSibling(new ChatComponentText(prefix));
                }

                modifiedComponent.appendSibling(originalComponent);
                if (suffix != null && !suffix.isEmpty()) {
                    modifiedComponent.appendSibling(new ChatComponentText(suffix));
                }

                cir.setReturnValue(modifiedComponent);
            }
        }
    }
}
