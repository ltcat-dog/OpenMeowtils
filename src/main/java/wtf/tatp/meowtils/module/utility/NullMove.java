package wtf.tatp.meowtils.module.utility;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.gui.Module;

public class NullMove extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;
    private static long LEFT;
    private static long RIGHT;
    private static long FORWARD;
    private static long BACK;

    public NullMove() {
        super("NullMove", Module.Category.Utility);
        this.tag(Module.ModuleTag.LEGIT);
        this.tooltip("Does not prevent movement if several movement keys are held at once.");
    }

    public static boolean shouldOverride() {
        Minecraft mc = Minecraft.getMinecraft();
        return Module.get(NullMove.class).enabled && mc.currentScreen == null && mc.thePlayer != null && mc.theWorld != null;
    }

    public static boolean isMovementKey(int keyCode) {
        GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
        return keyCode == gameSettings.keyBindLeft.getKeyCode()
            || keyCode == gameSettings.keyBindRight.getKeyCode()
            || keyCode == gameSettings.keyBindForward.getKeyCode()
            || keyCode == gameSettings.keyBindBack.getKeyCode();
    }

    public static Boolean checkKey(int keyCode, boolean pressed) {
        GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
        if (!pressed) {
            return false;
        } else if (keyCode == gameSettings.keyBindLeft.getKeyCode()) {
            return RIGHT == 0L || RIGHT <= LEFT;
        } else if (keyCode == gameSettings.keyBindRight.getKeyCode()) {
            return LEFT == 0L || LEFT <= RIGHT;
        } else if (keyCode == gameSettings.keyBindForward.getKeyCode()) {
            return BACK == 0L || BACK <= FORWARD;
        } else {
            return keyCode != gameSettings.keyBindBack.getKeyCode() ? null : FORWARD == 0L || FORWARD <= BACK;
        }
    }

    public static void updateTime(int keyCode, boolean pressed) {
        GameSettings gameSettings = Minecraft.getMinecraft().gameSettings;
        long time = pressed ? System.nanoTime() : 0L;
        if (keyCode == gameSettings.keyBindLeft.getKeyCode()) {
            LEFT = time;
        } else if (keyCode == gameSettings.keyBindRight.getKeyCode()) {
            RIGHT = time;
        } else if (keyCode == gameSettings.keyBindForward.getKeyCode()) {
            FORWARD = time;
        } else if (keyCode == gameSettings.keyBindBack.getKeyCode()) {
            BACK = time;
        }
    }
}
