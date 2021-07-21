package me.hydos.blaze4d.mixin;

import net.fabricmc.loader.api.FabricLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.vulkan.VK12;
import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class Blaze4DMixinPlugin implements IMixinConfigPlugin {
    private static final String MIXIN_PACKAGE_ROOT = "me.hydos.blaze4d.mixin.";
    private static final Logger LOGGER = LogManager.getLogger("Blaze4D");

    public boolean sodiumPresent;
    public boolean irisPresent;

    @Override
    public void onLoad(String mixinPackage) {
        sodiumPresent = FabricLoader.getInstance().getModContainer("sodium").isPresent();
        irisPresent = FabricLoader.getInstance().getModContainer("iris").isPresent();

        LOGGER.info("Sodium Present: " + sodiumPresent);
        LOGGER.info("Iris Present: " + irisPresent);
    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith(MIXIN_PACKAGE_ROOT)) {
            String mixinName = mixinClassName.substring(MIXIN_PACKAGE_ROOT.length());

            String[] split = mixinName.split("\\.");
            if (split.length == 2) {
                // Must have a sub package. check if its mod dependent
                String category = split[0];
                return !(category.startsWith("iris") && !irisPresent || category.startsWith("sodium") && !sodiumPresent);
            }
            return true;
        } else {
            LOGGER.error("Tried to load a foreign mixin");
            return false;
        }
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {

    }
}
