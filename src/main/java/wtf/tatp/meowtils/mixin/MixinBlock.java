package wtf.tatp.meowtils.mixin;

import net.minecraft.block.Block;
import net.minecraft.block.BlockColored;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.manager.session.Server;
import wtf.tatp.meowtils.module.bedwars.HeightOverlay;

@Mixin({Block.class})
public abstract class MixinBlock {
    @Inject(
        method = {"getActualState"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void meowtils$getActualState(IBlockState state, IBlockAccess access, BlockPos pos, CallbackInfoReturnable<IBlockState> cir) {
        if ((Object)this == Blocks.wool) {
            if (!Server.HYPIXEL.isNotActive()) {
                if (!Bedwars.GAME.isNotActive()) {
                    if (Module.get(HeightOverlay.class).woolOverlay) {
                        if (pos.getY() >= HeightOverlay.getHeight() - 1) {
                            cir.setReturnValue(state.withProperty(BlockColored.COLOR, HeightOverlay.getWoolColor()));
                        }
                    }
                }
            }
        }
    }
}
