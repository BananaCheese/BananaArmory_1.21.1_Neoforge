package net.bananacheese.bananasarmory.client.renderer;

import com.mojang.blaze3d.platform.NativeImage;
import net.bananacheese.bananasarmory.BananasArmory;
import net.bananacheese.bananasarmory.item.custom.ArmorFrameItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ported from DynamicTextureManager. Renames: MinecraftClient -> Minecraft,
 * NativeImageBackedTexture -> DynamicTexture, Identifier -> ResourceLocation,
 * client.getResourceManager().getResource(id).orElseThrow().getInputStream()
 * -> resourceManager.getResourceOrThrow(id).open(),
 * client.getTextureManager().registerTexture(id, tex) -> textureManager.register(id, tex).
 *
 * The NativeImage / texture-compositing logic itself is unchanged — that
 * API (com.mojang.blaze3d.platform.NativeImage) has been stable across many
 * versions including 1.21.1.
 *
 * Two spots flagged inline below are the ones most likely to need a small
 * fix once you can compile against real 1.21.1 sources: the DynamicTexture
 * constructor signature, and DynamicTexture's pixel-access method name.
 */
@OnlyIn(Dist.CLIENT)
public class DynamicTextureManager {
    private static final Map<String, ResourceLocation> TEXTURE_CACHE = new HashMap<>();
    private static final Map<String, DynamicTexture> TEXTURE_OBJECTS = new HashMap<>();

    /**
     * Gets or generates a composite texture for an armor frame.
     */
    public static ResourceLocation getOrCreateArmorTexture(ItemStack stack) {
        if (!(stack.getItem() instanceof ArmorFrameItem frameItem)) {
            return getFallbackTexture(stack);
        }

        String cacheKey = buildCacheKey(stack);

        if (TEXTURE_CACHE.containsKey(cacheKey)) {
            return TEXTURE_CACHE.get(cacheKey);
        }

        ResourceLocation compositeId = generateCompositeTexture(stack, frameItem, cacheKey);
        TEXTURE_CACHE.put(cacheKey, compositeId);

        return compositeId;
    }

    private static String buildCacheKey(ItemStack stack) {
        ArmorFrameItem frameItem = (ArmorFrameItem) stack.getItem();
        StringBuilder key = new StringBuilder(frameItem.getFrameType().name().toLowerCase());

        List<ArmorFrameItem.ComponentData> components = ArmorFrameItem.getComponents(stack);

        components.stream()
                .sorted((a, b) -> a.id().compareTo(b.id()))
                .forEach(comp -> {
                    String itemName = comp.id().substring(comp.id().lastIndexOf(':') + 1);
                    key.append("_").append(itemName);
                });

        return key.toString();
    }

    private static ResourceLocation generateCompositeTexture(ItemStack stack, ArmorFrameItem frameItem, String cacheKey) {
        Minecraft client = Minecraft.getInstance();

        try {
            String frameName = frameItem.getFrameType().name().toLowerCase();
            ResourceLocation baseTextureId = ResourceLocation.fromNamespaceAndPath(BananasArmory.MODID,
                    "textures/item/armor/frames/" + frameName + "_frame.png");

            NativeImage baseImage = loadTexture(baseTextureId);
            if (baseImage == null) {
                BananasArmory.LOGGER.warn("Failed to load base texture: " + baseTextureId);
                return getFallbackTexture(stack);
            }

            List<ArmorFrameItem.ComponentData> components = ArmorFrameItem.getComponents(stack);
            for (ArmorFrameItem.ComponentData comp : components) {
                String componentTexturePath = getComponentTexturePath(comp.id());
                ResourceLocation compTextureId = ResourceLocation.fromNamespaceAndPath(BananasArmory.MODID,
                        "textures/item/armor/components/item/" + componentTexturePath + ".png");

                NativeImage compImage = loadTexture(compTextureId);
                if (compImage != null) {
                    overlayImage(baseImage, compImage);
                    compImage.close();
                } else {
                    BananasArmory.LOGGER.warn("Failed to load component texture: " + compTextureId);
                }
            }

            ResourceLocation compositeId = ResourceLocation.fromNamespaceAndPath(BananasArmory.MODID,
                    "dynamic/armor_frames/" + cacheKey);

            int width = baseImage.getWidth();
            int height = baseImage.getHeight();

            // NOTE: verify this constructor against 1.21.1 sources — the
            // (name, width, height, useMipmaps) shape matches the original,
            // but DynamicTexture's constructor overloads have shifted
            // between versions (some take a NativeImage directly instead).
            DynamicTexture texture = new DynamicTexture(width, height, false);

            // NOTE: pixel-access method name — original called getImage(),
            // mojmap 1.21.1 may call this getPixels() instead. Verify.
            NativeImage textureImage = texture.getPixels();
            if (textureImage != null) {
                for (int x = 0; x < width; x++) {
                    for (int y = 0; y < height; y++) {
                        textureImage.setPixelRGBA(x, y, baseImage.getPixelRGBA(x, y));
                    }
                }
            }

            texture.upload();

            client.getTextureManager().register(compositeId, texture);
            TEXTURE_OBJECTS.put(cacheKey, texture);

            baseImage.close();

            BananasArmory.LOGGER.info("Generated dynamic texture: " + compositeId);
            return compositeId;

        } catch (Exception e) {
            BananasArmory.LOGGER.error("Failed to generate composite texture for " + cacheKey, e);
            return getFallbackTexture(stack);
        }
    }

    private static NativeImage loadTexture(ResourceLocation id) {
        Minecraft client = Minecraft.getInstance();
        try (InputStream stream = client.getResourceManager().getResourceOrThrow(id).open()) {
            return NativeImage.read(stream);
        } catch (Exception e) {
            BananasArmory.LOGGER.debug("Could not load texture: " + id);
            return null;
        }
    }

    private static void overlayImage(NativeImage base, NativeImage overlay) {
        int width = Math.min(base.getWidth(), overlay.getWidth());
        int height = Math.min(base.getHeight(), overlay.getHeight());

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int overlayPixel = overlay.getPixelRGBA(x, y);
                int overlayAlpha = (overlayPixel >> 24) & 0xFF;

                if (overlayAlpha > 0) {
                    int basePixel = base.getPixelRGBA(x, y);
                    int blended = blendPixels(basePixel, overlayPixel, overlayAlpha);
                    base.setPixelRGBA(x, y, blended);
                }
            }
        }
    }

    private static int blendPixels(int base, int overlay, int overlayAlpha) {
        if (overlayAlpha == 255) {
            return overlay;
        }

        float alpha = overlayAlpha / 255f;

        int baseA = (base >> 24) & 0xFF;
        int baseR = (base >> 16) & 0xFF;
        int baseG = (base >> 8) & 0xFF;
        int baseB = base & 0xFF;

        int overlayR = (overlay >> 16) & 0xFF;
        int overlayG = (overlay >> 8) & 0xFF;
        int overlayB = overlay & 0xFF;

        int r = (int) (overlayR * alpha + baseR * (1 - alpha));
        int g = (int) (overlayG * alpha + baseG * (1 - alpha));
        int b = (int) (overlayB * alpha + baseB * (1 - alpha));
        int a = Math.max(baseA, overlayAlpha);

        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static String getComponentTexturePath(String componentId) {
        return componentId.substring(componentId.lastIndexOf(':') + 1);
    }

    private static ResourceLocation getFallbackTexture(ItemStack stack) {
        if (stack.getItem() instanceof ArmorFrameItem frameItem) {
            String frameName = frameItem.getFrameType().name().toLowerCase();
            return ResourceLocation.fromNamespaceAndPath(BananasArmory.MODID, "item/" + frameName + "_frame");
        }
        return ResourceLocation.withDefaultNamespace("item/barrier");
    }

    /**
     * Clears the texture cache (call when reloading resources).
     */
    public static void clearCache() {
        for (DynamicTexture texture : TEXTURE_OBJECTS.values()) {
            texture.close();
        }
        TEXTURE_OBJECTS.clear();
        TEXTURE_CACHE.clear();
        BananasArmory.LOGGER.info("Cleared dynamic texture cache");
    }
}
