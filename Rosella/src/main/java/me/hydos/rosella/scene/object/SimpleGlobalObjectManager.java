package me.hydos.rosella.scene.object;

import me.hydos.rosella.Rosella;
import me.hydos.rosella.render.material.Material;
import me.hydos.rosella.render.renderer.Renderer;
import me.hydos.rosella.render.shader.RawShaderProgram;
import me.hydos.rosella.render.shader.ShaderProgram;
import me.hydos.rosella.vkobjects.VkCommon;

import java.util.ArrayList;
import java.util.List;

public class SimpleGlobalObjectManager implements GlobalObjectManager {

    private final Rosella rosella;
    public Renderer renderer;
    private final VkCommon common;

    public final List<Material> materials = new ArrayList<>();
    public final List<Material> unprocessedMaterials = new ArrayList<>();

    public SimpleGlobalObjectManager(Rosella rosella, VkCommon common) {
        this.rosella = rosella;
        this.common = common;
    }

    public void setRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    @Override
    public Material registerMaterial(Material material) {
        material.loadTextures(rosella); //TODO: ew ew ew ew ew ew ew ew ew ew ew ew ew ew ew ew ew ew ew ew ew ew ew ew ew ew
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
}
