package wtf.tatp.meowtils.module.advanced;

import java.util.Arrays;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockChest;
import net.minecraft.block.BlockColored;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockLog;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockPackedIce;
import net.minecraft.block.BlockPlanks;
import net.minecraft.block.BlockStone;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemFlintAndSteel;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import org.lwjgl.opengl.GL11;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.RenderPlayerEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.CheckValue;
import wtf.tatp.meowtils.gui.values.ExpandValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.util.TeamUtil;

public class GhostHand extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;
    @Config
    public boolean interactTeammates = true;
    @Config
    public boolean interactEnemies = false;
    @Config
    public boolean interactArmorstands = false;
    @Config
    public boolean useItemWhitelist = true;
    @Config
    public boolean swords = false;
    @Config
    public boolean tools = true;
    @Config
    public boolean hand = true;
    @Config
    public boolean blocks = false;
    @Config
    public boolean buckets = false;
    @Config
    public boolean flintAndSteel = false;
    @Config
    public boolean useBlockWhitelist = false;
    @Config
    public boolean beds = true;
    @Config
    public boolean obsidian = true;
    @Config
    public boolean defenseBlocks = true;
    @Config
    public boolean chests = true;
    @Config
    public boolean bedwarsOnly = false;
    @Config
    public String transparent = "None";

    public GhostHand() {
        super("GhostHand", Module.Category.Advanced);
        this.tag(Module.ModuleTag.BLATANT);
        this.tooltip("Allows you to interact through entities.");
        this.addMode(new ModeValue("Transparent", Arrays.asList("Always", "None"), "transparent", this));
        this.addToggle(new ToggleValue("Use item whitelist", "useItemWhitelist", this));
        this.addToggle(new ToggleValue("Use block whitelist", "useBlockWhitelist", this));
        this.addToggle(new ToggleValue("Bedwars only", "bedwarsOnly", this));
        this.addCheck(new CheckValue("Through teammates", "interactTeammates", this));
        this.addCheck(new CheckValue("Through enemies", "interactEnemies", this));
        this.addCheck(new CheckValue("Through armorstands", "interactArmorstands", this));
        this.addExpand(new ExpandValue("Item whitelist", e -> {
            e.addCheck(new CheckValue("§bSwords", "swords", this));
            e.addCheck(new CheckValue("§6Tools", "tools", this));
            e.addCheck(new CheckValue("§eHand", "hand", this));
            e.addCheck(new CheckValue("Blocks", "blocks", this));
            e.addCheck(new CheckValue("§9Buckets", "buckets", this));
            e.addCheck(new CheckValue("§7Flint and steel", "flintAndSteel", this));
        }));
        this.addExpand(new ExpandValue("Block whitelist", e -> {
            e.addCheck(new CheckValue("§cBeds", "beds", this));
            e.addCheck(new CheckValue("§8Obsidian", "obsidian", this));
            e.addCheck(new CheckValue("Defense blocks", "defenseBlocks", this));
            e.addCheck(new CheckValue("§6Chests", "chests", this));
        }));
    }

    @EventTarget
    public void onRenderPlayer(RenderPlayerEvent event) {
        if (this.mc.thePlayer != null && this.mc.theWorld != null) {
            EntityPlayer player = event.getPlayer();
            if (!this.transparent.equals("None")) {
                if (!player.isInvisible()) {
                    if (player != this.mc.thePlayer) {
                        if (isWhitelistedEntity(player)) {
                            if (event.getStage() == RenderPlayerEvent.Stage.PRE) {
                                GL11.glEnable(3042);
                                GL11.glBlendFunc(770, 771);
                                GL11.glColor4f(1.0F, 1.0F, 1.0F, 0.5F);
                            }

                            if (event.getStage() == RenderPlayerEvent.Stage.POST) {
                                GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
                                GL11.glDisable(3042);
                            }
                        }
                    }
                }
            }
        }
    }

    public boolean shouldActivate(Entity entity) {
        return this.bedwarsOnly && Bedwars.GAME.isNotActive() ? false : isWhitelistedEntity(entity) && isWhitelistedItem() && isWhitelistedBlock();
    }

    private static boolean isWhitelistedEntity(Entity entity) {
        GhostHand g = Module.get(GhostHand.class);
        if (g.interactTeammates && entity instanceof EntityPlayer && TeamUtil.isTeam((EntityPlayer)entity) && !TeamUtil.isBot((EntityPlayer)entity)) {
            return true;
        } else {
            return g.interactEnemies && entity instanceof EntityPlayer && !TeamUtil.isBot((EntityPlayer)entity)
                ? true
                : g.interactArmorstands && entity instanceof EntityArmorStand;
        }
    }

    private static boolean isWhitelistedItem() {
        Minecraft mc = Minecraft.getMinecraft();
        GhostHand g = Module.get(GhostHand.class);
        if (!g.useItemWhitelist) {
            return true;
        } else {
            ItemStack itemStack = mc.thePlayer.getHeldItem();
            if (itemStack == null) {
                return g.hand;
            } else {
                Item item = mc.thePlayer.getHeldItem().getItem();
                if (item instanceof ItemSword && g.swords) {
                    return true;
                } else if ((item instanceof ItemTool || item instanceof ItemShears) && g.tools) {
                    return true;
                } else if (item instanceof ItemBlock && g.blocks) {
                    return true;
                } else {
                    return (item == Items.bucket || item == Items.lava_bucket || item == Items.water_bucket) && g.buckets
                        ? true
                        : item instanceof ItemFlintAndSteel && g.flintAndSteel;
                }
            }
        }
    }

    private static boolean isWhitelistedBlock() {
        Minecraft mc = Minecraft.getMinecraft();
        GhostHand g = Module.get(GhostHand.class);
        if (!g.useBlockWhitelist) {
            return true;
        } else if (mc.objectMouseOver == null) {
            return false;
        } else {
            if (mc.objectMouseOver.typeOfHit == MovingObjectType.BLOCK) {
                Block block = mc.theWorld.getBlockState(mc.objectMouseOver.getBlockPos()).getBlock();
                if (block instanceof BlockBed && g.beds) {
                    return true;
                }

                if (block instanceof BlockObsidian && g.obsidian) {
                    return true;
                }

                if ((
                        block instanceof BlockColored
                            || block instanceof BlockLog
                            || block instanceof BlockPlanks
                            || block instanceof BlockStone
                            || block instanceof BlockPackedIce
                    )
                    && g.defenseBlocks) {
                    return true;
                }

                if ((block instanceof BlockChest || block instanceof BlockEnderChest) && g.chests) {
                    return true;
                }
            }

            return false;
        }
    }
}
