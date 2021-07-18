package me.hydos.rosella.render.material

import me.hydos.rosella.fbo.RenderPass
import me.hydos.rosella.render.Topology
import me.hydos.rosella.render.material.state.StateInfo
import me.hydos.rosella.render.shader.ShaderProgram
import me.hydos.rosella.render.vertex.VertexFormat

data class PipelineCreateInfo(
    val renderPass: RenderPass,
    val descriptorSetLayout: Long,
    val polygonMode: Int,
    val shader: ShaderProgram,
    val topology: Topology,
    val vertexFormat: VertexFormat,
    val stateInfo: StateInfo,
)
