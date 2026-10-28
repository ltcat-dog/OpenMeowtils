package wtf.tatp.meowtils.module.bedwars;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S0DPacketCollectItem;
import net.minecraft.util.EnumChatFormatting;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.ReceivePacketEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.manager.session.Duels;
import wtf.tatp.meowtils.util.NameUtil;
import wtf.tatp.meowtils.util.TeamUtil;
import wtf.tatp.meowtils.util.Util;

public class PickupAlerts extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;
    @Config
    public int cooldown = 2;
    @Config
    public boolean pingSound = true;
    @Config
    public boolean iron = false;
    @Config
    public boolean gold = false;
    @Config
    public boolean diamonds = true;
    @Config
    public boolean emeralds = true;
    private static final Map<UUID, Long> COOLDOWN = new HashMap<>();

    public PickupAlerts() {
        super("PickupAlerts", Module.Category.Bedwars);
        this.tag(Module.ModuleTag.SAFE);
        this.tooltip("Alerts you when someone picks up a resource.");
        this.addSlider(new SliderValue("Cooldown", 0.0, 10.0, 1.0, "s", "cooldown", this, int.class));
        this.addToggle(new ToggleValue("Ping sound", "pingSound", this));
        this.addCheck(new CheckValue("For §7Iron Ingots", "iron", this));
        this.addCheck(new CheckValue("For §6Gold Ingots", "gold", this));
        this.addCheck(new CheckValue("For §bDiamonds", "diamonds", this));
        this.addCheck(new CheckValue("For §2Emeralds", "emeralds", this));
    }

    @EventTarget
    public void onReceivePacket(ReceivePacketEvent event) {
        if (event.getPacket() instanceof S0DPacketCollectItem) {
            if (Bedwars.GAME.isNotActive() && Duels.BEDWARS.isNotActive()) {
                return;
            }

            S0DPacketCollectItem packet = (S0DPacketCollectItem)event.getPacket();
            Entity entity = this.mc.theWorld.getEntityByID(packet.getEntityID());
            Entity item = this.mc.theWorld.getEntityByID(packet.getCollectedItemEntityID());
            long alertCooldown = this.cooldown * 1000L;
            if (!(entity instanceof EntityPlayer)) {
                return;
            }

            EntityPlayer player = (EntityPlayer)entity;
            if (player == this.mc.thePlayer) {
                return;
            }

            if (TeamUtil.isTeam(player)) {
                return;
            }

            if (TeamUtil.ignoreFriends(player.getUniqueID().toString()) || TeamUtil.ignoreFriends(player.getName())) {
                return;
            }

            if (!(item instanceof EntityItem)) {
                return;
            }

            ItemStack itemStack = ((EntityItem)item).getEntityItem();
            if (itemStack == null) {
                return;
            }

            if (!isAllowedItem(itemStack.getItem())) {
                return;
            }

            String itemName = itemStack.getDisplayName();
            long now = System.currentTimeMillis();
            long lastPickup = COOLDOWN.getOrDefault(player.getUniqueID(), 0L);
            if (now - lastPickup <= alertCooldown) {
                return;
            }

            COOLDOWN.put(player.getUniqueID(), now);
            Meowtils.addMessage(
                NameUtil.getTabDisplayName(player.getName()) + EnumChatFormatting.GRAY + " picked up " + itemColor(itemStack.getItem()) + itemName
            );
            if (this.pingSound) {
                Util.playSound(Util.Sound.PING_MEDIUM, 100);
            }
        }
    }

    private static boolean isAllowedItem(Item item) {
        PickupAlerts p = Module.get(PickupAlerts.class);
        if (p.iron && item == Items.iron_ingot) {
            return true;
        } else if (p.gold && item == Items.gold_ingot) {
            return true;
        } else {
            return p.diamonds && item == Items.diamond ? true : p.emeralds && item == Items.emerald;
        }
    }

    private static EnumChatFormatting itemColor(Item item) {
        if (item == Items.iron_ingot) {
            return EnumChatFormatting.WHITE;
        } else if (item == Items.gold_ingot) {
            return EnumChatFormatting.GOLD;
        } else if (item == Items.diamond) {
            return EnumChatFormatting.AQUA;
        } else {
            return item == Items.emerald ? EnumChatFormatting.DARK_GREEN : EnumChatFormatting.RED;
        }
    }

    @Override
    public void onReset() {
        COOLDOWN.clear();
    }

    @Override
    public void onDisable() {
        COOLDOWN.clear();
    }
}
