package me.hydos.rosella.fbo;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import me.hydos.rosella.render.texture.*;
import me.hydos.rosella.vkobjects.VkCommon;
import org.lwjgl.vulkan.VkCommandBuffer;

import java.util.List;

/**
 * Contains an pointer to an ImageView & a Sampler to sample the image for shaders
 */
public class FboImage {

    private static final SamplerCreateInfo fboSamplerCreateInfo = new SamplerCreateInfo(TextureFilter.NEAREST, WrapMode.CLAMP_TO_EDGE);
    public long imageView;
    public TextureSampler sampler;
    public VkCommandBuffer commandBuffers;

    public FboImage(long imageView, VkCommon common) {
        this.imageView = imageView;
        this.sampler = new TextureSampler(fboSamplerCreateInfo, common.device);
    }
}
