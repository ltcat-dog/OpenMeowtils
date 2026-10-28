package wtf.tatp.meowtils.module.advanced;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.resources.I18n;
import net.minecraft.init.Items;
import net.minecraft.inventory.ContainerChest;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryEnderChest;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.GuiOpenEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.BindValue;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.manager.session.Server;
import wtf.tatp.meowtils.mixin.AccessorGuiContainer;
import wtf.tatp.meowtils.util.DelayedTask;

public class AutoChest extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;
    @Config
    public int dumpKey = 0;
    @Config
    public int takeKey = 0;
    @Config
    public int delay = 100;
    @Config
    public int waitDelay = 100;
    @Config
    public boolean renderClicked = true;
    @Config
    public boolean enderChests = true;
    @Config
    public boolean normalChests = false;
    @Config
    public boolean iron = true;
    @Config
    public boolean gold = true;
    @Config
    public boolean diamonds = true;
    @Config
    public boolean emeralds = true;
    private static final String LOCAL_CHEST = I18n.format("container.chest", new Object[0]);
    private static final String LOCAL_ENDER_CHEST = I18n.format("container.enderchest", new Object[0]);
    public static final Set<Integer> CLICKED_SLOTS = new HashSet<>();
    private static long nextClickTime = 0L;
    private static boolean shouldDeposit = false;
    private static boolean shouldTake = false;
    private static boolean shouldDump = false;
    private static int lastCheckedSlot = 0;

    public AutoChest() {
        super("AutoChest", Module.Category.Advanced);
        this.tag(Module.ModuleTag.BLATANT);
        this.tooltip("Automatically deposit resources into a chest. Only works in bedwars.\n§cWARNING: §cLow §cdelays §care §cdetectable!");
        this.addSlider(new SliderValue("Delay", 0.0, 500.0, 50.0, "ms", "delay", this, int.class));
        this.addSlider(new SliderValue("Wait delay", 0.0, 500.0, 50.0, "ms", "waitDelay", this, int.class));
        this.addBind(new BindValue("Dump", "dumpKey", this));
        this.addBind(new BindValue("Take", "takeKey", this));
        this.addToggle(new ToggleValue("Render clicked", "renderClicked", this));
        this.addToggle(new ToggleValue("Allow for §5Enderchest", "enderChests", this));
        this.addToggle(new ToggleValue("Allow for §eChest", "normalChests", this));
        this.addCheck(new CheckValue("§7Iron Ingots", "iron", this));
        this.addCheck(new CheckValue("§6Gold Ingots", "gold", this));
        this.addCheck(new CheckValue("§bDiamonds", "diamonds", this));
        this.addCheck(new CheckValue("§2Emeralds", "emeralds", this));
    }

    @EventTarget
    public void onGuiOpen(GuiOpenEvent event) {
        if (!Bedwars.ALL.isNotActive()) {
            if (!(event.getGui() instanceof GuiChest)) {
                disable();
            } else {
                int openDelay = this.waitDelay / 50;
                if (event.getGui() instanceof GuiChest) {
                    GuiChest chest = (GuiChest)event.getGui();
                    if (chest.inventorySlots instanceof ContainerChest) {
                        ContainerChest container = (ContainerChest)chest.inventorySlots;
                        IInventory inventory = container.getLowerChestInventory();
                        if (this.allowedChest(inventory)) {
                            new DelayedTask(() -> {
                                shouldDeposit = true;
                                lastCheckedSlot = 0;
                            }, openDelay);
                        }
                    }
                }
            }
        }
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (this.mc.thePlayer != null && this.mc.theWorld != null && event.getPhase() == ClientTickEvent.Phase.PRE) {
            if (this.mc.currentScreen instanceof GuiChest) {
                if (!Bedwars.ALL.isNotActive()) {
                    if ((Mouse.isButtonDown(0) || Mouse.isButtonDown(1) || Mouse.isButtonDown(2)) && (shouldDump || shouldTake || shouldDeposit)) {
                        Meowtils.addMessage(EnumChatFormatting.RED + "You can't click while AutoChest is currently active, disabling.");
                        disable();
                    }

                    if (this.dumpKey != 0 && Keyboard.isKeyDown(this.dumpKey)) {
                        shouldDump = true;
                        lastCheckedSlot = 0;
                    }

                    if (this.takeKey != 0 && Keyboard.isKeyDown(this.takeKey)) {
                        shouldTake = true;
                        lastCheckedSlot = 0;
                    }

                    if (this.renderClicked) {
                        CLICKED_SLOTS.clear();
                    }

                    if (shouldDeposit || shouldTake || shouldDump) {
                        long now = System.currentTimeMillis();
                        if (now >= nextClickTime) {
                            nextClickTime = now + this.delay;
                            boolean clicked = this.activate((GuiContainer)this.mc.currentScreen);
                            if (!clicked) {
                                disable();
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean activate(GuiContainer gui) {
        for (int i = lastCheckedSlot; i < gui.inventorySlots.inventorySlots.size(); i++) {
            Slot slot = gui.inventorySlots.getSlot(i);
            if (slot != null && slot.getHasStack()) {
                if (!shouldDeposit && !shouldDump) {
                    if (!shouldTake) {
                        Meowtils.addMessage(EnumChatFormatting.RED + "Unable to activate AutoChest.");
                        return false;
                    }

                    if (slot.inventory == this.mc.thePlayer.inventory) {
                        continue;
                    }
                } else if (slot.inventory != this.mc.thePlayer.inventory) {
                    continue;
                }

                ItemStack stack = slot.getStack();
                if (stack != null && (shouldDump || this.allowedItem(stack.getItem()))) {
                    this.click(gui, slot, 0, 1);
                    lastCheckedSlot = i + 1;
                    return true;
                }
            }
        }

        return false;
    }

    private void click(GuiContainer gui, Slot slot, int mouseButton, int type) {
        ((AccessorGuiContainer)gui).clickSlot(slot, slot.slotNumber, mouseButton, type);
        if (this.renderClicked) {
            CLICKED_SLOTS.add(slot.slotNumber);
        }
    }

    private boolean allowedItem(Item item) {
        return this.iron && item == Items.iron_ingot
            || this.gold && item == Items.gold_ingot
            || this.diamonds && item == Items.diamond
            || this.emeralds && item == Items.emerald;
    }

    private boolean allowedChest(IInventory inventory) {
        String containerName = inventory.getDisplayName().getUnformattedText();
        if (this.enderChests && inventory instanceof InventoryEnderChest) {
            if (Server.HYPIXEL.isActive() && containerName.isEmpty()) {
                return true;
            }

            if (Server.HYPIXEL.isNotActive() && LOCAL_ENDER_CHEST.equals(containerName)) {
                return true;
            }
        }

        if (this.normalChests && !(inventory instanceof InventoryEnderChest)) {
            if (Server.HYPIXEL.isActive() && containerName.isEmpty()) {
                return true;
            }

            if (Server.HYPIXEL.isNotActive() && LOCAL_CHEST.equals(containerName)) {
                return true;
            }
        }

        return false;
    }

    private static void disable() {
        shouldDeposit = false;
        shouldTake = false;
        shouldDump = false;
    }

    @Override
    public void onDisable() {
        CLICKED_SLOTS.clear();
        disable();
    }
}
