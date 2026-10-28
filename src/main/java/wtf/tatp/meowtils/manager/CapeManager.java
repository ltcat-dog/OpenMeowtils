package wtf.tatp.meowtils.manager;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.stream.ImageInputStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.util.ResourceLocation;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import wtf.tatp.meowtils.Meowtils;
import wtf.tatp.meowtils.gui.Module;
import wtf.tatp.meowtils.module.render.Cape;

public class CapeManager {
    private static String lastCape = null;
    private static ResourceLocation cachedCape = null;
    private static final int FRAME_DELAY = 100;
    private static ResourceLocation[] cachedFrames = null;

    public static ResourceLocation getCape(String selectedCape) {
        String capeKey = selectedCape.equals("Custom") ? "custom_" + Module.get(Cape.class).customCapeName : selectedCape;
        if (!capeKey.equals(lastCape)) {
            lastCape = capeKey;
            cachedCape = null;
            cachedFrames = null;
            cachedCape = loadCape(selectedCape);
        }

        if (cachedFrames != null && cachedFrames.length > 0) {
            int frame = (int)(System.currentTimeMillis() / 100L % cachedFrames.length);
            return cachedFrames[frame];
        } else {
            return cachedCape;
        }
    }

    private static ResourceLocation loadCape(String id) {
        try {
            if ("Custom".equalsIgnoreCase(id)) {
                String cape = Module.get(Cape.class).customCapeName.replace(".png", "").replace(".gif", "");
                File capeGif = new File(Meowtils.CUSTOM_CAPE_DIR, cape + ".gif");
                File capePng = new File(Meowtils.CUSTOM_CAPE_DIR, cape + ".png");
                File file = capeGif.exists() ? capeGif : capePng;
                if (!file.exists()) {
                    return null;
                } else if (file.getName().endsWith(".gif")) {
                    ResourceLocation var7;
                    try (InputStream inputStream = Files.newInputStream(file.toPath())) {
                        var7 = loadGif(inputStream, "custom_" + cape);
                    }

                    return var7;
                } else {
                    return registerTexture("custom_" + cape, 0, ImageIO.read(file));
                }
            } else {
                Object capePng;
                try (InputStream in = CapeManager.class.getClassLoader().getResourceAsStream("meowtils/capes/" + id + ".png")) {
                    if (in != null) {
                        return registerTexture(id, 0, ImageIO.read(in));
                    }

                    capePng = null;
                }

                return (ResourceLocation)capePng;
            }
        } catch (Exception var37) {
            Meowtils.error("Failed to load cape: " + var37);
            return null;
        }
    }

    private static ResourceLocation loadGif(InputStream inputStream, String key) throws Exception {
        ImageReader reader = ImageIO.getImageReadersByFormatName("gif").next();
        List<ResourceLocation> frames = new ArrayList<>();

        try (ImageInputStream imageInputStream = ImageIO.createImageInputStream(inputStream)) {
            reader.setInput(imageInputStream, false);
            int count = reader.getNumImages(true);
            BufferedImage image = null;

            for (int i = 0; i < count; i++) {
                BufferedImage frame = reader.read(i);
                if (frame != null) {
                    if (image == null) {
                        image = new BufferedImage(frame.getWidth(), frame.getHeight(), 2);
                    }

                    int left = 0;
                    int top = 0;
                    IIOMetadata metadata = reader.getImageMetadata(i);
                    Node root = metadata.getAsTree("javax_imageio_gif_image_1.0");

                    for (Node node = root.getFirstChild(); node != null; node = node.getNextSibling()) {
                        if ("ImageDescriptor".equals(node.getNodeName())) {
                            NamedNodeMap attributes = node.getAttributes();
                            left = Integer.parseInt(attributes.getNamedItem("imageLeftPosition").getNodeValue());
                            top = Integer.parseInt(attributes.getNamedItem("imageTopPosition").getNodeValue());
                            break;
                        }
                    }

                    Graphics2D g = image.createGraphics();
                    g.drawImage(frame, left, top, null);
                    g.dispose();
                    frames.add(registerTexture(key, i, image));
                }
            }
        } finally {
            reader.dispose();
        }

        if (frames.isEmpty()) {
            return null;
        } else {
            cachedFrames = frames.toArray(new ResourceLocation[0]);
            return cachedFrames[0];
        }
    }

    private static ResourceLocation registerTexture(String key, int frame, BufferedImage image) {
        String location = "meowtils_cape/" + key.toLowerCase().replaceAll("[^a-z0-9_]", "_") + "/" + frame;
        return Minecraft.getMinecraft().getTextureManager().getDynamicTextureLocation(location, new DynamicTexture(image));
    }
}
