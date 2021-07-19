package me.hydos.rosella.fbo;

import me.hydos.rosella.render.texture.*;
import me.hydos.rosella.vkobjects.VkCommon;

/**
 * Contains an pointer to an ImageView & a Sampler to sample the image for shaders
 */
public class FboImageView {

    private static final SamplerCreateInfo fboSamplerCreateInfo = new SamplerCreateInfo(TextureFilter.NEAREST, WrapMode.CLAMP_TO_EDGE);
    public long imageView;
    public TextureSampler sampler;

    public FboImageView(long imageView, VkCommon common) {
        this.imageView = imageView;
        this.sampler = new TextureSampler(fboSamplerCreateInfo, common.device);
    }
}
