package wtf.tatp.meowtils.command.shortcuts;

import net.minecraft.command.CommandException;
import net.minecraft.util.EnumChatFormatting;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.handler.command.ClientCommand;

public class GamemodeCommand extends ClientCommand {
    @Override
    public String getName() {
        return "gm";
    }

    @Override
    public void process(String[] args) throws CommandException {
        if (args.length == 0) {
            Meowtils.addMessage(EnumChatFormatting.RED + "Usage: /gm <0|1|2|3|s|c|a|spec");
        } else {
            String arg = args[0];
            if (arg.equalsIgnoreCase("0")) {
                Meowtils.sendCleanMessage("/gamemode 0");
            } else if (arg.equalsIgnoreCase("1")) {
                Meowtils.sendCleanMessage("/gamemode 1");
            } else if (arg.equalsIgnoreCase("2")) {
                Meowtils.sendCleanMessage("/gamemode 2");
            } else if (arg.equalsIgnoreCase("3")) {
                Meowtils.sendCleanMessage("/gamemode 3");
            } else {
                Meowtils.addMessage(EnumChatFormatting.RED + "That gamemode doesn't exist.");
            }
        }
    }
}
