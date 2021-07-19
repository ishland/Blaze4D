package me.hydos.blaze4d.mixin.integration;

import com.mojang.blaze3d.pipeline.RenderTarget;
import me.hydos.blaze4d.Blaze4D;
import me.hydos.blaze4d.api.GlobalRenderSystem;
import me.hydos.rosella.fbo.FrameBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderTarget.class)
public class FramebufferMixin {

    @Shadow
    public int width;

    @Shadow
    public int height;

    @Shadow
    public int viewWidth;

    @Shadow
    public int viewHeight;

    @Shadow public int frameBufferId;
    private FrameBuffer rosellaFrameBuffer;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void createFrameBufferObject(boolean useDepth, CallbackInfo ci) {
        this.rosellaFrameBuffer = Blaze4D.rosella.common.fboManager.createFrameBuffer(Blaze4D.rosella.renderer.renderPass, Blaze4D.rosella);
    }

    @Inject(method = "resize", at = @At("HEAD"), cancellable = true)
    private void resizingBadAndWorst(int width, int height, boolean getError, CallbackInfo ci) {
        this.width = width;
        this.height = height;
        this.viewWidth = width;
        this.viewHeight = height;
        ci.cancel();
    }

    @Inject(method = "_blitToScreen", at = @At("HEAD"), cancellable = true)
    private void weDontSupportFbosAtm(int width, int height, boolean disableBlend, CallbackInfo ci) {
        Blaze4D.rosella.common.fboManager.setActiveFbo(rosellaFrameBuffer);
        GlobalRenderSystem.render();
        ci.cancel();
    }

    /**
     * @author Blaze4D
     * @reason Frame Buffers
     */
    @Overwrite
    public void createBuffers(int width, int height, boolean clearError) {
        frameBufferId = 69420;
    }
}
