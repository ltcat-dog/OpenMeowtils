package wtf.tatp.meowtils.mixin;

import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.module.render.Animations;

@Mixin({ItemRenderer.class})
public abstract class MixinItemRenderer {
    private final Animations s = Module.get(Animations.class);

    @Redirect(
        method = {"renderItemInFirstPerson"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/item/ItemStack;getItemUseAction()Lnet/minecraft/item/EnumAction;"
        )
    )
    private EnumAction meowtils$renderItemInFirstPersonCancelUse(ItemStack stack) {
        if (stack == null) {
            return EnumAction.NONE;
        } else {
            EnumAction action = stack.getItemUseAction();
            if (action == EnumAction.EAT && this.s.enabled && this.s.cancelConsume) {
                return EnumAction.NONE;
            } else if (action == EnumAction.DRINK && this.s.enabled && this.s.cancelConsume) {
                return EnumAction.NONE;
            } else {
                return action == EnumAction.BOW && this.s.enabled && this.s.cancelBow ? EnumAction.NONE : action;
            }
        }
    }

    @Redirect(
        method = {"renderItemInFirstPerson(F)V"},
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/entity/AbstractClientPlayer;getSwingProgress(F)F"
        )
    )
    private float meowtils$renderItemInFirstPersonCancelSwing(AbstractClientPlayer player, float partialTicks) {
        return !this.s.enabled || !this.s.cancelSwing || this.s.cancelSwingRightClick && !player.isBlocking() ? player.getSwingProgress(partialTicks) : 0.0F;
    }
}
