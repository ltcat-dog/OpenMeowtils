package wtf.tatp.meowtils.util;

import java.awt.Color;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockSlab;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import org.lwjgl.opengl.GL11;
import wtf.tatp.meowtils.mixin.AccessorMinecraft;

public class Render {
    public static void drawSlotBackground(int x, int y, int color) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableLighting();
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(x, y + 16, 0.0).color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >> 24 & 0xFF).endVertex();
        worldrenderer.pos(x + 16, y + 16, 0.0).color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >> 24 & 0xFF).endVertex();
        worldrenderer.pos(x + 16, y, 0.0).color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >> 24 & 0xFF).endVertex();
        worldrenderer.pos(x, y, 0.0).color(color >> 16 & 0xFF, color >> 8 & 0xFF, color & 0xFF, color >> 24 & 0xFF).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
    }

    public static void drawBlockBox(
        AxisAlignedBB box, BlockPos pos, boolean fill, Color fillColor, boolean outline, Color outlineColor, double expandX, double expandY, double expandZ
    ) {
        Minecraft mc = Minecraft.getMinecraft();
        if (pos != null) {
            Block block = mc.theWorld.getBlockState(pos).getBlock();
            if (block instanceof BlockChest) {
                box = box.expand(-0.05, -0.05, -0.05);
            }

            if (block instanceof BlockBed) {
                box = box.expand(0.0, -0.4, 0.0);
            }

            if (block instanceof BlockSlab) {
                box = box.expand(0.0, -0.25, 0.0);
            }
        }

        double x = mc.getRenderManager().viewerPosX;
        double y = mc.getRenderManager().viewerPosY;
        double z = mc.getRenderManager().viewerPosZ;
        AxisAlignedBB bb = box.expand(expandX, expandY, expandZ).offset(-x, -y, -z);
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        if (fill) {
            float r = fillColor.getRed() / 255.0F;
            float g = fillColor.getGreen() / 255.0F;
            float b = fillColor.getBlue() / 255.0F;
            float a = fillColor.getAlpha() / 255.0F;
            Tessellator tess = Tessellator.getInstance();
            WorldRenderer wr = tess.getWorldRenderer();
            GlStateManager.color(r, g, b, a);
            wr.begin(7, DefaultVertexFormats.POSITION);
            wr.pos(bb.minX, bb.minY, bb.minZ).endVertex();
            wr.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
            wr.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
            wr.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
            wr.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
            wr.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
            wr.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
            wr.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
            wr.pos(bb.minX, bb.minY, bb.minZ).endVertex();
            wr.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
            wr.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
            wr.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
            wr.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
            wr.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
            wr.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
            wr.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
            wr.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
            wr.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
            wr.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
            wr.pos(bb.minX, bb.minY, bb.minZ).endVertex();
            wr.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
            wr.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
            wr.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
            wr.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
            tess.draw();
        }

        if (outline) {
            float r = outlineColor.getRed() / 255.0F;
            float g = outlineColor.getGreen() / 255.0F;
            float b = outlineColor.getBlue() / 255.0F;
            float a = outlineColor.getAlpha() / 255.0F;
            GlStateManager.color(r, g, b, a);
            RenderGlobal.drawSelectionBoundingBox(bb);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    public static void drawEntityBox(
        Entity entity, boolean is3D, boolean fill, Color fillColor, boolean outline, Color outlineColor, double expandX, double expandY, double expandZ
    ) {
        Minecraft mc = Minecraft.getMinecraft();
        float partialTicks = ((AccessorMinecraft)mc).getTimer().renderPartialTicks;
        double dx = mc.getRenderManager().viewerPosX;
        double dy = mc.getRenderManager().viewerPosY;
        double dz = mc.getRenderManager().viewerPosZ;
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks;
        AxisAlignedBB bb = entity.getEntityBoundingBox()
            .offset(-entity.posX, -entity.posY, -entity.posZ)
            .offset(x - dx, y - dy, z - dz)
            .expand(expandX, expandY, expandZ);
        float fillRed = fillColor.getRed() / 255.0F;
        float fillGreen = fillColor.getGreen() / 255.0F;
        float fillBlue = fillColor.getBlue() / 255.0F;
        float fillAlpha = fillColor.getAlpha() / 255.0F;
        float outlineRed = outlineColor.getRed() / 255.0F;
        float outlineGreen = outlineColor.getGreen() / 255.0F;
        float outlineBlue = outlineColor.getBlue() / 255.0F;
        float outlineAlpha = outlineColor.getAlpha() / 255.0F;
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        if (is3D) {
            Tessellator tess = Tessellator.getInstance();
            WorldRenderer wr = tess.getWorldRenderer();
            if (fill) {
                GlStateManager.color(fillRed, fillGreen, fillBlue, fillAlpha);
                wr.begin(7, DefaultVertexFormats.POSITION);
                wr.pos(bb.minX, bb.minY, bb.minZ).endVertex();
                wr.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
                wr.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
                wr.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
                wr.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
                wr.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
                wr.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
                wr.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
                wr.pos(bb.minX, bb.minY, bb.minZ).endVertex();
                wr.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
                wr.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
                wr.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
                wr.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
                wr.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
                wr.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
                wr.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
                wr.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
                wr.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
                wr.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
                wr.pos(bb.minX, bb.minY, bb.minZ).endVertex();
                wr.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
                wr.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
                wr.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
                wr.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
                tess.draw();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }

            if (outline) {
                GlStateManager.color(outlineRed, outlineGreen, outlineBlue, outlineAlpha);
                GL11.glLineWidth(1.5F);
                wr.begin(1, DefaultVertexFormats.POSITION);
                wr.pos(bb.minX, bb.minY, bb.minZ).endVertex();
                wr.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
                wr.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
                wr.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
                wr.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
                wr.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
                wr.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
                wr.pos(bb.minX, bb.minY, bb.minZ).endVertex();
                wr.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
                wr.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
                wr.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
                wr.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
                wr.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
                wr.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
                wr.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
                wr.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
                wr.pos(bb.minX, bb.minY, bb.minZ).endVertex();
                wr.pos(bb.minX, bb.maxY, bb.minZ).endVertex();
                wr.pos(bb.maxX, bb.minY, bb.minZ).endVertex();
                wr.pos(bb.maxX, bb.maxY, bb.minZ).endVertex();
                wr.pos(bb.maxX, bb.minY, bb.maxZ).endVertex();
                wr.pos(bb.maxX, bb.maxY, bb.maxZ).endVertex();
                wr.pos(bb.minX, bb.minY, bb.maxZ).endVertex();
                wr.pos(bb.minX, bb.maxY, bb.maxZ).endVertex();
                tess.draw();
                GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            }
        } else {
            float width = (float)((bb.maxX - bb.minX) / 2.0);
            float height = (float)((bb.maxY - bb.minY) / 2.0);
            GlStateManager.translate(x - dx, y - dy + height, z - dz);
            GlStateManager.rotate(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
            GlStateManager.rotate(mc.getRenderManager().playerViewX, 1.0F, 0.0F, 0.0F);
            if (fill) {
                GL11.glColor4f(fillRed, fillGreen, fillBlue, fillAlpha * 0.5F);
                GL11.glBegin(7);
                GL11.glVertex3f(-width, height, 0.0F);
                GL11.glVertex3f(width, height, 0.0F);
                GL11.glVertex3f(width, -height, 0.0F);
                GL11.glVertex3f(-width, -height, 0.0F);
                GL11.glEnd();
            }

            GL11.glColor4f(outlineRed, outlineGreen, outlineBlue, 1.0F);
            GL11.glLineWidth(1.5F);
            GL11.glBegin(2);
            GL11.glVertex3f(-width, height, 0.0F);
            GL11.glVertex3f(width, height, 0.0F);
            GL11.glVertex3f(width, -height, 0.0F);
            GL11.glVertex3f(-width, -height, 0.0F);
            GL11.glEnd();
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        }

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    public static void drawRectFloat(float x1, float y1, float x2, float y2, int color) {
        float a = (color >> 24 & 0xFF) / 255.0F;
        float r = (color >> 16 & 0xFF) / 255.0F;
        float g = (color >> 8 & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        GlStateManager.color(r, g, b, a);
        wr.begin(7, DefaultVertexFormats.POSITION);
        wr.pos(x1, y2, 0.0).endVertex();
        wr.pos(x2, y2, 0.0).endVertex();
        wr.pos(x2, y1, 0.0).endVertex();
        wr.pos(x1, y1, 0.0).endVertex();
        tessellator.draw();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
    }

    public static void renderItemIcon(ItemStack stack, float x, float y, float scale) {
        Minecraft mc = Minecraft.getMinecraft();
        if (stack != null) {
            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y, 0.0F);
            GlStateManager.scale(scale, scale, scale);
            GlStateManager.enableRescaleNormal();
            RenderHelper.enableGUIStandardItemLighting();
            mc.getRenderItem().renderItemAndEffectIntoGUI(stack, 0, 0);
            mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, stack, 0, 0, null);
            RenderHelper.disableStandardItemLighting();
            GlStateManager.disableRescaleNormal();
            GlStateManager.popMatrix();
        }
    }
}
