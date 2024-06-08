package com.flyng.kalculus.core

import com.flyng.kalculus.kotlinext.filament.*
import kotlinx.browser.document
import kotlinx.browser.window
import org.khronos.webgl.Float32Array
import org.khronos.webgl.Uint16Array
import org.khronos.webgl.Uint32Array
import org.w3c.dom.HTMLCanvasElement
import org.w3c.dom.events.Event
import org.w3c.dom.get
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


class CoreEngine {
    private val canvas = document.getElementsByTagName("canvas")[0]!! as HTMLCanvasElement

    private val engine = Engine.create(canvas)

    private val scene = engine.createScene()

    private val triangle = EntityManager.get().create()

    private val vertexBuffer: VertexBuffer

    private val indexBuffer: IndexBuffer

    private val mat: Material

    private val instance: MaterialInstance

    init {
        scene.addEntity(triangle)

        vertexBuffer = VertexBuffer.Builder()
            .vertexCount(3)
            .bufferCount(2)
            .attribute(VertexAttribute.POSITION, 0, `VertexBuffer$AttributeType`.FLOAT2, 0, 8)
            .attribute(VertexAttribute.COLOR, 1, `VertexBuffer$AttributeType`.UBYTE4, 0, 4)
            .normalized(VertexAttribute.COLOR)
            .build(engine)
        vertexBuffer.setBufferAt(engine, 0, TRIANGLE_POSITIONS)
        vertexBuffer.setBufferAt(engine, 1, TRIANGLE_COLORS)

        indexBuffer = IndexBuffer.Builder()
            .indexCount(3)
            .bufferType(`IndexBuffer$IndexType`.USHORT)
            .build(engine)
        indexBuffer.setBuffer(engine, Uint16Array(arrayOf(0, 1, 2)))

        mat = engine.createMaterial("plain_color.filamat")
        instance = mat.getDefaultInstance()

        RenderableManager.Builder(1)
            .boundingBox(
                object : Box {
                    override var center = arrayOf(-1.0f, -1.0f, -1.0f)
                    override var halfExtent = arrayOf(1, 1, 1)
                }
            )
            .material(0, instance)
            .geometry(0, `RenderableManager$PrimitiveType`.TRIANGLES, vertexBuffer, indexBuffer)
            .build(engine, triangle)
    }

    private val swapChain = engine.createSwapChain()

    private val renderer = engine.createRenderer()

    private val camera = engine.createCamera(EntityManager.get().create())

    private val view = engine.createView()

    init {
        view.setCamera(camera)
        view.setScene(scene)

        renderer.setClearOptions(
            object : `Renderer$ClearOptions` {
                override var clearColor = arrayOf(0.0, 0.1, 0.2, 1.0)
                override var clear: Boolean? = true
            }
        )

        window.requestAnimationFrame(this::render)
        window.addEventListener("resize", this::resize)
    }

    private fun render(time: Double) {
        renderer.render(swapChain, view)
        window.requestAnimationFrame(this::render)
    }

    private fun resize(event: Event) {
        val dpr = window.devicePixelRatio
        val width = (window.innerWidth * dpr).toInt()
        val height = (window.innerHeight * dpr).toInt()
        canvas.setAttribute("width", width.toString())
        canvas.setAttribute("height", height.toString())
        view.setViewport(intArrayOf(0, 0, width, height))

        val aspect = width.toDouble() / height.toDouble()
        camera.setProjection(`Camera$Projection`.ORTHO, -aspect, aspect, -1.0, 1.0, 0.0, 1.0)
    }

    companion object {
        private val TRIANGLE_POSITIONS = Float32Array(
            array = arrayOf(
                1.0f, 0.0f,
                cos(PI * 2 / 3).toFloat(), sin(PI * 2 / 3).toFloat(),
                cos(PI * 4 / 3).toFloat(), sin(PI * 4 / 3).toFloat()
            )
        )

        private val TRIANGLE_COLORS = Uint32Array(
            array = arrayOf(0xffff0000.toInt(), 0xff00ff00.toInt(), 0xff0000ff.toInt())
        )
    }
}