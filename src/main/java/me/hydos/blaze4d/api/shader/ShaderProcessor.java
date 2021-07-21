package me.hydos.blaze4d.api.shader;

/**
 * New version of the shader processor. now with more parsing
 */
public class ShaderProcessor {

    public static String processSodiumBase(String shaderSrc) {
        // FIXME: do actual parsing and move VanillaShaderProcessor into this
        return shaderSrc.replace("""
                layout(std140) uniform ubo_DrawParameters {
                    DrawParameters Chunks[256];
                };
                """, """
                // Hack to make vanilla shader processor process the shader properly
                uniform DrawParameters Chunks[256];
                """);
    }
}
