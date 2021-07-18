package me.hydos.rosella.fbo;

import it.unimi.dsi.fastutil.longs.LongList;

/**
 * Information for a FrameBuffer Object
 */
public record FrameBuffer(LongList imageViews) {
}
