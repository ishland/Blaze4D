package me.hydos.blaze4d.mixin.vertices;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import me.hydos.blaze4d.Blaze4D;
import me.hydos.blaze4d.api.VanillaRenderSystem;
import me.hydos.blaze4d.api.util.ConversionUtils;
import me.hydos.rosella.memory.ManagedBuffer;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryUtil;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import java.nio.ByteBuffer;

@Mixin(BufferUploader.class)
public class BufferRendererMixin {

    /**
     * @author Blaze4D
     * @reason to draw
     */
    @Overwrite
    public static void end(BufferBuilder bufferBuilder) {
        Matrix4f projMatrix = new Matrix4f(VanillaRenderSystem.projectionMatrix);
        Matrix4f modelViewMatrix = new Matrix4f(VanillaRenderSystem.modelViewMatrix);
        Vector3f chunkOffset = new Vector3f(VanillaRenderSystem.chunkOffset);
        com.mojang.math.Vector3f shaderLightDirections0 = VanillaRenderSystem.shaderLightDirections0.copy();
        com.mojang.math.Vector3f shaderLightDirections1 = VanillaRenderSystem.shaderLightDirections1.copy();

        Pair<BufferBuilder.DrawState, ByteBuffer> drawData = bufferBuilder.popNextBuffer();
        BufferBuilder.DrawState drawState = drawData.getFirst();
        ByteBuffer originalBuffer = drawData.getSecond();

        originalBuffer.clear();

        int vertexCount = drawState.vertexCount();

        if (vertexCount > 0) {
            ByteBuffer copiedBuffer = MemoryUtil.memAlloc(drawState.vertexBufferSize());
            copiedBuffer.put(0, originalBuffer, 0, drawState.vertexBufferSize());

            ObjectIntPair<ManagedBuffer<ByteBuffer>> indexBufferPair = VanillaRenderSystem.createIndices(drawState.mode(), drawState.vertexCount());

            VanillaRenderSystem.uploadAsyncCreatableObject(
                    new ManagedBuffer<>(copiedBuffer, true),
                    indexBufferPair.key(),
                    indexBufferPair.valueInt(),
                    ConversionUtils.FORMAT_CONVERSION_MAP.get(drawState.format().getElements()),
                    VanillaRenderSystem.activeShader,
                    VanillaRenderSystem.createTextureArray(),
                    VanillaRenderSystem.currentStateInfo.snapshot(),
                    projMatrix,
                    modelViewMatrix,
                    chunkOffset,
                    shaderLightDirections0,
                    shaderLightDirections1,
                    drawState.format(),
                    drawState.mode(),
                    Blaze4D.rosella
            );
        }
    }

    /**
     * @author Blaze4D
     * @reason to draw
     */
    @Overwrite
    public static void _endInternal(BufferBuilder builder) {
        end(builder);
    }
}
