package wtf.tatp.meowtils.handler.command;

import java.util.Collections;
import java.util.List;
import net.minecraft.command.CommandException;

public abstract class ClientCommand {
    public abstract String getName();

    public abstract void process(String[] var1) throws CommandException;

    public List<String> getAliases() {
        return Collections.emptyList();
    }

    public boolean tabCompleteNames() {
        return false;
    }
}
