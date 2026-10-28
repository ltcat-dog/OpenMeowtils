package wtf.tatp.meowtils.module.bedwars;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemArmor.ArmorMaterial;
import net.minecraft.util.EnumChatFormatting;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.manager.session.Duels;
import wtf.tatp.meowtils.module.hypixel.PartyNotifier;
import wtf.tatp.meowtils.util.NameUtil;
import wtf.tatp.meowtils.util.TeamUtil;
import wtf.tatp.meowtils.util.Util;

public class ArmorAlerts extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;
    @Config
    public boolean pingSound = true;
    @Config
    public boolean chain = true;
    @Config
    public boolean iron = true;
    @Config
    public boolean diamond = true;
    @Config
    public String alertType = "Chat";
    private static int tickCounter = 0;
    private static final Map<UUID, ArmorMaterial> ALERTED_ARMOR = new HashMap<>();

    public ArmorAlerts() {
        super("ArmorAlerts", Module.Category.Bedwars);
        this.tag(Module.ModuleTag.SAFE);
        this.tooltip("Alerts you when players buy an armor upgrade.");
        this.addMode(new ModeValue("Alert", Arrays.asList("Chat", "Notification", "All"), "alertType", this));
        this.addToggle(new ToggleValue("Ping sound", "pingSound", this));
        this.addCheck(new CheckValue("§7Chainmail Armor", "chain", this));
        this.addCheck(new CheckValue("Iron Armor", "iron", this));
        this.addCheck(new CheckValue("§bDiamond Armor", "diamond", this));
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (this.mc.thePlayer != null && this.mc.theWorld != null && event.getPhase() == ClientTickEvent.Phase.POST) {
            if (!Bedwars.GAME.isNotActive() || !Duels.BEDWARS.isNotActive()) {
                tickCounter++;
                if (tickCounter >= 20) {
                    tickCounter = 0;

                    for (EntityPlayer player : this.mc.theWorld.playerEntities) {
                        if (player != null
                            && player.ticksExisted >= 60
                            && player != this.mc.thePlayer
                            && !TeamUtil.isBot(player)
                            && !TeamUtil.isTeam(player)
                            && !TeamUtil.ignoreFriends(player.getUniqueID().toString())
                            && !TeamUtil.ignoreFriends(player.getName())) {
                            this.checkArmor(player);
                        }
                    }
                }
            }
        }
    }

    private void checkArmor(EntityPlayer player) {
        ItemStack leggings = player.inventory.armorInventory[1];
        if (leggings != null && leggings.getItem() instanceof ItemArmor) {
            ItemArmor armor = (ItemArmor)leggings.getItem();
            ArmorMaterial material = armor.getArmorMaterial();
            if (material != ArmorMaterial.LEATHER) {
                if (material != ArmorMaterial.CHAIN || this.chain) {
                    if (material != ArmorMaterial.IRON || this.iron) {
                        if (material != ArmorMaterial.DIAMOND || this.diamond) {
                            ArmorMaterial lastMaterial = ALERTED_ARMOR.get(player.getUniqueID());
                            int current = this.getPriority(material);
                            int last = this.getPriority(lastMaterial);
                            if (current > last) {
                                ALERTED_ARMOR.put(player.getUniqueID(), material);
                                this.alert(player, material);
                            }
                        }
                    }
                }
            }
        }
    }

    private int getPriority(ArmorMaterial material) {
        if (material == null) {
            return 0;
        } else {
            switch (material) {
                case CHAIN:
                    return 1;
                case IRON:
                    return 2;
                case DIAMOND:
                    return 3;
                default:
                    return 0;
            }
        }
    }

    private void alert(EntityPlayer player, ArmorMaterial material) {
        String name = NameUtil.getTabDisplayName(player.getName());
        String armorName;
        EnumChatFormatting color;
        switch (material) {
            case CHAIN:
                armorName = "Chainmail Armor";
                color = EnumChatFormatting.DARK_GRAY;
                break;
            case IRON:
                armorName = "Iron Armor";
                color = EnumChatFormatting.WHITE;
                break;
            case DIAMOND:
                armorName = "Diamond Armor";
                color = EnumChatFormatting.AQUA;
                break;
            default:
                return;
        }

        String text = name + EnumChatFormatting.GRAY + " purchased " + color + armorName;
        if (!this.alertType.equals("Notification")) {
            Meowtils.addMessage(text);
        }

        if (!this.alertType.equals("Chat")) {
            NotificationManager.show("ArmorAlerts", text, NotificationManager.Type.ALERT, 1500L);
        }

        if (this.pingSound) {
            Util.playSound(Util.Sound.PING, 100);
        }

        PartyNotifier.armorAlerts(player.getName(), armorName);
    }

    @Override
    public void onReset() {
        ALERTED_ARMOR.clear();
    }
}
