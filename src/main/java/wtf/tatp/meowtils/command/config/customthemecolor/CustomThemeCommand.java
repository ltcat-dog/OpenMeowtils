package wtf.tatp.meowtils.command.config.customthemecolor;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.event.ClickEvent;
import net.minecraft.event.HoverEvent;
import net.minecraft.event.ClickEvent.Action;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.EnumChatFormatting;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.handler.command.ClientCommand;
import wtf.tatp.meowtils.util.Prefix;

public class CustomThemeCommand extends ClientCommand {
    @Override
    public String getName() {
        return "theme";
    }

    @Override
    public void process(String[] args) throws CommandException {
        if (args.length > 0) {
            Meowtils.addMessage(EnumChatFormatting.RED + "Usage: /theme");
        } else {
            this.openColorSelector("m");
            this.openColorSelector("e");
            this.openColorSelector("o");
            this.openColorSelector("w");
            Meowtils.addMessage("Bracket color:");
            this.openColorSelector("[");
            this.openColorSelector("]");
        }
    }

    private void openColorSelector(String targetLetter) {
        Minecraft mc = Minecraft.getMinecraft();
        String baseText = targetLetter.toUpperCase() + ": ";
        ChatComponentText base = new ChatComponentText(Prefix.getPrefix() + EnumChatFormatting.WHITE + baseText);
        EnumChatFormatting[] sortedColors = new EnumChatFormatting[]{
            EnumChatFormatting.BLACK,
            EnumChatFormatting.DARK_GRAY,
            EnumChatFormatting.GRAY,
            EnumChatFormatting.WHITE,
            EnumChatFormatting.DARK_RED,
            EnumChatFormatting.RED,
            EnumChatFormatting.GOLD,
            EnumChatFormatting.YELLOW,
            EnumChatFormatting.DARK_GREEN,
            EnumChatFormatting.GREEN,
            EnumChatFormatting.DARK_AQUA,
            EnumChatFormatting.AQUA,
            EnumChatFormatting.DARK_BLUE,
            EnumChatFormatting.BLUE,
            EnumChatFormatting.DARK_PURPLE,
            EnumChatFormatting.LIGHT_PURPLE
        };

        for (EnumChatFormatting color : sortedColors) {
            if (color.isColor()) {
                ChatComponentText colorComponent = new ChatComponentText(color + "⬛");
                colorComponent.getChatStyle().setChatClickEvent(new ClickEvent(Action.RUN_COMMAND, "/settheme " + targetLetter + " " + color.name()));
                colorComponent.getChatStyle()
                    .setChatHoverEvent(
                        new HoverEvent(
                            net.minecraft.event.HoverEvent.Action.SHOW_TEXT,
                            new ChatComponentText("Click to set " + targetLetter.toUpperCase() + " to " + color + color.name())
                        )
                    );
                base.appendSibling(colorComponent);
            }
        }

        mc.thePlayer.addChatMessage(base);
    }
}
