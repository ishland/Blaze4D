package me.hydos.rosella.scene.object;

import me.hydos.rosella.render.material.Material;
import me.hydos.rosella.render.shader.RawShaderProgram;
import me.hydos.rosella.render.shader.ShaderProgram;

/**
 * Allows for multiple ways for the engine to handle objects.
 */
public interface GlobalObjectManager {

    /**
     * registers a {@link Material} into the engine.
     *
     * @param material the material to register
     */
    Material registerMaterial(Material material);

    /**
     * registers a {@link RawShaderProgram} into the engine.
     *
     * @param program the program to register
     */
    ShaderProgram addShader(RawShaderProgram program);

    /**
     * Called when new materials are ready to be processed.
     */
    void submitMaterials();
}
