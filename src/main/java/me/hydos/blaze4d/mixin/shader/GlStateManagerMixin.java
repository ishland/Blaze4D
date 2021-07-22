package me.hydos.blaze4d.mixin.shader;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import me.hydos.blaze4d.Blaze4D;
import me.hydos.blaze4d.api.VanillaRenderSystem;
import me.hydos.blaze4d.api.shader.MinecraftShaderProgram;
import me.hydos.blaze4d.api.shader.ShaderContext;
import me.hydos.blaze4d.api.util.ByteArrayResource;
import me.hydos.rosella.render.resource.Identifier;
import me.hydos.rosella.render.resource.Resource;
import me.hydos.rosella.render.shader.RawShaderProgram;
import me.hydos.rosella.render.shader.ShaderType;
import me.hydos.rosella.scene.object.impl.SimpleObjectManager;
import org.lwjgl.opengl.GL20;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

/**
 * This Mixin handles the interactions between Minecraft shaders and GL programs and passes it onto rosella
 */
@Mixin(value = GlStateManager.class, remap = false)
public class GlStateManagerMixin {

    //========================
    //        SHADERS
    //========================

    /**
     * @author Blaze4D
     * @reason To Integrate Shaders
     */
    @Overwrite
    public static int glCreateShader(int type) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        // Check last shader's type to see if they belong in the same shader
        // TODO: maybe support more shader types in the future?
        ShaderType rosellaType = type == GL20.GL_VERTEX_SHADER ? ShaderType.VERTEX_SHADER : ShaderType.FRAGMENT_SHADER;
        ShaderContext shaderContext = new ShaderContext();
        shaderContext.glShaderType = type;
        shaderContext.rosellaShaderType = rosellaType;
        VanillaRenderSystem.SHADER_MAP.put(VanillaRenderSystem.nextShaderId, shaderContext);
        VanillaRenderSystem.nextShaderId++;
        return VanillaRenderSystem.nextShaderId - 1;
    }

    /**
     * @author Blaze4D
     * @reason To Integrate Shaders
     */
    @Overwrite
    public static void glShaderSource(int shader, List<String> shaderLines) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        ShaderContext context = VanillaRenderSystem.SHADER_MAP.get(shader);
        if (context == null) {
            throw new RuntimeException("Failed to get ShaderContext. (No shader was found with id " + shader + ")");
        }

        context.shader = shaderSrcToResource(shaderLines);
    }

    /**
     * @author Blaze4D
     * @reason To Integrate Shaders
     * If something ever goes wrong, assume its our fault :(
     */
    @Overwrite
    public static String glGetShaderInfoLog(int shader, int maxLength) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return "Internal Blaze4D Error";
    }

    /**
     * @author Blaze4D
     * @reason To Integrate Shaders
     * <p>
     * This method is really just a method to get the compilation status of a shader.
     * as long as no exceptions have been thrown, assume everything is OK
     */
    @Overwrite
    public static int glGetShaderi(int shader, int pname) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        return GL20.GL_TRUE;
    }

    //========================
    //    SHADER PROGRAMS
    //========================

    /**
     * @author Blaze4D
     * @reason To Integrate Shader Programs
     */
    @Overwrite
    public static int glCreateProgram() {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        MinecraftShaderProgram program = new MinecraftShaderProgram(
                null,
                null,
                Blaze4D.rosella.common.device,
                Blaze4D.rosella.common.memory,
                VanillaRenderSystem.DEFAULT_MAX_OBJECTS,
                VanillaRenderSystem.blaze4d$capturedShaderProgram.blaze4d$getUniforms(),
                VanillaRenderSystem.processedSamplers);
        VanillaRenderSystem.processedSamplers.clear();
        VanillaRenderSystem.currentSamplerBinding = 1;
        VanillaRenderSystem.SHADER_PROGRAM_MAP.put(VanillaRenderSystem.nextShaderProgramId, program);
        Blaze4D.rosella.renderer.rebuildCommandBuffers(Blaze4D.rosella.renderer.renderPass, (SimpleObjectManager) Blaze4D.rosella.objectManager);
        return VanillaRenderSystem.nextShaderProgramId++;
    }

    /**
     * @author Blaze4D
     * @reason To Integrate Shader Programs
     */
    @Overwrite
    public static void glAttachShader(int programId, int shaderId) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        ShaderContext shader = VanillaRenderSystem.SHADER_MAP.get(shaderId);
        RawShaderProgram program = VanillaRenderSystem.SHADER_PROGRAM_MAP.get(programId);
        if (program == null) {
            throw new RuntimeException("Shader was requested without begin registered");
        }

        if (shader.rosellaShaderType == ShaderType.VERTEX_SHADER) {
            program.setVertexShader(shader.shader);
        } else {
            program.setFragmentShader(shader.shader);
        }
    }

    /**
     * @author Blaze4D
     * @reason To Integrate Shader Programs
     * <p>
     * Basically compiles the shader program
     */
    @Overwrite
    public static void glLinkProgram(int program) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        Identifier id = VanillaRenderSystem.generateId(program);
        Blaze4D.rosella.objectManager.addShader(VanillaRenderSystem.SHADER_PROGRAM_MAP.get(program));
    }

    /**
     * @author Blaze4D
     * @reason To Integrate Shader Programs
     * <p>
     * Since shaders should define this in the vertex format, we shouldn't need to worry about this.
     */
    @Overwrite
    public static void _glBindAttribLocation(int program, int index, CharSequence name) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
    }

    /**
     * @author Blaze4D
     * @reason To Integrate Shader Programs
     * <p>
     * C Documentation: "Returns a parameter from a program object"
     * It really just's lets you query things from the program like status, etc
     */
    @Overwrite
    public static int glGetProgrami(int program, int pname) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        switch (pname) {
            case GL20.GL_LINK_STATUS, GL20.GL_COMPILE_STATUS -> {
                // Since we throw exceptions instead of failing quietly, assume everything is OK
                return 1;
            }

            default -> VanillaRenderSystem.programErrorLog = "glGetProgramI is not implemented for " + pname;
        }
        return 0;
    }

    /**
     * @author Blaze4D
     * @reason To Integrate Shader Programs
     * <p>
     * When something errors, this is called to figure out what went wrong.
     */
    @Overwrite
    public static String glGetProgramInfoLog(int program, int maxLength) {
        RenderSystem.assertThread(RenderSystem::isOnRenderThread);
        String lastError = VanillaRenderSystem.programErrorLog;
        VanillaRenderSystem.programErrorLog = "";
        return lastError;
    }

    //========================
    //       UTILITIES
    //========================

    /**
     * Converts a list of lines of shader source code into a {@link Resource} which can be loaded by Rosella
     *
     * @param shaderSrc the source of the shader
     * @return a readable resource for {@link me.hydos.rosella.Rosella}
     */
    private static Resource shaderSrcToResource(List<String> shaderSrc) {
        byte[] shaderBytes = String.join("\n", shaderSrc).getBytes(StandardCharsets.UTF_8);
        return new ByteArrayResource(shaderBytes);
    }

    @Overwrite
    public static void glCompileShader(int shader) {
    }

    @Overwrite
    public static int _glGetUniformLocation(int program, CharSequence name) {
        return 0;
    }

    @Overwrite
    public static void glDeleteShader(int shader) {
    }

    @Overwrite
    public static void glDeleteProgram(int program) {
    }

    @Overwrite
    public static void _glUniform1(int location, IntBuffer value) {
    }

    @Overwrite
    public static void _glUniform1i(int location, int value) {
    }

    @Overwrite
    public static void _glUniform1(int location, FloatBuffer value) {
    }

    @Overwrite
    public static void _glUniform2(int location, IntBuffer value) {
    }

    @Overwrite
    public static void _glUniform2(int location, FloatBuffer value) {
    }

    @Overwrite
    public static void _glUniform3(int location, IntBuffer value) {
    }

    @Overwrite
    public static void _glUniform3(int location, FloatBuffer value) {
    }

    @Overwrite
    public static void _glUniform4(int location, IntBuffer value) {
    }

    @Overwrite
    public static void _glUniform4(int location, FloatBuffer value) {
    }

    @Overwrite
    public static void _glUniformMatrix2(int location, boolean transpose, FloatBuffer value) {
    }

    @Overwrite
    public static void _glUniformMatrix3(int location, boolean transpose, FloatBuffer value) {
    }

    @Overwrite
    public static void _glUniformMatrix4(int location, boolean transpose, FloatBuffer value) {
    }

    @Overwrite
    public static int _glGetAttribLocation(int program, CharSequence name) {
        return 0;
    }

    @Overwrite
    public static void _glUseProgram(int program) {
    }
}
