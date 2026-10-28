package wtf.tatp.meowtils.manager.slots.impl;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.manager.session.Duels;
import wtf.tatp.meowtils.manager.slots.SlotProvider;
import wtf.tatp.meowtils.module.bedwars.ShopHelper;

public class ShopHelperSlot implements SlotProvider {
    @Override
    public boolean shouldRender(Slot slot) {
        ShopHelper q = Module.get(ShopHelper.class);
        return q.enabled && q.highlightAffordable && (Bedwars.GAME.isActive() || Duels.BEDWARS.isActive());
    }

    @Override
    public int getColor(Slot slot) {
        if (slot != null && slot.getHasStack()) {
            ItemStack stack = slot.getStack();
            ShopHelper.ItemCost cost = ShopHelper.getCostFromLore(stack);
            if (cost == null) {
                return 0;
            } else {
                return !ShopHelper.shouldHighlight(stack, cost) ? 0 : ShopHelper.getColor(cost.resourceType);
            }
        } else {
            return 0;
        }
    }
}
