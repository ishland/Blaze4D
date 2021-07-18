package me.hydos.rosella.scene.object.impl;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import me.hydos.rosella.Rosella;
import me.hydos.rosella.fbo.RenderPass;
import me.hydos.rosella.render.info.InstanceInfo;
import me.hydos.rosella.render.info.RenderInfo;
import me.hydos.rosella.render.material.Material;
import me.hydos.rosella.render.renderer.Renderer;
import me.hydos.rosella.render.shader.RawShaderProgram;
import me.hydos.rosella.render.shader.ShaderManager;
import me.hydos.rosella.render.shader.ShaderProgram;
import me.hydos.rosella.render.texture.TextureManager;
import me.hydos.rosella.scene.object.ObjectManager;
import me.hydos.rosella.scene.object.Renderable;
import me.hydos.rosella.vkobjects.VkCommon;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Just a basic object manager
 */
public class SimpleObjectManager implements ObjectManager {

    private final Rosella rosella;
    public Renderer renderer;
    private final VkCommon common;
    public final Map<RenderInfo, List<InstanceInfo>> renderObjects = new Object2ObjectArrayMap<>();

    public final List<Material> materials = new ArrayList<>();
    public final List<Material> unprocessedMaterials = new ArrayList<>();

    public SimpleObjectManager(Rosella rosella, VkCommon common) {
        common.shaderManager = new ShaderManager(rosella);
        common.textureManager = new TextureManager(common);
        this.rosella = rosella;
        this.common = common;
    }

    @Override
    public void rebuildCmdBuffers(RenderPass pass, Rosella rosella, Renderer renderer) {

    }

    @Override
    public Renderable addObject(Renderable obj) {
        if (!renderObjects.containsKey(obj.getRenderInfo())) {
            renderObjects.put(obj.getRenderInfo(), new ArrayList<>());
        }
        obj.onAddedToScene(rosella);
        renderObjects.get(obj.getRenderInfo()).add(obj.getInstanceInfo());
        return obj;
    }

    @Override
    public Material registerMaterial(Material material) {
        material.loadTextures(this, rosella); //TODO: ew ew ew ew ew ew ew ew ew ew ew ew ew ew ew ew ew ew ew ew ew ew ew ew ew ew
        unprocessedMaterials.add(material);
        return material;
    }

    @Override
    public ShaderProgram addShader(RawShaderProgram program) {
        return common.shaderManager.getOrCreateShader(program);
    }

    @Override
    public void submitMaterials() {
        for (Material material : unprocessedMaterials) {
            if (material.getShader().getRaw().getDescriptorSetLayout() == 0L) {
                material.getShader().getRaw().createDescriptorSetLayout();
            }
            material.pipeline = common.pipelineManager.getPipeline(material, renderer);
            materials.add(material);
        }
        unprocessedMaterials.clear();
    }

    @Override
    public void free() {
        materials.clear();
    }

    @Override
    public void postInit(Renderer renderer) {
        this.renderer = renderer;
    }
}
