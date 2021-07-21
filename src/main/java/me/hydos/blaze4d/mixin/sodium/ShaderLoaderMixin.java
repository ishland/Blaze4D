package me.hydos.blaze4d.mixin.sodium;

import me.hydos.blaze4d.api.shader.ShaderProcessor;
import me.jellysquid.mods.sodium.client.gl.shader.ShaderLoader;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * We Mixin here to modify the base shader.
 */
@Mixin(ShaderLoader.class)
public class ShaderLoaderMixin {

    @Inject(method = "getShaderSource", at = @At("RETURN"), cancellable = true)
    private static void processSodiumShader(ResourceLocation name, CallbackInfoReturnable<String> cir) {
        String shaderSrc = cir.getReturnValue();

        // Check if the shader is base.vsh
        if(shaderSrc.contains("layout(std140) uniform ubo_DrawParameters")) {
            cir.setReturnValue(ShaderProcessor.processSodiumBase(shaderSrc));
        }
    }
}
