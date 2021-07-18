package me.hydos.rosella.fbo;

import it.unimi.dsi.fastutil.longs.LongArrayList;
import me.hydos.rosella.render.renderer.Renderer;
import me.hydos.rosella.render.swapchain.Swapchain;
import me.hydos.rosella.vkobjects.VkCommon;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkFramebufferCreateInfo;

import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.List;

import static me.hydos.rosella.render.util.VkUtilsKt.ok;
import static org.lwjgl.vulkan.VK10.*;

/**
 * A proper Frame Buffer Object (FBO) Manager for Rosella
 */
public class FrameBufferManager {

    private final Swapchain swapchain;
    private final VkCommon common;
    private final Renderer renderer;
    public final List<Framebuffer> framebuffers = new ArrayList<>();

    public FrameBufferManager(Swapchain swapchain, Renderer renderer, VkCommon common) {
        this.swapchain = swapchain;
        this.renderer = renderer;
        this.common = common;
    }

    public Framebuffer createFrameBuffer(RenderPass renderPass) {
        LongArrayList imageViews = new LongArrayList(swapchain.getSwapChainImageViews().size());
        try (MemoryStack stack = MemoryStack.stackPush()) {
            LongBuffer attachments = stack.longs(VK_NULL_HANDLE, renderer.depthBuffer.getDepthImageView());
            LongBuffer pFramebuffer = stack.mallocLong(1);
            VkFramebufferCreateInfo framebufferInfo = VkFramebufferCreateInfo.callocStack(stack)
                    .sType(VK_STRUCTURE_TYPE_FRAMEBUFFER_CREATE_INFO)
                    .renderPass(renderPass.getRenderPass())
                    .width(swapchain.getSwapChainExtent().width())
                    .height(swapchain.getSwapChainExtent().height())
                    .layers(1);
            for (long imageView : swapchain.getSwapChainImageViews()) {
                attachments.put(0, imageView);
                framebufferInfo.pAttachments(attachments);
                ok(vkCreateFramebuffer(common.device.rawDevice, framebufferInfo, null, pFramebuffer));
                imageViews.add(pFramebuffer.get(0));
            }
        }

        Framebuffer framebuffer = new Framebuffer(imageViews);
        framebuffers.add(framebuffer);
        return framebuffer;
    }

    public int getFboCount() {
        int result = 0;
        for (Framebuffer framebuffer : framebuffers) {
            for (Long imageView : framebuffer.imageViews()) {
                result += 1;
            }
        }
        return result;
    }
}
