package me.hydos.blaze4d;

import me.hydos.rosella.Rosella;
import me.hydos.rosella.display.GlfwWindow;
import me.hydos.rosella.scene.object.SimpleGlobalObjectManager;
import me.hydos.rosella.scene.object.impl.SimpleFramebufferObjectManager;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.StringFormatterMessageFactory;

public class Blaze4D implements ClientModInitializer {

    public static final Logger LOGGER = LogManager.getLogger("Blaze4D", new StringFormatterMessageFactory());
    public static final boolean VALIDATION_ENABLED = false;
    public static final boolean RENDERDOC_ENABLED = true;

    public static Rosella rosella;
    public static GlfwWindow window;

    public static void finishSetup() {
        rosella.renderer.rebuildCommandBuffers(rosella.renderer.renderPass, (SimpleGlobalObjectManager) rosella.objectManager);
    }

    @Override
    public void onInitializeClient() {
        try {
            if (RENDERDOC_ENABLED) {
                System.loadLibrary("renderdoc");
            }
        } catch (UnsatisfiedLinkError e) {
            LOGGER.warn("Unable to find renderdoc on path.");
        }
    }
}
