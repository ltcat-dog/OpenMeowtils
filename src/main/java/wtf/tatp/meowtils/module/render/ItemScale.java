package wtf.tatp.meowtils.module.render;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemAxe;
import net.minecraft.item.ItemHoe;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemSword;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;

public class ItemScale extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;
    @Config
    public float scale = 2.5F;
    @Config
    public boolean importantOnly = false;
    @Config
    public boolean resources = true;
    @Config
    public boolean gear = true;
    @Config
    public boolean heads = true;

    public ItemScale() {
        super("ItemScale", Module.Category.Render);
        this.tag(Module.ModuleTag.LEGIT);
        this.tooltip("Scale dropped items.");
        this.addSlider(new SliderValue("Scale", 0.5, 5.0, 0.1, "x", "scale", this, float.class));
        this.addToggle(new ToggleValue("Important only", "importantOnly", this));
        this.addCheck(new CheckValue("§7Include §bGear", "gear", this));
        this.addCheck(new CheckValue("§7Include §6Resources", "resources", this));
        this.addCheck(new CheckValue("§7Include §dHeads", "heads", this));
    }

    public static boolean shouldScale(Item item) {
        ItemScale i = Module.get(ItemScale.class);
        if (item == null) {
            return false;
        } else if (!i.importantOnly) {
            return false;
        } else {
            if (i.resources) {
                if (item == Items.iron_ingot) {
                    return true;
                }

                if (item == Items.gold_ingot) {
                    return true;
                }

                if (item == Items.diamond) {
                    return true;
                }

                if (item == Items.emerald) {
                    return true;
                }

                if (item == Items.golden_apple) {
                    return true;
                }

                if (item == Items.potionitem) {
                    return true;
                }
            }

            if (i.gear) {
                if (item instanceof ItemSword) {
                    return true;
                }

                if (item instanceof ItemPickaxe) {
                    return true;
                }

                if (item instanceof ItemAxe) {
                    return true;
                }

                if (item instanceof ItemSpade) {
                    return true;
                }

                if (item instanceof ItemHoe) {
                    return true;
                }

                if (item instanceof ItemArmor) {
                    return true;
                }
            }

            return i.heads ? item == Items.skull : false;
        }
    }
}
