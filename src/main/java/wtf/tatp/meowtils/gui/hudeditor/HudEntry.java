package wtf.tatp.meowtils.gui.hudeditor;

import java.lang.reflect.Field;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.gui.Module;

public class HudEntry {
    public float scale;
    private final Module owner;
    private final String name;
    private final Supplier<int[]> bounds;
    private final Field posX;
    private final Field posY;

    public HudEntry(@Nullable String name, Module owner, String xPosConfigField, String yPosConfigField, Supplier<int[]> bounds) {
        this.name = name;
        this.owner = owner;
        this.bounds = bounds;

        try {
            this.posX = owner.getClass().getDeclaredField(xPosConfigField);
            this.posY = owner.getClass().getDeclaredField(yPosConfigField);
            this.posX.setAccessible(true);
            this.posY.setAccessible(true);
        } catch (NoSuchFieldException var7) {
            throw new RuntimeException(owner.getClass().getName() + " is missing posX/posY fields needed for HUD editor.", var7);
        }
    }

    public int getX() {
        try {
            return this.posX.getInt(this.owner);
        } catch (Exception var2) {
            var2.printStackTrace();
            return 0;
        }
    }

    public int getY() {
        try {
            return this.posY.getInt(this.owner);
        } catch (Exception var2) {
            var2.printStackTrace();
            return 0;
        }
    }

    public void setX(int x) {
        try {
            this.posX.setInt(this.owner, x);
        } catch (Exception var3) {
            Meowtils.error("Unable to setX for: " + this.owner + var3);
        }
    }

    public void setY(int y) {
        try {
            this.posY.setInt(this.owner, y);
        } catch (Exception var3) {
            Meowtils.error("Unable to setY for: " + this.owner + var3);
        }
    }

    public String getName() {
        return this.name != null ? this.name : this.owner.getName();
    }

    public int[] getBounds() {
        int[] b = this.bounds.get();
        return b != null && b.length >= 2 ? b : new int[]{1, 1};
    }

    public Module getOwner() {
        return this.owner;
    }
}
