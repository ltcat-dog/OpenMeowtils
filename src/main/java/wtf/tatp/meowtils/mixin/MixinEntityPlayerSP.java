package wtf.tatp.meowtils.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import wtf.tatp.meowtils.module.render.Animations;

@Mixin({EntityPlayerSP.class})
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer {
    protected MixinEntityPlayerSP(World world, GameProfile profile) {
        super(world, profile);
    }

    public int getItemInUseCount() {
        return Animations.shouldBlock() ? 10 : super.getItemInUseCount();
    }

    public boolean isBlocking() {
        return Animations.shouldBlock() || super.isBlocking();
    }
}
