package wtf.tatp.meowtils.command.lists;

import java.util.Arrays;
import java.util.List;
import net.minecraft.command.CommandException;
import net.minecraft.util.EnumChatFormatting;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.handler.command.ClientCommand;
import wtf.tatp.meowtils.manager.lists.BlacklistManager;
import wtf.tatp.meowtils.util.MojangNameToUUID;
import wtf.tatp.meowtils.util.NameUtil;

public class BlacklistCommand extends ClientCommand {
    @Override
    public String getName() {
        return "blacklist";
    }

    @Override
    public List<String> getAliases() {
        return Arrays.asList("bl");
    }

    @Override
    public void process(String[] args) throws CommandException {
        if (args.length == 0) {
            Meowtils.addMessage(EnumChatFormatting.RED + "Usage: /blacklist <player> [reasons]");
        } else if (args[0].equalsIgnoreCase("info")) {
            if (args.length < 2) {
                Meowtils.addMessage(EnumChatFormatting.RED + "Usage: /blacklist info <player>");
            } else {
                String playerName = args[1];
                MojangNameToUUID.lookup(
                    playerName,
                    uuid -> {
                        if (BlacklistManager.isBlacklisted(uuid)) {
                            Meowtils.addMessage(
                                NameUtil.getTabDisplayName(playerName)
                                    + EnumChatFormatting.RED
                                    + " is blacklisted since: "
                                    + BlacklistManager.getFormattedEntry(uuid)
                            );
                        } else if (BlacklistManager.isBlacklisted(playerName)) {
                            Meowtils.addMessage(
                                NameUtil.getTabDisplayName(playerName)
                                    + EnumChatFormatting.RED
                                    + " is blacklisted since: "
                                    + BlacklistManager.getFormattedEntry(playerName)
                            );
                        } else {
                            Meowtils.addMessage(NameUtil.getTabDisplayName(playerName) + EnumChatFormatting.GREEN + " is not blacklisted.");
                        }
                    }
                );
            }
        } else {
            String playerName = args[0];
            String reason = args.length > 1 ? BlacklistManager.formatReasons(Arrays.copyOfRange(args, 1, args.length)) : "cheating";
            MojangNameToUUID.lookup(
                playerName,
                uuid -> {
                    if (uuid != null) {
                        BlacklistManager.appendReason(uuid, reason);
                        Meowtils.addMessage(
                            EnumChatFormatting.GREEN
                                + "Updated blacklist for "
                                + EnumChatFormatting.RESET
                                + NameUtil.getTabDisplayName(playerName)
                                + EnumChatFormatting.GREEN
                                + ": "
                                + BlacklistManager.colorReasons(reason)
                        );
                    } else {
                        BlacklistManager.appendReason(playerName, reason);
                        Meowtils.addMessage(
                            EnumChatFormatting.GREEN
                                + "Updated blacklist for "
                                + EnumChatFormatting.RESET
                                + NameUtil.getTabDisplayName(playerName)
                                + EnumChatFormatting.GREEN
                                + ": "
                                + BlacklistManager.colorReasons(reason)
                        );
                    }
                }
            );
        }
    }

    @Override
    public boolean tabCompleteNames() {
        return true;
    }
}
