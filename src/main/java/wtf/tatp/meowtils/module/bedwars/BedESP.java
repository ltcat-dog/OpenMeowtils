package wtf.tatp.meowtils.module.bedwars;

import java.awt.Color;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.BlockBed.EnumPartType;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.chunk.Chunk;
import wtf.tatp.meowtils.config.Config;
import wtf.tatp.meowtils.event.ClientTickEvent;
import wtf.tatp.meowtils.event.RenderWorldLastEvent;
import wtf.tatp.meowtils.event.WorldEvent;
import wtf.tatp.meowtils.event.api.EventTarget;
import wtf.tatp.meowtils.gui.ColorLink;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.gui.values.BrightnessValue;
import wtf.tatp.meowtils.gui.values.ColorValue;
import wtf.tatp.meowtils.gui.values.ModeValue;
import wtf.tatp.meowtils.gui.values.OpacityValue;
import wtf.tatp.meowtils.gui.values.SaturationValue;
import wtf.tatp.meowtils.gui.values.ToggleValue;
import wtf.tatp.meowtils.manager.session.Bedwars;
import wtf.tatp.meowtils.util.ColorUtil;
import wtf.tatp.meowtils.util.Render;

public class BedESP extends Module {
    @Config
    public boolean enabled = false;
    @Config
    public int key = 0;
    @Config
    public String mode = "Full";
    @Config
    public boolean showObsidian = true;
    @Config
    public int red = 255;
    @Config
    public int green = 255;
    @Config
    public int blue = 255;
    @Config
    public float opacity = 25.0F;
    @Config
    public boolean bedwarsOnly = false;
    private static boolean scanned = false;
    public static final Set<BlockPos> BEDS = ConcurrentHashMap.newKeySet();
    public static final Set<BlockPos> OBSIDIAN = ConcurrentHashMap.newKeySet();

    public BedESP() {
        super("BedESP", Module.Category.Bedwars);
        this.tag(Module.ModuleTag.SAFE);
        this.tooltip("Highlight beds and surrounding obsidian.");
        ColorLink colorLink = new ColorLink("red", "green", "blue", this);
        this.addColor(new ColorValue("Bed color", colorLink));
        this.addSaturation(new SaturationValue(colorLink));
        this.addBrightness(new BrightnessValue(colorLink));
        this.addOpacity(new OpacityValue("Opacity", "opacity", this));
        this.addMode(new ModeValue("Mode", Arrays.asList("Full", "Outline"), "mode", this));
        this.addToggle(new ToggleValue("Show obsidian", "showObsidian", this));
        this.addToggle(new ToggleValue("Bedwars only", "bedwarsOnly", this));
    }

    @EventTarget
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        if (this.mc.thePlayer != null && this.mc.theWorld != null) {
            if (!this.bedwarsOnly || !Bedwars.GAME.isNotActive()) {
                this.renderBeds();
                if (this.showObsidian) {
                    this.renderObsidian();
                }
            }
        }
    }

    @EventTarget
    public void onClientTick(ClientTickEvent event) {
        if (this.mc.thePlayer != null && this.mc.theWorld != null && event.getPhase() == ClientTickEvent.Phase.POST) {
            if (!this.bedwarsOnly || !Bedwars.ALL.isNotActive()) {
                if (!scanned) {
                    scanned = true;
                    BEDS.clear();
                    OBSIDIAN.clear();
                    init();
                }
            }
        }
    }

    @EventTarget
    public void onWorldEvent(WorldEvent event) {
        if (event.getType() == WorldEvent.Type.UNLOAD) {
            BEDS.clear();
            OBSIDIAN.clear();
            scanned = false;
        }
    }

    public static void updateBlocks(BlockPos pos, IBlockState state) {
        Block block = state.getBlock();
        if (block instanceof BlockBed && state.getValue(BlockBed.PART) == EnumPartType.HEAD) {
            BEDS.add(pos);
            checkObsidian(pos);
        } else {
            if (BEDS.remove(pos)) {
                OBSIDIAN.removeIf(o -> o.distanceSq(pos) <= 4.0);
            }

            if (block == Blocks.obsidian || block == Blocks.air) {
                for (BlockPos bed : BEDS) {
                    if (bed.distanceSq(pos) <= 4.0) {
                        checkObsidian(bed);
                    }
                }
            }
        }
    }

    private static void checkObsidian(BlockPos headPos) {
        Minecraft mc = Minecraft.getMinecraft();
        OBSIDIAN.removeIf(pos -> pos.distanceSq(headPos) <= 4.0);
        IBlockState state = mc.theWorld.getBlockState(headPos);
        if (state.getBlock() instanceof BlockBed) {
            BlockPos foot = headPos.offset(((EnumFacing)state.getValue(BlockBed.FACING)).getOpposite());

            for (BlockPos part : new BlockPos[]{headPos, foot}) {
                for (EnumFacing facing : EnumFacing.values()) {
                    BlockPos offset = part.offset(facing);
                    if (mc.theWorld.getBlockState(offset).getBlock() == Blocks.obsidian) {
                        OBSIDIAN.add(offset);
                    }
                }
            }
        }
    }

    public static void updateChunk(Chunk chunk) {
        int minX = chunk.xPosition << 4;
        int minZ = chunk.zPosition << 4;

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = 0; y < 256; y++) {
                    BlockPos pos = new BlockPos(minX + x, y, minZ + z);
                    IBlockState state = chunk.getBlockState(pos);
                    if (state.getBlock() instanceof BlockBed && state.getValue(BlockBed.PART) == EnumPartType.HEAD) {
                        BEDS.add(pos);
                        checkObsidian(pos);
                    }
                }
            }
        }
    }

    private void renderBeds() {
        Iterator<BlockPos> iterator = BEDS.iterator();

        while (iterator.hasNext()) {
            BlockPos head = iterator.next();
            IBlockState state = this.mc.theWorld.getBlockState(head);
            if (state.getBlock() instanceof BlockBed && state.getValue(BlockBed.PART) == EnumPartType.HEAD) {
                BlockPos foot = head.offset(((EnumFacing)state.getValue(BlockBed.FACING)).getOpposite());
                IBlockState footState = this.mc.theWorld.getBlockState(foot);
                if (footState.getBlock() instanceof BlockBed) {
                    boolean renderMode = this.mode.equals("Full");
                    Color color = ColorUtil.getColor(this.red, this.green, this.blue, ColorUtil.convertOpacity(this.opacity));
                    AxisAlignedBB box = new AxisAlignedBB(
                        Math.min(head.getX(), foot.getX()),
                        head.getY(),
                        Math.min(head.getZ(), foot.getZ()),
                        Math.max(head.getX() + 1, foot.getX() + 1),
                        head.getY() + 0.5625,
                        Math.max(head.getZ() + 1, foot.getZ() + 1)
                    );
                    Render.drawBlockBox(box, null, renderMode, color, !renderMode, color, 0.0, 0.0, 0.0);
                }
            } else {
                iterator.remove();
            }
        }
    }

    private void renderObsidian() {
        Iterator<BlockPos> iterator = OBSIDIAN.iterator();

        while (iterator.hasNext()) {
            BlockPos pos = iterator.next();
            IBlockState state = this.mc.theWorld.getBlockState(pos);
            if (state.getBlock() != Blocks.obsidian) {
                iterator.remove();
            } else {
                boolean renderMode = this.mode.equals("Full");
                Color color = ColorUtil.getColor(170, 0, 170, ColorUtil.convertOpacity(this.opacity));
                AxisAlignedBB box = new AxisAlignedBB(
                    pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1
                );
                Render.drawBlockBox(box, pos, renderMode, color, !renderMode, color, 0.0, 0.0, 0.0);
            }
        }
    }

    private static void init() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc.thePlayer != null && mc.theWorld != null) {
            BlockPos player = mc.thePlayer.getPosition();

            for (BlockPos pos : BlockPos.getAllInBox(player.add(-64, -20, -64), player.add(64, 20, 64))) {
                IBlockState state = mc.theWorld.getBlockState(pos);
                if (state.getBlock() instanceof BlockBed && state.getValue(BlockBed.PART) == EnumPartType.HEAD) {
                    BEDS.add(pos);
                    checkObsidian(pos);
                }
            }
        }
    }

    @Override
    public void onEnable() {
        BEDS.clear();
        OBSIDIAN.clear();
        scanned = false;
        init();
    }

    @Override
    public void onDisable() {
        BEDS.clear();
        OBSIDIAN.clear();
        scanned = false;
    }
}
