package me.hydos.rosella.render.texture;

import it.unimi.dsi.fastutil.ints.IntArrayPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntPriorityQueues;
import me.hydos.rosella.memory.Memory;
import me.hydos.rosella.render.VkKt;
import me.hydos.rosella.render.renderer.Renderer;
import me.hydos.rosella.vkobjects.VkCommon;
import org.lwjgl.vulkan.VK10;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TextureManager {

    public static final int BLANK_TEXTURE_ID = 0;
    public static Texture BLANK_TEXTURE;

    private final VkCommon common;

    private final Map<Integer, Texture> textureMap = new HashMap<>();
    private final Map<SamplerCreateInfo, Map<Integer, TextureSampler>> samplerCache = new HashMap<>();
    private final Set<Texture> preparedTextures = new HashSet<>();
    private final IntPriorityQueue reusableTexIds = IntPriorityQueues.synchronize(new IntArrayPriorityQueue());

    private int nextTexId = 1; // we start at 1 because 0 is reserved for the blank texture

    public TextureManager(VkCommon common) {
        this.common = common;
    }

    public final VkCommon getCommon() {
        return this.common;
    }

    public void initializeBlankTexture(Renderer renderer) {
        if (BLANK_TEXTURE == null) {
            createTexture(renderer, BLANK_TEXTURE_ID, 1, 1, VK10.VK_FORMAT_R8G8B8A8_UNORM);
            setTextureSamplerNoCache(BLANK_TEXTURE_ID, new SamplerCreateInfo(TextureFilter.NEAREST, WrapMode.REPEAT));
            BLANK_TEXTURE = getTexture(BLANK_TEXTURE_ID);
            prepareTexture(renderer, BLANK_TEXTURE);
        } else {
            throw new RuntimeException("Blank texture already initialized");
        }
    }

    public int generateTextureId() {
        if (!reusableTexIds.isEmpty()) {
            return reusableTexIds.dequeueInt();
        } else {
            return nextTexId++;
        }
    }

    public void deleteTexture(int textureId) {
        // TODO: actually delete image from gpu
        Texture removedTex = this.textureMap.remove(textureId);
        preparedTextures.remove(removedTex);
        reusableTexIds.enqueue(textureId);
    }

    public Texture getTexture(int textureId) {
        return textureMap.get(textureId);
    }

    public void createTexture(Renderer renderer, int textureId, int width, int height, int imgFormat) {
        TextureImage textureImage = new TextureImage(0L, 0L, 0L);
        VkKt.createTextureImage(renderer, common.device, width, height, imgFormat, textureImage);
        textureImage.setView(VkKt.createTextureImageView(common.device, imgFormat, textureImage.getTextureImage()));
        textureMap.put(textureId, new Texture(imgFormat, width, height, textureImage, null));
    }

    public void setTextureSampler(int textureId, int textureNo, SamplerCreateInfo samplerCreateInfo) {
        Map<Integer, TextureSampler> textureNoMap = samplerCache.computeIfAbsent(samplerCreateInfo, s -> new HashMap<>());
        TextureSampler textureSampler = textureNoMap.computeIfAbsent(textureNo, t -> new TextureSampler(samplerCreateInfo, common.device));
        textureMap.get(textureId).setTextureSampler(textureSampler.getPointer());
    }

    /**
     * Like The Above Method but without caching. Used for blank textures and FBO's
     *
     * @param textureId         the id of the texture
     * @param samplerCreateInfo the create info
     */
    public void setTextureSamplerNoCache(int textureId, SamplerCreateInfo samplerCreateInfo) {
        TextureSampler textureSampler = new TextureSampler(samplerCreateInfo, common.device);
        textureMap.get(textureId).setTextureSampler(textureSampler.getPointer());
    }

    public void drawToExistingTexture(Renderer renderer, Memory memory, int textureId, UploadableImage image, ImageRegion srcRegion, ImageRegion dstRegion) {
        Texture texture = getTexture(textureId);
        if (preparedTextures.contains(texture)) {
            VkKt.transitionImageLayout(
                    renderer,
                    common.device,
                    renderer.depthBuffer,
                    texture.getTextureImage().getTextureImage(),
                    texture.getImgFormat(),
                    VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL,
                    VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL
            );
            preparedTextures.remove(texture);
        }

        VkKt.copyToTexture(
                renderer,
                common.device,
                memory,
                image,
                srcRegion,
                dstRegion,
                texture
        );
    }

    public void drawToExistingTexture(Renderer renderer, Memory memory, int textureId, UploadableImage image) {
        ImageRegion region = new ImageRegion(image.getWidth(), image.getHeight(), 0, 0);
        drawToExistingTexture(renderer, memory, textureId, image, region, region);
    }

    public void prepareTexture(Renderer renderer, Texture texture) {
        if (!preparedTextures.contains(texture)) {
            VkKt.transitionImageLayout(
                    renderer,
                    common.device,
                    renderer.depthBuffer,
                    texture.getTextureImage().getTextureImage(),
                    texture.getImgFormat(),
                    VK10.VK_IMAGE_LAYOUT_TRANSFER_DST_OPTIMAL,
                    VK10.VK_IMAGE_LAYOUT_SHADER_READ_ONLY_OPTIMAL
            );
            preparedTextures.add(texture);
        }

    }
}
