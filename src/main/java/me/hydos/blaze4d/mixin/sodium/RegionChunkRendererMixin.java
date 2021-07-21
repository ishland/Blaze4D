package me.hydos.blaze4d.mixin.sodium;

import me.hydos.blaze4d.api.sodium.SodiumRenderSystem;
import me.jellysquid.mods.sodium.client.gl.device.CommandList;
import me.jellysquid.mods.sodium.client.gl.device.RenderDevice;
import me.jellysquid.mods.sodium.client.gl.tessellation.GlTessellation;
import me.jellysquid.mods.sodium.client.model.vertex.type.ChunkVertexType;
import me.jellysquid.mods.sodium.client.render.chunk.RegionChunkRenderer;
import me.jellysquid.mods.sodium.client.render.chunk.ShaderChunkRenderer;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

@Mixin(RegionChunkRenderer.class)
public abstract class RegionChunkRendererMixin extends ShaderChunkRenderer {

    @Shadow
    private static ByteBuffer createChunkInfoBuffer(MemoryStack stack) {
        return null;
    }

    public RegionChunkRendererMixin(RenderDevice device, ChunkVertexType vertexType) {
        super(device, vertexType);
    }

    @Inject(method = "<init>", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/gl/device/CommandList;uploadData(Lme/jellysquid/mods/sodium/client/gl/buffer/GlMutableBuffer;Ljava/nio/ByteBuffer;Lme/jellysquid/mods/sodium/client/gl/buffer/GlBufferUsage;)V"))
    private void setChunkInfoBuffer(RenderDevice device, ChunkVertexType vertexType, CallbackInfo ci) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            SodiumRenderSystem.chunkInfoBuffer = createChunkInfoBuffer(stack);
        }
    }

    @Inject(method = "executeDrawBatches", at = @At(value = "INVOKE", target = "Lme/jellysquid/mods/sodium/client/gl/device/DrawCommandList;multiDrawElementsBaseVertex(Lorg/lwjgl/PointerBuffer;Ljava/nio/IntBuffer;Ljava/nio/IntBuffer;Lme/jellysquid/mods/sodium/client/gl/tessellation/GlIndexType;)V"))
    private void _render(CommandList commandList, GlTessellation tessellation, CallbackInfo ci) {
        tessellation.bind(commandList);
    }
}
