package wtf.tatp.meowtils.module.bedwars;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.World;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.MouseInputEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.manager.session.Duels;
import wtf.tatp.meowtils.module.meowtils.Notifications;

public class AntiMisplace extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;

    public AntiMisplace() {
        super("AntiMisplace", Module.Category.Bedwars);
        this.tag(Module.ModuleTag.LEGIT);
        this.tooltip("Prevents placing obsidian if it isn't around a bed.");
    }

    @EventTarget
    public void onMouseInput(MouseInputEvent event) {
        if (this.mc.thePlayer != null && this.mc.theWorld != null) {
            if (this.mc.currentScreen == null) {
                if (event.getAction() == MouseInputEvent.Action.RIGHT_CLICK) {
                    if (this.mc.objectMouseOver != null && this.mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK) {
                        if (!Bedwars.GAME.isNotActive() || !Duels.BEDWARS.isNotActive()) {
                            if (this.mc.thePlayer.getCurrentEquippedItem() != null) {
                                if (this.mc.thePlayer.getCurrentEquippedItem().getItem() == Item.getItemFromBlock(Blocks.obsidian)) {
                                    if (!this.isAdjacentToBed(
                                        this.mc.theWorld, this.mc.objectMouseOver.getBlockPos().offset(this.mc.objectMouseOver.sideHit)
                                    )) {
                                        event.setCancelled(true);
                                        KeyBinding.setKeyBindState(this.mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                                        if (Notifications.getMode() != Notifications.Mode.NOTIFICATION) {
                                            Meowtils.addMessage(EnumChatFormatting.RED + "Prevented you from placing obsidian!");
                                        }

                                        if (Notifications.getMode() != Notifications.Mode.CHAT) {
                                            NotificationManager.show("AntiMisplace", "Can't place here!", NotificationManager.Type.ALERT, 1500L);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean isAdjacentToBed(World world, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            if (world.getBlockState(pos.offset(facing)).getBlock() == Blocks.bed) {
                return true;
            }
        }

        return false;
    }
}
