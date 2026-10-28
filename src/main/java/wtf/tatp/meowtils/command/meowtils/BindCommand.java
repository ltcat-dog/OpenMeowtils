package wtf.tatp.meowtils.command.meowtils;

import net.minecraft.command.CommandException;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.input.Keyboard;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.ConfigManager;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.handler.command.ClientCommand;
import wtf.tatp.meowtils.module.meowtils.GUI;

public class BindCommand extends ClientCommand {
    @Override
    public String getName() {
        return "bind";
    }

    @Override
    public void process(String[] args) throws CommandException {
        if (args.length == 0) {
            Meowtils.addMessage(EnumChatFormatting.RED + "Usage: /bind <key>");
        } else if (args[0].length() >= 2) {
            Meowtils.addMessage(EnumChatFormatting.RED + "Invalid key, to set other keys use the GUI.");
        } else {
            String key = args[0].toUpperCase().replace(" ", "");
            int keyCode = Keyboard.getKeyIndex(key);
            if (keyCode == 0) {
                Meowtils.addMessage(EnumChatFormatting.RED + "Unknown key: " + args[0]);
            } else {
                try {
                    Module.get(GUI.class).key = keyCode;
                    ConfigManager.save();
                    Meowtils.addMessage("Set key to: " + EnumChatFormatting.YELLOW + Keyboard.getKeyName(keyCode));
                } catch (Exception var5) {
                    var5.printStackTrace();
                    Meowtils.addMessage(EnumChatFormatting.RED + "Error while attempting to set bind.");
                }
            }
        }
    }
}
