package wtf.tatp.meowtils.manager.slots.impl;

import net.minecraft.inventory.Slot;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.manager.session.Skywars;
import wtf.tatp.meowtils.manager.slots.SlotProvider;
import wtf.tatp.meowtils.module.skywars.ItemHighlight;

public class ItemHighlightSlot implements SlotProvider {
    @Override
    public boolean shouldRender(Slot slot) {
        if (!Module.get(ItemHighlight.class).enabled) {
            return false;
        } else {
            return !Skywars.GAME.isActive() && !Skywars.MINI.isActive() ? false : slot.getStack() != null && slot.getStack().getItem() != null;
        }
    }

    @Override
    public int getColor(Slot slot) {
        ItemHighlight h = Module.get(ItemHighlight.class);
        if (slot.getStack() == null) {
            return -1;
        } else if (h.showBlacklisted && ItemHighlight.isBlacklisted(ItemHighlight.getListName(slot.getStack()))) {
            return ItemHighlight.getColor(h.blacklistColor);
        } else if (h.showBest && ItemHighlight.shouldHighlight(slot.getStack())) {
            return ItemHighlight.getColor(h.bestColor);
        } else {
            return h.showSafelisted && ItemHighlight.isSafelisted(ItemHighlight.getListName(slot.getStack())) ? ItemHighlight.getColor(h.safelistColor) : 0;
        }
    }
}
