package me.hydos.rosella.fbo;

import me.hydos.rosella.scene.object.Renderable;

/**
 * Allows for multiple ways for the engine to handle objects.
 */
public interface FramebufferObjectManager {

    /**
     * adds an object into the current scene.
     *
     * @param renderable the material to add to the scene
     */
    Renderable addObject(Renderable renderable);

    /**
     * Called when the engine is exiting.
     */
    void free();
}

