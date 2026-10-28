package wtf.tatp.meowtils.module.render;

import java.awt.Color;
import java.util.Arrays;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityFireball;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.RenderWorldLastEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.OpacityValue;
import wtf.tatp.meowtils.gui.values.SliderValue;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.Render;

public class Indicators extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;
    @Config
    public float opacity = 50.0F;
    @Config
    public String mode = "3D";
    @Config
    public String render = "Full";
    @Config
    public boolean arrow = true;
    @Config
    public boolean fireball = true;
    @Config
    public boolean pearl = true;
    @Config
    public float arrowExpand = 0.0F;
    @Config
    public float fireballExpand = 0.0F;
    @Config
    public float pearlExpand = 0.0F;

    public Indicators() {
        super("Indicators", Module.Category.Render);
        this.tag(Module.ModuleTag.LEGIT);
        this.tooltip("Highlights dangerous projectiles.");
        this.addMode(new ModeValue("Mode", Arrays.asList("3D", "2D"), "mode", this));
        this.addMode(new ModeValue("Render", Arrays.asList("Full", "Outline"), "render", this));
        this.addOpacity(new OpacityValue("Opacity", "opacity", this));
        this.addCheck(new CheckValue("Arrows", "arrow", this));
        this.addSlider(new SliderValue("Arrow expand", 0.0, 1.0, 0.1, "x", "arrowExpand", this, Float.class));
        this.addCheck(new CheckValue("§cFireballs", "fireball", this));
        this.addSlider(new SliderValue("Fireball expand", 0.0, 1.0, 0.1, "x", "fireballExpand", this, Float.class));
        this.addCheck(new CheckValue("§5Ender Pearls", "pearl", this));
        this.addSlider(new SliderValue("Pearl expand", 0.0, 1.0, 0.1, "x", "pearlExpand", this, Float.class));
    }

    @EventTarget
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (this.mc.thePlayer != null && this.mc.theWorld != null) {
            boolean renderMode = this.render.equals("Full");
            boolean boxMode = this.mode.equals("3D");
            Color arrowColor = ColorUtil.getColor(255, 255, 255, ColorUtil.convertOpacity(this.opacity));
            Color fireballColor = ColorUtil.getColor(170, 0, 0, ColorUtil.convertOpacity(this.opacity));
            Color pearlColor = ColorUtil.getColor(170, 0, 170, ColorUtil.convertOpacity(this.opacity));

            for (Entity entity : this.mc.theWorld.loadedEntityList) {
                float expand = getExpandValue(entity);
                if (entity instanceof EntityArrow && this.arrow) {
                    Render.drawEntityBox(entity, boxMode, renderMode, arrowColor, !renderMode, arrowColor, expand, expand, expand);
                }

                if (entity instanceof EntityFireball && this.fireball) {
                    Render.drawEntityBox(entity, boxMode, renderMode, fireballColor, !renderMode, fireballColor, expand, expand, expand);
                }

                if (entity instanceof EntityEnderPearl && this.pearl) {
                    Render.drawEntityBox(entity, boxMode, renderMode, pearlColor, !renderMode, pearlColor, expand, expand, expand);
                }
            }
        }
    }

    private static float getExpandValue(Entity entity) {
        Indicators e = Module.get(Indicators.class);
        if (entity instanceof EntityArrow) {
            return e.arrowExpand;
        } else if (entity instanceof EntityFireball) {
            return e.fireballExpand;
        } else {
            return entity instanceof EntityEnderPearl ? e.pearlExpand : 0.0F;
        }
    }
}
