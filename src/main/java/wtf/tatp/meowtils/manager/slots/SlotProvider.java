package wtf.tatp.meowtils.manager.slots;

import net.minecraft.inventory.Slot;

public interface SlotProvider {
    boolean shouldRender(Slot var1);

    int getColor(Slot var1);
}
