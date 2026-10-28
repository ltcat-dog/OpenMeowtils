package wtf.tatp.meowtils.mixin;

import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.handler.command.ClientCommand;
import wtf.tatp.meowtils.handler.command.CommandHandler;
import wtf.tatp.meowtils.manager.NotificationManager;
import wtf.tatp.meowtils.module.meowtils.Notifications;
import wtf.tatp.meowtils.module.meowtils.Settings;
import wtf.tatp.meowtils.util.ChatCopyUtil;

@Mixin({GuiChat.class})
public abstract class MixinGuiChat {
    @Shadow
    private GuiTextField inputField;
    @Shadow
    private List<String> foundPlayerNames;
    @Shadow
    private boolean playerNamesFound;
    @Shadow
    private int autocompleteIndex;
    @Unique
    private String lastTyped = "";

    @Inject(
        method = {"sendAutocompleteRequest"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void meowtils$sendAutocompleteRequest(String partial, String full, CallbackInfo ci) {
        String text = this.inputField.getText();
        if (text.startsWith("/")) {
            String[] parts = text.substring(1).split(" ", 2);
            ClientCommand command = CommandHandler.getCommand(parts[0]);
            if (command != null) {
                if (!command.tabCompleteNames()) {
                    ci.cancel();
                } else {
                    NetHandlerPlayClient netHandler = Minecraft.getMinecraft().getNetHandler();
                    if (netHandler != null) {
                        String beforeCursor = this.inputField.getText().substring(0, this.inputField.getCursorPosition());
                        String[] args = beforeCursor.substring(1).split(" ", -1);
                        if (args.length >= 2) {
                            ci.cancel();
                            String typed = args[args.length - 1].toLowerCase();
                            if (!typed.equals(this.lastTyped)) {
                                this.playerNamesFound = false;
                                this.autocompleteIndex = 0;
                                this.foundPlayerNames.clear();
                            }

                            this.lastTyped = typed;

                            for (NetworkPlayerInfo info : netHandler.getPlayerInfoMap()) {
                                String name = info.getGameProfile().getName();
                                if (name.toLowerCase().startsWith(typed)) {
                                    this.foundPlayerNames.add(name);
                                }
                            }

                            if (!this.foundPlayerNames.isEmpty()) {
                                this.playerNamesFound = true;
                            }
                        }
                    }
                }
            }
        }
    }

    @Inject(
        method = {"mouseClicked"},
        at = {@At("HEAD")},
        cancellable = true
    )
    private void meowtils$mouseClicked(int mouseX, int mouseY, int mouseButton, CallbackInfo ci) {
        if (mouseButton == 1) {
            if (Module.get(Settings.class).copyChat) {
                if (Meowtils.isLunar()) {
                    Meowtils.addMessage(
                        EnumChatFormatting.RED
                            + "Copy chat is not supported on "
                            + EnumChatFormatting.AQUA
                            + "Lunar Client"
                            + EnumChatFormatting.RED
                            + ", instead use the provided chat mod."
                    );
                } else {
                    IChatComponent chatLine = ChatCopyUtil.getHoveredChatLine();
                    if (chatLine == null) {
                        return;
                    }

                    String text = EnumChatFormatting.getTextWithoutFormattingCodes(chatLine.getFormattedText());
                    if (text == null) {
                        text = chatLine.getUnformattedText();
                    }

                    if (text == null || text.trim().isEmpty()) {
                        return;
                    }

                    GuiScreen.setClipboardString(text.trim());
                    if (Notifications.getMode() != Notifications.Mode.NOTIFICATION) {
                        Meowtils.addMessage("Copied message.");
                    }

                    if (Notifications.getMode() != Notifications.Mode.CHAT) {
                        NotificationManager.show("Meowtils", "Copied chat message", NotificationManager.Type.INFO, 1500L);
                    }

                    ci.cancel();
                }
            }
        }
    }
}
