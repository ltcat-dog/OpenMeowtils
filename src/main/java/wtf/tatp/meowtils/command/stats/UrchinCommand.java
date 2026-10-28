package wtf.tatp.meowtils.command.stats;

import java.util.Arrays;
import java.util.List;
import net.minecraft.command.CommandException;
import net.minecraft.util.EnumChatFormatting;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.handler.command.ClientCommand;
import wtf.tatp.meowtils.module.hypixel.Stats;

public class UrchinCommand extends ClientCommand {
    @Override
    public String getName() {
        return "urchin";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("urc", "ur");
    }

    @Override
    public void process(String[] args) throws CommandException {
        if (args.length == 0) {
            Meowtils.addMessage(EnumChatFormatting.RED + "Usage: /urchin <player>");
        } else if (Module.get(Stats.class).urchinApiKey.isEmpty()) {
            Meowtils.addMessage(EnumChatFormatting.RED + "No Urchin API key is set!");
        } else {
            Stats.checkUrchin(args[0], true);
        }
    }

    @Override
    public boolean tabCompleteNames() {
        return true;
    }
}
