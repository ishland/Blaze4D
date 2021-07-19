package me.hydos.rosella.fbo;

import it.unimi.dsi.fastutil.longs.LongList;
import me.hydos.rosella.Rosella;
import me.hydos.rosella.scene.object.impl.SimpleFramebufferObjectManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Information for a FrameBuffer Object
 */
public class FrameBuffer {

    public final SimpleFramebufferObjectManager objectManager;
    public final List<FboImageView> imageViews = new ArrayList<>();

    public FrameBuffer(LongList imageViews, Rosella rosella) {
        for (Long imageView : imageViews) {
            this.imageViews.add(new FboImageView(imageView, rosella.common));
        }
        objectManager = new SimpleFramebufferObjectManager(rosella, rosella.common);
    }
}
