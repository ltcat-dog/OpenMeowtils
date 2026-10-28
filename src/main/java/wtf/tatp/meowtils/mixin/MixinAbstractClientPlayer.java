package wtf.tatp.meowtils.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.CapeManager;
import wtf.tatp.meowtils.manager.SkinManager;
import wtf.tatp.meowtils.module.hypixel.AccountHider;
import wtf.tatp.meowtils.module.render.Cape;

@Mixin({AbstractClientPlayer.class})
public abstract class MixinAbstractClientPlayer extends EntityPlayer {
    public MixinAbstractClientPlayer(World world, GameProfile profile) {
        super(world, profile);
    }

    @Inject(
        method = {"getLocationCape"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void meowtils$getLocationCape(CallbackInfoReturnable<ResourceLocation> cir) {
        Cape c = Module.get(Cape.class);
        if (c.enabled && ((Object)(Object)this instanceof EntityPlayerSP || (Object)this instanceof EntityPlayer && c.renderOnAll)) {
            ResourceLocation cape = CapeManager.getCape(c.selectedCape);
            if (cape != null) {
                cir.setReturnValue(cape);
            }
        }
    }

    @Inject(
        method = {"getLocationSkin"},
        at = {@At("RETURN")},
        cancellable = true
    )
    private void meowtils$getLocationSkin(CallbackInfoReturnable<ResourceLocation> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer)(Object)this;
        if (Module.get(AccountHider.class).enabled) {
            if (Module.get(AccountHider.class).skin) {
                if (player != null && player == Minecraft.getMinecraft().thePlayer) {
                    ResourceLocation skin = SkinManager.getSkin(Module.get(AccountHider.class).skinLocation);
                    if (skin != null) {
                        cir.setReturnValue(skin);
                    }
                }
            }
        }
    }

    @Inject(
        method = {"getSkinType"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void meowtils$getSkinType(CallbackInfoReturnable<String> cir) {
        AbstractClientPlayer player = (AbstractClientPlayer)(Object)this;
        AccountHider a = Module.get(AccountHider.class);
        if (a.enabled && a.skin && player == Minecraft.getMinecraft().thePlayer) {
            if (a.armMode.equals("Default")) {
                cir.setReturnValue("default");
            } else if (a.armMode.equals("Slim")) {
                cir.setReturnValue("slim");
            } else {
                cir.setReturnValue("default");
            }
        }
    }
}
