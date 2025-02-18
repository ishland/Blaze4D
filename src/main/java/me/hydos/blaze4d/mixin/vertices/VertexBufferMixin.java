package me.hydos.blaze4d.mixin.vertices;

import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.VertexBuffer;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;
import me.hydos.blaze4d.Blaze4D;
import me.hydos.blaze4d.api.GlobalRenderSystem;
import me.hydos.blaze4d.api.util.ConversionUtils;
import me.hydos.rosella.memory.BufferInfo;
import me.hydos.rosella.memory.ManagedBuffer;
import me.hydos.rosella.render.info.RenderInfo;
import net.minecraft.client.renderer.ShaderInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.ByteBuffer;

/**
 * Turns out, Minecraft uses this class for world rendering. when a part of the world is to be rendered, the buffer will be cleared and replaced with just the sky. this will then be uploaded to a {@link VertexBuffer} and then cleared again for the rest of the game to render.
 */
@Mixin(VertexBuffer.class)
public class VertexBufferMixin {
    @Unique
    private RenderInfo currentRenderInfo;
    @Unique
    private BufferBuilder.DrawState drawState;

    /**
     * @author Blaze4D
     * @reason To render
     */
    @Overwrite
    private void upload_(BufferBuilder bufferBuilder) {
        Pair<BufferBuilder.DrawState, ByteBuffer> drawData = bufferBuilder.popNextBuffer();
        // We have to manipulate some stuff with the ByteBuffer before we store it
        BufferBuilder.DrawState providedDrawState = drawData.getFirst();
        ByteBuffer providedBuffer = drawData.getSecond();

        if (providedDrawState.vertexCount() > 0 && providedDrawState.indexCount() > 0) {
            if (!providedDrawState.indexOnly()) {
                providedBuffer.limit(providedDrawState.vertexBufferSize());
                BufferInfo vertexBuffer = Blaze4D.rosella.bufferManager.createVertexBuffer(new ManagedBuffer<>(providedBuffer, false));

                ManagedBuffer<ByteBuffer> rawIndexBuffer = GlobalRenderSystem.createIndices(providedDrawState.mode(), providedDrawState.indexCount());
                BufferInfo indexBuffer = Blaze4D.rosella.bufferManager.createIndexBuffer(rawIndexBuffer);

                drawState = providedDrawState;
                currentRenderInfo = new RenderInfo(vertexBuffer, indexBuffer, providedDrawState.indexCount());
            }
        }

        providedBuffer.limit(providedDrawState.bufferSize());
        providedBuffer.position(0);
    }

    /**
     * @author Blaze4D
     * @reason To render the sky
     */
    @Overwrite
    public void _drawWithShader(com.mojang.math.Matrix4f mcModelViewMatrix, com.mojang.math.Matrix4f mcProjectionMatrix, ShaderInstance shader) {
        GlobalRenderSystem.updateUniforms(shader, mcModelViewMatrix, mcProjectionMatrix);
        addBufferToRosella();
    }

    /**
     * @author Blaze4D
     * @reason To render the world
     */
    @Overwrite
    public void drawChunkLayer() {
        addBufferToRosella();
    }

    @Unique
    private void addBufferToRosella() {
        // TODO: why were these format checks here? (ported from old code) drawState.format() != com.mojang.blaze3d.vertex.DefaultVertexFormat.BLIT_SCREEN && drawState.format() != com.mojang.blaze3d.vertex.DefaultVertexFormat.POSITION
        if (currentRenderInfo != null && drawState != null) {
            GlobalRenderSystem.uploadPreCreatedObject(
                    currentRenderInfo,
                    GlobalRenderSystem.activeShader,
                    ConversionUtils.mcDrawModeToRosellaTopology(drawState.mode()),
                    GlobalRenderSystem.DEFAULT_POLYGON_MODE,
                    ConversionUtils.FORMAT_CONVERSION_MAP.get(drawState.format().getElements()),
                    GlobalRenderSystem.currentStateInfo.snapshot(),
                    GlobalRenderSystem.getCurrentTextureMap(),
                    Blaze4D.rosella
            );
        }
    }

    @Inject(method = "close", at = @At("HEAD"), cancellable = true)
    private void close(CallbackInfo ci) {
        if (currentRenderInfo != null) currentRenderInfo.free(Blaze4D.rosella.common.device, Blaze4D.rosella.common.memory);
        ci.cancel();
    }
}
