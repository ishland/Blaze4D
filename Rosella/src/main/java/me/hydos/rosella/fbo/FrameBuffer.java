package me.hydos.rosella.fbo;

import it.unimi.dsi.fastutil.longs.LongList;
import me.hydos.rosella.Rosella;
import me.hydos.rosella.scene.object.impl.SimpleFramebufferObjectManager;

/**
 * Information for a FrameBuffer Object
 */
public class FrameBuffer {

    public final SimpleFramebufferObjectManager objectManager;
    public final LongList imageViews;

    public FrameBuffer(LongList imageViews, Rosella rosella) {
        this.imageViews = imageViews;
        objectManager = new SimpleFramebufferObjectManager(rosella, rosella.common);
    }
}
