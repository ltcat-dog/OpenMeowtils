package wtf.tatp.meowtils.module.hypixel;

import java.util.LinkedList;
import java.util.Queue;
import net.minecraft.util.EnumChatFormatting;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.config.ConfigManager;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.ButtonValue;
import wtf.tatp.meowtils.gui.values.ExpandValue;
import wtf.tatp.meowtils.gui.values.TextValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.handler.PartyHandler;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.manager.session.Server;
import wtf.tatp.meowtils.module.meowtils.Notifications;
import wtf.tatp.meowtils.util.ColorUtil;

public class PartyNotifier extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;
    @Config
    public boolean showPrefix = true;
    @Config
    public boolean antiCheat = true;
    @Config
    public boolean denicker = true;
    @Config
    public boolean armorAlerts = false;
    @Config
    public boolean bedTracker = false;
    @Config
    public boolean consumeAlerts = false;
    @Config
    public boolean itemAlerts = false;
    @Config
    public boolean upgradeAlerts = false;
    @Config
    public boolean partyDetector = false;
    @Config
    public boolean urchin = true;
    @Config
    public String antiCheatMessage = "#player failed #check";
    @Config
    public String denickerMessage = "#player is nicked.";
    @Config
    public String denickerMessageFull = "#denicked is nicked as #name.";
    @Config
    public String armorAlertsMessage = "#player purchased #armor";
    @Config
    public String bedTrackerMessage = "#player is #distance blocks from your bed! #warning";
    @Config
    public String consumeAlertsMessage = "#player consumed #item";
    @Config
    public String itemAlertsMessage = "#player has #item";
    @Config
    public String upgradeAlertsMessage = "#team purchased #upgrade";
    @Config
    public String partyDetectorMessage = "Warning: #amount players joined! (Party)";
    @Config
    public String urchinMessage = "#player is tagged on Urchin for #tag";
    private static final String ANTICHEAT_DEFAULT = "#player failed #check";
    private static final String DENICKER_DEFAULT = "#player is nicked.";
    private static final String DENICKER_FULL_DEFAULT = "#denicked is nicked as #name.";
    private static final String ARMORALERTS_DEFAULT = "#player purchased #armor";
    private static final String BEDTRACKER_DEFAULT = "#player is #distance blocks from your bed! #warning";
    private static final String CONSUMEALERTS_DEFAULT = "#player consumed #item";
    private static final String ITEMALERTS_DEFAULT = "#player has #item";
    private static final String UPGRADEALERTS_DEFAULT = "#team purchased #upgrade";
    private static final String PARTYDETECTOR_DEFAULT = "Warning: #amount players joined! (Party)";
    private static final String URCHIN_DEFAULT = "#player is tagged on Urchin for #tag";
    private static final Queue<String> MESSAGE_QUEUE = new LinkedList<>();
    private static int tickCounter = 0;

    public PartyNotifier() {
        super("PartyNotifier", Module.Category.Hypixel);
        this.tag(Module.ModuleTag.LEGIT);
        this.tooltip("Sends a notification to party chat from selected modules.");
        this.addToggle(new ToggleValue("Show prefix", "showPrefix", this));
        this.addButton(new ButtonValue("Reset messages", 5.0F, () -> {
            resetDefault();
            if (Notifications.getMode() != Notifications.Mode.NOTIFICATION) {
                Meowtils.addMessage("Reset all PartyNotifier messages.");
            }

            if (Notifications.getMode() != Notifications.Mode.CHAT) {
                NotificationManager.show("PartyNotifier", "Reset messages.", NotificationManager.Type.INFO, 2000L);
            }
        }));
        this.addExpand(new ExpandValue("AntiCheat notify", e -> {
            e.addToggle(new ToggleValue("Enabled", "antiCheat", this));
            e.addText(new TextValue(null, "#player #check", "antiCheatMessage", this));
        }));
        this.addExpand(new ExpandValue("Denicker notify", e -> {
            e.addToggle(new ToggleValue("Enabled", "denicker", this));
            e.addText(new TextValue(null, "#player", "denickerMessage", this));
            e.addText(new TextValue(null, "#player #denicked", "denickerMessageFull", this));
        }));
        this.addExpand(new ExpandValue("Urchin notify", e -> {
            e.addToggle(new ToggleValue("Enabled", "urchin", this));
            e.addText(new TextValue(null, "#player #tag", "urchinMessage", this));
        }));
        this.addExpand(new ExpandValue("ArmorAlerts notify", e -> {
            e.addToggle(new ToggleValue("Enabled", "armorAlerts", this));
            e.addText(new TextValue(null, "#player #armor", "armorAlertsMessage", this));
        }));
        this.addExpand(new ExpandValue("BedTracker notify", e -> {
            e.addToggle(new ToggleValue("Enabled", "bedTracker", this));
            e.addText(new TextValue(null, "#player #distance #warning", "bedTrackerMessage", this));
        }));
        this.addExpand(new ExpandValue("ConsumeAlerts notify", e -> {
            e.addToggle(new ToggleValue("Enabled", "consumeAlerts", this));
            e.addText(new TextValue(null, "#player #item", "consumeAlertsMessage", this));
        }));
        this.addExpand(new ExpandValue("ItemAlerts notify", e -> {
            e.addToggle(new ToggleValue("Enabled", "itemAlerts", this));
            e.addText(new TextValue(null, "#player #item", "itemAlertsMessage", this));
        }));
        this.addExpand(new ExpandValue("UpgradeAlerts notify", e -> {
            e.addToggle(new ToggleValue("Enabled", "upgradeAlerts", this));
            e.addText(new TextValue(null, "#team #upgrade", "upgradeAlertsMessage", this));
        }));
        this.addExpand(new ExpandValue("PartyDetector notify", e -> {
            e.addToggle(new ToggleValue("Enabled", "partyDetector", this));
            e.addText(new TextValue(null, "#amount", "partyDetectorMessage", this));
        }));
    }

    private static PartyNotifier p() {
        return Module.get(PartyNotifier.class);
    }

    private static void resetDefault() {
        p().antiCheatMessage = "#player failed #check";
        p().denickerMessage = "#player is nicked.";
        p().denickerMessageFull = "#denicked is nicked as #name.";
        p().armorAlertsMessage = "#player purchased #armor";
        p().bedTrackerMessage = "#player is #distance blocks from your bed! #warning";
        p().consumeAlertsMessage = "#player consumed #item";
        p().itemAlertsMessage = "#player has #item";
        p().upgradeAlertsMessage = "#team purchased #upgrade";
        p().partyDetectorMessage = "Warning: #amount players joined! (Party)";
        p().urchinMessage = "#player is tagged on Urchin for #tag";
        ConfigManager.save();
    }

    private static void notifyParty(String message) {
        MESSAGE_QUEUE.add(message);
    }

    private void alert(String message) {
        if (!Server.HYPIXEL.isNotActive()) {
            Meowtils.debugMessage(EnumChatFormatting.YELLOW + "[Notify]: " + EnumChatFormatting.WHITE + "Sent: " + message);
            if (PartyHandler.inParty()) {
                if (this.showPrefix) {
                    Meowtils.sendMessage(message);
                } else {
                    Meowtils.sendCleanMessage("/pc " + message);
                }
            }
        }
    }

    private static void handleError(Exception e) {
        resetDefault();
        Meowtils.addMessage(EnumChatFormatting.RED + "There was an error in PartyNotifier. It has been reset.");
        Meowtils.error("Error in PartyNotifier: " + e);
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (this.mc.thePlayer != null && this.mc.theWorld != null && event.getPhase() == ClientTickEvent.Phase.POST) {
            tickCounter++;
            if (tickCounter >= 20) {
                tickCounter = 0;
                if (MESSAGE_QUEUE.size() > 20) {
                    MESSAGE_QUEUE.clear();
                    Meowtils.addMessage(EnumChatFormatting.RED + "Error notifying party. Cleared queue.");
                }

                if (!MESSAGE_QUEUE.isEmpty()) {
                    this.alert(MESSAGE_QUEUE.poll());
                }
            }
        }
    }

    public static void antiCheat(String player, String check) {
        if (p().enabled && p().antiCheat) {
            try {
                notifyParty(p().antiCheatMessage.replace("#player", ColorUtil.unformattedText(player)).replace("#check", check));
            } catch (Exception var3) {
                handleError(var3);
            }
        }
    }

    public static void denicker(String player, String denicked, boolean full) {
        if (p().enabled && p().denicker) {
            try {
                if (full) {
                    notifyParty(p().denickerMessageFull.replace("#denicked", denicked).replace("#name", player));
                } else {
                    notifyParty(p().denickerMessage.replace("#player", player));
                }
            } catch (Exception var4) {
                handleError(var4);
            }
        }
    }

    public static void armorAlerts(String player, String armor) {
        if (p().enabled && p().armorAlerts) {
            try {
                notifyParty(p().armorAlertsMessage.replace("#player", player).replace("#armor", armor));
            } catch (Exception var3) {
                handleError(var3);
            }
        }
    }

    public static void bedTracker(String player, int distance) {
        if (p().enabled && p().bedTracker) {
            try {
                notifyParty(p().bedTrackerMessage.replace("#player", player).replace("#distance", String.valueOf(distance)).replace("#warning", "⚠"));
            } catch (Exception var3) {
                handleError(var3);
            }
        }
    }

    public static void consumeAlerts(String player, String item) {
        if (p().enabled && p().consumeAlerts) {
            try {
                notifyParty(p().consumeAlertsMessage.replace("#player", player).replace("#item", item));
            } catch (Exception var3) {
                handleError(var3);
            }
        }
    }

    public static void itemAlerts(String player, String item) {
        if (p().enabled && p().itemAlerts) {
            try {
                notifyParty(p().itemAlertsMessage.replace("#player", player).replace("#item", item));
            } catch (Exception var3) {
                handleError(var3);
            }
        }
    }

    public static void upgradeAlerts(String team, String upgrade) {
        if (p().enabled && p().upgradeAlerts) {
            try {
                notifyParty(p().upgradeAlertsMessage.replace("#team", team).replace("#upgrade", upgrade));
            } catch (Exception var3) {
                handleError(var3);
            }
        }
    }

    public static void partyDetector(int amount) {
        if (p().enabled && p().partyDetector) {
            try {
                notifyParty(p().partyDetectorMessage.replace("#amount", String.valueOf(amount)));
            } catch (Exception var2) {
                handleError(var2);
            }
        }
    }

    public static void urchin(String name, String tag) {
        if (p().enabled && p().urchin) {
            try {
                notifyParty(p().urchinMessage.replace("#player", name).replace("#tag", tag));
            } catch (Exception var3) {
                handleError(var3);
            }
        }
    }
}
