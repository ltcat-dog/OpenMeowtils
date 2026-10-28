package wtf.tatp.meowtils.command.config.flagmessagecolor;

import net.minecraft.command.CommandException;
import net.minecraft.util.EnumChatFormatting;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.config.ConfigManager;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.handler.command.ClientCommand;
import wtf.tatp.meowtils.module.antisnipe.AntiCheat;

public class SetFlagMessageColorCommand extends ClientCommand {
    @Override
    public String getName() {
        return "setflagmessagecolor";
    }

    @Override
    public void process(String[] args) throws CommandException {
        if (args.length != 2) {
            Meowtils.addMessage(EnumChatFormatting.RED + "Usage: /setflagmessagecolor");
        } else {
            String letter = args[0].toLowerCase();
            String colorName = args[1].toUpperCase();

            try {
                EnumChatFormatting.valueOf(colorName);
                AntiCheat s = Module.get(AntiCheat.class);
                switch (letter) {
                    case "reason":
                        s.componentColor = colorName;
                        break;
                    case "wdr":
                        s.buttonColor = colorName;
                        break;
                    case "bracket":
                        s.bracketColor = colorName;
                        break;
                    default:
                        Meowtils.addMessage(EnumChatFormatting.RED + "Invalid letter.");
                        return;
                }

                ConfigManager.save();
                Meowtils.addMessage(EnumChatFormatting.GREEN + "Set color for '" + letter.toUpperCase() + "' to " + colorName);
            } catch (IllegalArgumentException var7) {
                Meowtils.addMessage(EnumChatFormatting.RED + "Invalid color name.");
            }
        }
    }
}
