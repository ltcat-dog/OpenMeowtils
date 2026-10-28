package wtf.tatp.meowtils.command.shortcuts;

import net.minecraft.command.CommandException;
import net.minecraft.util.EnumChatFormatting;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.handler.command.ClientCommand;

public class BlockAddCommand extends ClientCommand {
    @Override
    public String getName() {
        return "block";
    }

    @Override
    public void process(String[] args) throws CommandException {
        if (args.length < 1) {
            Meowtils.addMessage(EnumChatFormatting.RED + "Usage: /block <player>");
        } else {
            String arg = args[0];
            if (arg.equalsIgnoreCase("list")) {
                Meowtils.sendCleanMessage("/block list");
            } else if (arg.equalsIgnoreCase("help")) {
                Meowtils.sendCleanMessage("/block help");
            } else if (arg.equalsIgnoreCase("removeall")) {
                if (args.length < 2) {
                    Meowtils.sendCleanMessage("/block removeall");
                } else if (args[1].equalsIgnoreCase("cancel")) {
                    Meowtils.sendCleanMessage("/block removeall cancel");
                } else if (args[1].equalsIgnoreCase("yesiamabouttodeleteallmyblocks")) {
                    Meowtils.sendCleanMessage("/block removeall yesiamabouttodeleteallmyblocks");
                }
            } else if (!arg.equalsIgnoreCase("add") && !arg.equalsIgnoreCase("remove")) {
                Meowtils.sendCleanMessage("/block add " + arg);
            } else if (args.length < 2) {
                Meowtils.addMessage(EnumChatFormatting.RED + "Usage: /block " + arg + " <player>");
            } else {
                Meowtils.sendCleanMessage("/block " + arg.toLowerCase() + " " + args[1]);
            }
        }
    }

    @Override
    public boolean tabCompleteNames() {
        return true;
    }
}
