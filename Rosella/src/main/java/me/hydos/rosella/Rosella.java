package me.hydos.rosella;

import me.hydos.rosella.device.VulkanDevice;
import me.hydos.rosella.device.VulkanQueues;
import me.hydos.rosella.display.Display;
import me.hydos.rosella.logging.DebugLogger;
import me.hydos.rosella.logging.DefaultDebugLogger;
import me.hydos.rosella.memory.ThreadPoolMemory;
import me.hydos.rosella.memory.buffer.GlobalBufferManager;
import me.hydos.rosella.render.renderer.Renderer;
import me.hydos.rosella.scene.object.GlobalObjectManager;
import me.hydos.rosella.scene.object.SimpleGlobalObjectManager;
import me.hydos.rosella.scene.object.impl.SimpleFramebufferObjectManager;
import me.hydos.rosella.util.SemaphorePool;
import me.hydos.rosella.vkobjects.VkCommon;
import me.hydos.rosella.vkobjects.VulkanInstance;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.message.StringFormatterMessageFactory;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkLayerProperties;

import java.nio.IntBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static me.hydos.rosella.render.util.VkUtilsKt.ok;
import static org.lwjgl.vulkan.EXTDebugUtils.vkDestroyDebugUtilsMessengerEXT;
import static org.lwjgl.vulkan.KHRSurface.vkDestroySurfaceKHR;
import static org.lwjgl.vulkan.VK10.*;

/**
 * Main Rosella class. If your interacting with the engine from here, You will most likely be safe.
 */
public class Rosella {

    public static final Logger LOGGER = LogManager.getLogger("Rosella", new StringFormatterMessageFactory());
    public static final int VULKAN_VERSION = VK_API_VERSION_1_0;
    public static final int POLYGON_MODE = VK_POLYGON_MODE_FILL;
    public final GlobalBufferManager bufferManager;
    public final GlobalObjectManager objectManager;
    public final VkCommon common = new VkCommon();
    public final Renderer renderer;

    public Rosella(Display display, String applicationName, boolean enableBasicValidation) {
        this(display, enableBasicValidation ? Collections.singletonList("VK_LAYER_KHRONOS_validation") : Collections.emptyList(), applicationName, new DefaultDebugLogger());
    }

    public Rosella(Display display, List<String> requestedValidationLayers, String applicationName, DebugLogger debugLogger) {
        List<String> requiredExtensions = display.getRequiredExtensions();
        if (!validationLayersSupported(requestedValidationLayers)) {
            throw new RuntimeException("The application requested validation layers but they are not supported");
        }

        // Setup core vulkan stuff
        common.display = display;
        common.vkInstance = new VulkanInstance(requestedValidationLayers, requiredExtensions, applicationName, debugLogger);
        common.surface = display.createSurface(common);
        common.device = new VulkanDevice(common, requestedValidationLayers);
        common.queues = new VulkanQueues(common);
        common.memory = new ThreadPoolMemory(common);
        common.semaphorePool = new SemaphorePool(common.device.rawDevice);
        this.objectManager = new SimpleGlobalObjectManager(this, common);

        // Setup the object manager
        this.renderer = new Renderer(this); //TODO: make swapchain, etc initialization happen outside of the renderer and in here
        ((SimpleGlobalObjectManager) this.objectManager).setRenderer(this.renderer); //FIXME ew
        common.textureManager.initializeBlankTexture(renderer);
        this.bufferManager = new GlobalBufferManager(this);

        // Tell the display we are initialized
        display.onReady();
    }

    /**
     * Get's the main FBO's object manager
     *
     * @return the main FBO's object manager
     */
    public SimpleFramebufferObjectManager getMainFboObjManager() {
        return common.fboManager.getMainFbo().objectManager;
    }

    /**
     * Free's the vulkan resources.
     */
    public void free() {
        common.device.waitForIdle();
        common.shaderManager.free();
        renderer.free();

        // Free the rest of it
        common.memory.free();
        common.semaphorePool.free();

        vkDestroyCommandPool(common.device.rawDevice, renderer.commandPool, null);
        vkDestroyDevice(common.device.rawDevice, null);
        vkDestroySurfaceKHR(common.vkInstance.rawInstance, common.surface, null);

        common.vkInstance.messenger.ifPresent(messenger -> { // FIXME
            vkDestroyDebugUtilsMessengerEXT(common.vkInstance.rawInstance, messenger, null);
        });

        vkDestroyInstance(common.vkInstance.rawInstance, null);

        common.display.exit();
    }

    /**
     * Checks if the system supports validation layers.
     *
     * @param requestedValidationLayers the validation layers requested by the application/user
     * @return if the system supports the request validation layers.
     */
    private boolean validationLayersSupported(List<String> requestedValidationLayers) {
        return getSupportedValidationLayers().containsAll(requestedValidationLayers);
    }

    /**
     * Gets all validation layers supported by the machine
     *
     * @return all validation layers that are supported
     */
    private Set<String> getSupportedValidationLayers() {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pLayerCount = stack.ints(0);
            ok(vkEnumerateInstanceLayerProperties(pLayerCount, null));
            VkLayerProperties.Buffer availableLayers = VkLayerProperties.mallocStack(pLayerCount.get(0), stack);
            ok(vkEnumerateInstanceLayerProperties(pLayerCount, availableLayers));
            return availableLayers.stream()
                    .map(VkLayerProperties::layerNameString)
                    .collect(Collectors.toSet());
        }

    }

    static {
        LOGGER.atLevel(Level.ALL);
    }
}
