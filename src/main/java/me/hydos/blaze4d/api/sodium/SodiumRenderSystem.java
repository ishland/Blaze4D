package me.hydos.blaze4d.api.sodium;

import java.nio.ByteBuffer;

/**
 * Sodium Specific parts which dont belong in {@link me.hydos.blaze4d.api.VanillaRenderSystem}
 */
public class SodiumRenderSystem {

    /**
     * A Buffer for holding the offset data of the chunks. We store it to avoid redoing it each time we want to write to the UBO.
     */
    public static ByteBuffer chunkInfoBuffer;


}
