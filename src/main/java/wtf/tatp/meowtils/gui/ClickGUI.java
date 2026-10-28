package wtf.tatp.meowtils.gui;

import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.gui.component.Component;
import wtf.tatp.meowtils.gui.component.Frame;
import wtf.tatp.meowtils.gui.component.ModuleComponent;
import wtf.tatp.meowtils.gui.component.subcomponents.ExpandComponent;
import wtf.tatp.meowtils.gui.component.subcomponents.ModeComponent;
import wtf.tatp.meowtils.gui.component.subcomponents.TextComponent;
import wtf.tatp.meowtils.gui.hudeditor.HudEditor;
import wtf.tatp.meowtils.manager.TextureManager;
import wtf.tatp.meowtils.module.meowtils.GUI;
import wtf.tatp.meowtils.util.ColorUtil;

public class ClickGUI extends GuiScreen {
    private static final String VERSION_TAG = "Meowtils 2.0.1";
    private static final ResourceLocation HUD_BUTTON = TextureManager.get("textures/gui/hud_button.png");
    private static final ResourceLocation BLUR_SHADER = new ResourceLocation("minecraft", "shaders/post/blur.json");
    private static final int TOOLTIP_COLOR = new Color(14, 14, 14).getRGB();
    private static final float VERSION_TAG_SCALE = 0.65F;
    private final List<String> tooltipLines = new ArrayList<>();
    private final List<ModeComponent> expandedModes = new ArrayList<>();
    public ArrayList<Frame> frames;
    private Object lastHoveredModule = null;
    private long hoverStartTime = 0L;
    private boolean blurActive = false;

    public ClickGUI() {
        this.frames = new ArrayList<>();

        Module.Category[] values;
        for (Module.Category category : values = Module.Category.values()) {
            Frame frame = new Frame(category);
            this.frames.add(frame);
        }
    }

    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();
        GUI gui = Module.get(GUI.class);
        ScaledResolution sr = new ScaledResolution(mc);
        float scale = GuiUtil.getScale();
        int scaledMouseX = (int)(mouseX / scale);
        int scaledMouseY = (int)(mouseY / scale);
        int versionTagWidth = mc.fontRendererObj.getStringWidth("Meowtils 2.0.1");
        int versionTagHeight = mc.fontRendererObj.FONT_HEIGHT;
        float versionTagX = sr.getScaledWidth() - versionTagWidth * 0.65F - 2.0F;
        float versionTagY = sr.getScaledHeight() - versionTagHeight * 0.65F - 2.0F;
        Meowtils.fontRenderer.drawStringWithShadow("Meowtils 2.0.1", versionTagX - 4.0F, versionTagY + 4.0F, ColorUtil.rgb(gui.red, gui.green, gui.blue), 7.0F);
        int screenHeightButton = sr.getScaledHeight();
        mc.getTextureManager().bindTexture(HUD_BUTTON);
        GlStateManager.enableBlend();
        GlStateManager.enableAlpha();
        Gui.drawModalRectWithCustomSizedTexture(6, screenHeightButton - 20, 0.0F, 0.0F, 90, 20, 90.0F, 20.0F);
        GlStateManager.disableBlend();
        GlStateManager.disableAlpha();
        Meowtils.fontRenderer.drawStringWithShadow("HUD Editor", 25.0F, screenHeightButton - 10, -1, 7.0F);
        GlStateManager.pushMatrix();
        GlStateManager.scale(scale, scale, 1.0F);
        ModuleComponent hovered = null;
        this.expandedModes.clear();

        for (Frame frame : this.frames) {
            frame.updateScroll();
            frame.renderFrame();
            frame.updatePosition(scaledMouseX, scaledMouseY);

            for (Component component : frame.getComponents()) {
                component.updateComponent(scaledMouseX, scaledMouseY);
                if (component instanceof ModuleComponent) {
                    ModuleComponent part = (ModuleComponent)component;
                    if (part.isHovered()) {
                        hovered = part;
                    }

                    this.addExpandedModes(part.getSubcomponents(), this.expandedModes);
                }
            }
        }

        for (ModeComponent overlay : this.expandedModes) {
            overlay.renderExpanded(scaledMouseX, scaledMouseY);
        }

        GlStateManager.popMatrix();
        if (hovered != null && Module.get(GUI.class).tooltips) {
            Frame parentFrame = hovered.parent;
            if (!parentFrame.isOpen()) {
                return;
            }

            if (hovered.mod != this.lastHoveredModule) {
                this.lastHoveredModule = hovered.mod;
                this.hoverStartTime = System.currentTimeMillis();
            }

            if (System.currentTimeMillis() - this.hoverStartTime < 250L) {
                return;
            }

            String tooltip = hovered.mod.getTooltip();
            if (tooltip != null && !tooltip.isEmpty()) {
                int mouseXScaled = Mouse.getX() * sr.getScaledWidth() / mc.displayWidth;
                int mouseYScaled = sr.getScaledHeight() - Mouse.getY() * sr.getScaledHeight() / mc.displayHeight - 1;
                float guiTooltipscale = GuiUtil.getScale() / 2.0F;
                int drawX = (int)((mouseXScaled + 6) / guiTooltipscale);
                int drawY = (int)(mouseYScaled / guiTooltipscale);
                int screenWidth = (int)(sr.getScaledWidth() / guiTooltipscale);
                int screenHeight = (int)(sr.getScaledHeight() / guiTooltipscale);
                int availableWidth = screenWidth - drawX - 8;
                this.tooltipLines.clear();
                StringBuilder sb = new StringBuilder(64);
                GL11.glPushMatrix();
                GL11.glScalef(guiTooltipscale, guiTooltipscale, 1.0F);
                float fontScale = 10.5F;

                for (String manualLine : tooltip.split("\n")) {
                    sb.setLength(0);
                    int wordStart = 0;
                    int len = manualLine.length();

                    for (int i = 0; i <= len; i++) {
                        if (i == len || manualLine.charAt(i) == ' ') {
                            String word = manualLine.substring(wordStart, i);
                            wordStart = i + 1;
                            int oldLength = sb.length();
                            if (oldLength != 0) {
                                sb.append(' ');
                            }

                            sb.append(word);
                            if (Meowtils.fontRenderer.getStringWidth(sb.toString(), fontScale) > availableWidth) {
                                sb.setLength(oldLength);
                                this.tooltipLines.add(sb.toString());
                                sb.setLength(0);
                                sb.append(word);
                            }
                        }
                    }

                    if (sb.length() > 0) {
                        this.tooltipLines.add(sb.toString());
                    }
                }

                int lineHeight = mc.fontRendererObj.FONT_HEIGHT + 2;
                int maxWidth = 0;

                for (String line : this.tooltipLines) {
                    maxWidth = (int)Math.max((float)maxWidth, Meowtils.fontRenderer.getStringWidth(line, fontScale));
                }

                int totalHeight = this.tooltipLines.size() * lineHeight;
                if (drawX + maxWidth > screenWidth) {
                    drawX = screenWidth - maxWidth - 8;
                }

                if (drawY + totalHeight > screenHeight) {
                    drawY = screenHeight - totalHeight - 8;
                }

                if (drawX < 0) {
                    drawX = 0;
                }

                if (drawY < 0) {
                    drawY = 0;
                }

                int padding = 3;
                Gui.drawRect(drawX - padding, drawY - padding, drawX + maxWidth + padding, drawY + totalHeight - 2 + padding, TOOLTIP_COLOR);

                for (int ix = 0; ix < this.tooltipLines.size(); ix++) {
                    Meowtils.fontRenderer.drawString(this.tooltipLines.get(ix), drawX, drawY + ix * lineHeight + 7, -1, fontScale);
                }

                GL11.glPopMatrix();
            }
        }
    }

    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            int mouseX = (int)((float)(Mouse.getX() * this.width) / this.mc.displayWidth / GuiUtil.getScale());
            int mouseY = (int)(
                (this.height - (float)(Mouse.getY() * this.height) / this.mc.displayHeight - 1.0F) / GuiUtil.getScale()
            );
            int scrollSmoothness = Module.get(GUI.class).scrollSpeed;

            for (Frame frame : this.frames) {
                if (frame.isMouseOverFrame(mouseX, mouseY)) {
                    frame.scrollTarget += scroll > 0 ? scrollSmoothness : -scrollSmoothness;
                    frame.clampScroll();
                    break;
                }
            }
        }
    }

    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        float scale = GuiUtil.getScale();
        int scaledMouseX = (int)(mouseX / scale);
        int scaledMouseY = (int)(mouseY / scale);
        this.hudEditorClicked(mouseX, mouseY, mouseButton);
        List<ModeComponent> overlays = new ArrayList<>();

        for (Frame frame : this.frames) {
            for (Component component : frame.getComponents()) {
                if (component instanceof ModuleComponent) {
                    ModuleComponent part = (ModuleComponent)component;
                    this.addExpandedModes(part.getSubcomponents(), overlays);
                }
            }
        }

        for (ModeComponent overlay : overlays) {
            if (overlay.mouseClicked(scaledMouseX, scaledMouseY, mouseButton)) {
                return;
            }
        }

        if (ModeComponent.expandedPart != null) {
            boolean clickedInsideAny = false;
            ModeComponent expanded = ModeComponent.expandedPart;
            int boxX = expanded.component.parent.getX();
            int topY = expanded.component.parent.getY() + 4 + expanded.offset;
            int boxWidth = 80;
            int totalHeight = 11;
            if (expanded.modeExpanded()) {
                int extra = (expanded.option.getModes().size() - 1) * 10;
                totalHeight += extra;
            }

            if (scaledMouseX > boxX && scaledMouseX < boxX + boxWidth && scaledMouseY > topY && scaledMouseY < topY + totalHeight) {
                clickedInsideAny = true;
            }

            if (!clickedInsideAny) {
                expanded.expanded = false;
                ModeComponent.expandedPart = null;
            }
        }

        for (Frame frame : this.frames) {
            if (frame.isWithinHeader(scaledMouseX, scaledMouseY) && mouseButton == 0) {
                frame.setDrag(true);
                frame.dragX = scaledMouseX - frame.getX();
                frame.dragY = scaledMouseY - frame.getY();
            }

            if (frame.isWithinHeader(scaledMouseX, scaledMouseY) && mouseButton == 1) {
                frame.setOpen(!frame.isOpen());
            }

            if (frame.isOpen() && !frame.getComponents().isEmpty()) {
                for (Component componentx : frame.getComponents()) {
                    componentx.mouseClicked(scaledMouseX, scaledMouseY, mouseButton);
                }
            }
        }
    }

    private void hudEditorClicked(int mouseX, int mouseY, int mouseButton) {
        ScaledResolution sr = new ScaledResolution(this.mc);
        int screenHeightButton = sr.getScaledHeight();
        int buttonX = 6;
        int buttonY = screenHeightButton - 20;
        int buttonW = 72;
        int buttonH = 13;
        if (mouseButton == 0 && mouseX >= buttonX && mouseX <= buttonX + buttonW && mouseY >= buttonY && mouseY <= buttonY + buttonH) {
            this.mc.displayGuiScreen(new HudEditor());
        }
    }

    private void addExpandedModes(List<Component> components, List<ModeComponent> result) {
        for (Component c : new ArrayList<>(components)) {
            if (c instanceof ModeComponent) {
                ModeComponent mode = (ModeComponent)c;
                if (mode.modeExpanded()) {
                    result.add(mode);
                }
            } else if (c instanceof ExpandComponent) {
                ExpandComponent expand = (ExpandComponent)c;
                if (expand.option.getState()) {
                    this.addExpandedModes(new ArrayList<>(expand.getSub()), result);
                }
            }
        }
    }

    protected void keyTyped(char typedChar, int keyCode) {
        for (Frame frame : this.frames) {
            if (frame.isOpen() && (keyCode != 1 || TextComponent.isTyping()) && !frame.getComponents().isEmpty()) {
                for (Component component : frame.getComponents()) {
                    component.keyTyped(typedChar, keyCode);
                }
            }
        }

        if (keyCode == 1) {
            this.mc.displayGuiScreen(null);
        }
    }

    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (Frame frame : this.frames) {
            frame.setDrag(false);
        }
    }

    public boolean doesGuiPauseGame() {
        return false;
    }

    public void initGui() {
        super.initGui();
        Keyboard.enableRepeatEvents(true);
        this.applyBlur();
    }

    public void onGuiClosed() {
        super.onGuiClosed();
        Keyboard.enableRepeatEvents(false);
        this.clearBlur();

        for (Frame frame : this.frames) {
            frame.resetScroll();
        }
    }

    private void applyBlur() {
        if (Module.get(GUI.class).blurGui && !this.blurActive) {
            this.clearBlur();

            try {
                this.mc.entityRenderer.loadShader(BLUR_SHADER);
                this.blurActive = true;
            } catch (Exception var2) {
                var2.printStackTrace();
            }
        }
    }

    private void clearBlur() {
        if (this.blurActive && this.mc.entityRenderer.getShaderGroup() != null) {
            this.mc.entityRenderer.getShaderGroup().deleteShaderGroup();
            this.blurActive = false;
        }
    }

    public void rebuildExtensionsFrame() {
        for (Frame frame : this.frames) {
            if (frame.category == Module.Category.Extensions) {
                frame.rebuild();
                return;
            }
        }
    }

    public void rebuildFrames() {
        for (Frame frame : this.frames) {
            frame.rebuild();
        }
    }
}
