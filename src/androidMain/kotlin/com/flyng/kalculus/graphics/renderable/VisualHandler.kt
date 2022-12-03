package com.flyng.kalculus.graphics.renderable

import com.flyng.kalculus.core.manager.MaterialManager
import com.flyng.kalculus.exposition.visual.Visual
import com.flyng.kalculus.exposition.visual.boundary.Boundary
import com.flyng.kalculus.exposition.visual.primitive.DynamicColor
import com.flyng.kalculus.exposition.visual.primitive.PlainColor
import com.flyng.kalculus.exposition.visual.primitive.Primitive
import com.flyng.kalculus.exposition.visual.primitive.Topology
import com.flyng.kalculus.exposition.visual.vertex.ByteSize
import com.flyng.kalculus.exposition.visual.vertex.ColorAttribute
import com.flyng.kalculus.exposition.visual.vertex.PositionAttribute
import com.flyng.kalculus.exposition.visual.vertex.Vertex
import com.flyng.kalculus.graphics.renderable.mesh.Mesh
import com.google.android.filament.*
import java.nio.ByteBuffer
import java.nio.ByteOrder

actual object VisualHandler : Renderable {
    override fun loadMesh(
        visual: Visual,
        engine: Engine,
        materialManager: MaterialManager,
    ): Mesh {
        // create and load vertex buffer
        val vertexBuffer = loadVertexBuffer(visual.vertices(), engine)

        // create and load index buffer
        val indexBuffer = loadIndexBuffer(visual.indices(), engine)

        return createMesh(
            visual.primitives(),
            visual.boundary(),
            engine,
            vertexBuffer,
            indexBuffer,
            materialManager,
        )
    }

    private fun loadVertexBuffer(vertices: List<Vertex>, engine: Engine): VertexBuffer {
        if (vertices.isEmpty()) {
            throw RuntimeException("A Visual must have at least 1 vertex.")
        }
        val vertexCount = vertices.size
        val attributes = vertices[0].data
        val vertexSize = attributes.fold(0) { prev, attr -> prev + attr.size }

        val vertexData = ByteBuffer.allocate(vertexCount * vertexSize)
            // respect the native byte order
            .order(ByteOrder.nativeOrder())
            // put vertex data
            .apply {
                vertices.forEach { vertex ->
                    vertex.data.forEach {
                        when (it) {
                            is PositionAttribute -> {
                                putFloat(it.x)
                                putFloat(it.y)
                                putFloat(it.z)
                            }

                            is ColorAttribute -> {
                                putInt(it.color)
                            }
                        }
                    }
                }
            }
            // make sure the cursor is pointing in the right place in the byte buffer
            .flip()

        // declare layout of the mesh
        val vertexBuffer = VertexBuffer.Builder()
            .bufferCount(1)
            .vertexCount(vertexCount)
            .apply {
                attributes.forEachIndexed { idx, attr ->
                    // Because we interleave position and color data we must specify offset and stride
                    // We could use de-interleaved data by declaring two buffers and giving each
                    // attribute a different buffer index
                    when (attr) {
                        is PositionAttribute -> {
                            attribute(
                                VertexBuffer.VertexAttribute.POSITION,
                                0,  // each visual should only use 1 vertex buffer
                                VertexBuffer.AttributeType.FLOAT3,
                                attributes
                                    .dropLast(attributes.size - idx)
                                    .fold(0) { prev, it -> prev + it.size },
                                vertexSize
                            )
                            if (!attr.normalized) {
                                normalized(VertexBuffer.VertexAttribute.POSITION)
                            }
                        }

                        is ColorAttribute -> {
                            attribute(
                                VertexBuffer.VertexAttribute.COLOR,
                                0,
                                VertexBuffer.AttributeType.UBYTE4,
                                attributes
                                    .dropLast(attributes.size - idx)
                                    .fold(0) { prev, it -> prev + it.size },
                                vertexSize
                            )
                            if (!attr.normalized) {
                                // We store colors as unsigned bytes but since we want values between 0 and 1
                                // in the material (shaders), we must mark the attribute as normalized
                                normalized(VertexBuffer.VertexAttribute.COLOR)
                            }
                        }
                    }
                }
            }
            .build(engine)

        // Feed the vertex data to the mesh
        // We only set 1 buffer because the data is interleaved
        return vertexBuffer.apply { setBufferAt(engine, 0, vertexData) }
    }

    private fun loadIndexBuffer(indices: ShortArray, engine: Engine): IndexBuffer {
        val indexCount = indices.size

        // create indices
        val indexData = ByteBuffer.allocate(indexCount * ByteSize.SHORT)
            .order(ByteOrder.nativeOrder())
            .apply {
                indices.forEach {
                    putShort(it)
                }
            }
            .flip()

        val indexBuffer = IndexBuffer.Builder()
            .indexCount(indexCount)
            .bufferType(IndexBuffer.Builder.IndexType.USHORT)
            .build(engine)

        return indexBuffer.apply { setBuffer(engine, indexData) }
    }

    private fun createMesh(
        primitives: List<Primitive>,
        boundary: Boundary,
        engine: Engine,
        vertexBuffer: VertexBuffer,
        indexBuffer: IndexBuffer,
        materialManager: MaterialManager,
    ): Mesh {
        val primitiveCount = primitives.size

        // To create a renderable we first create a generic entity
        val entity = engine.entityManager.create()

        // Overall bounding box of the renderable
        val box = with(boundary) {
            Box(centerX, centerY, centerZ, halfExtentX, halfExtentY, halfExtentZ)
        }

        // Store all material instances of the renderable
        val materials = mutableListOf<MaterialInstance>()

        // We then create a renderable component on that entity
        // A renderable is made of several primitives
        RenderableManager.Builder(primitiveCount)
            .boundingBox(box)
            .apply {
                primitives.forEachIndexed { index, (topology, offset, count, material) ->
                    geometry(
                        index,
                        when (topology) {
                            Topology.POINTS -> RenderableManager.PrimitiveType.POINTS
                            Topology.LINES -> RenderableManager.PrimitiveType.LINES
                            Topology.TRIANGLES -> RenderableManager.PrimitiveType.TRIANGLES
                            Topology.LINE_STRIP -> RenderableManager.PrimitiveType.LINE_STRIP
                            Topology.TRIANGLE_STRIP -> RenderableManager.PrimitiveType.TRIANGLE_STRIP
                        },
                        vertexBuffer,
                        indexBuffer,
                        offset,
                        count
                    )
                    material(
                        index,
                        materialManager[material].let { filamat ->
                            when (material) {
                                is PlainColor -> filamat.defaultInstance.also { materials.add(it) }
                                is DynamicColor -> {
                                    filamat.createInstance().apply {
                                        with(material.baseColor) {
                                            setParameter(
                                                "baseColor", Colors.RgbaType.SRGB,
                                                red, green, blue, alpha
                                            )
                                        }
                                        materials.add(this)
                                    }
                                }
                            }
                        }
                    )
                }
            }
            .build(engine, entity)

        return Mesh(entity, indexBuffer, vertexBuffer, box, materials)
    }
}