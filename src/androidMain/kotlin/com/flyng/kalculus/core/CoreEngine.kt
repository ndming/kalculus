package com.flyng.kalculus.core

import android.content.Context
import android.content.res.AssetManager
import android.util.Log
import android.view.Choreographer
import android.view.Surface
import android.view.SurfaceView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.flyng.kalculus.adapter.VisualAdapter
import com.flyng.kalculus.core.manager.*
import com.flyng.kalculus.exposition.visual.Visual
import com.flyng.kalculus.theme.ThemeMode
import com.flyng.kalculus.theme.ThemeProfile
import com.google.android.filament.*
import com.google.android.filament.android.DisplayHelper
import com.google.android.filament.android.UiHelper
import com.google.android.filament.android.UiHelper.ContextErrorPolicy

/**
 * The core rendering engine, backed by [Filament]. The core should be created when the owner activity or fragment into
 * which graphics is rendered enters [Lifecycle.State.CREATED] state. By itself, the engine is a [LifecycleEventObserver]
 * that can observe the host activity/fragment's [Lifecycle]. When creating the engine, the host should add the core to
 * its lifecycle's observers so that the core can automatically react to the lifecycle of the host.
 *
 * Example usage:
 *
 * ```kotlin
 *  class MainActivity : ComponentActivity() {
 *
 *      private lateinit var coreEngine: CoreEngine
 *
 *      override fun onCreate(savedInstanceState: Bundle?) {
 *          super.onCreate(savedInstanceState)
 *
 *          coreEngine = CoreEngine(context = this, assetManager = assets).also {
 *              lifecycle.addObserver(it)
 *          }
 *      }
 * ```
 *
 * The engine will create an instance of [SurfaceView] which can then be used to embed to Android's View layout or
 * `AndroidView` composable.
 *
 * Example use with Android's view-based layout:
 *
 * ```kotlin
 *  class MainActivity : ComponentActivity() {
 *
 *      private lateinit var coreEngine: CoreEngine
 *
 *      override fun onCreate(savedInstanceState: Bundle?) {
 *          super.onCreate(savedInstanceState)
 *
 *          coreEngine = CoreEngine(...)
 *
 *          setContentView(coreEngine.surfaceView)
 *      }
 * ```
 *
 * Example use with Jetpack's Composable:
 *
 * ```kotlin
 *  class MainActivity : ComponentActivity() {
 *
 *      private lateinit var coreEngine: CoreEngine
 *
 *      override fun onCreate(savedInstanceState: Bundle?) {
 *          super.onCreate(savedInstanceState)
 *
 *          coreEngine = CoreEngine(...)
 *
 *          setContent {
 *              AndroidView(
 *                  modifier = Modifier.fillMaxSize(),
 *                  factory = { coreEngine.surfaceView }
 *              )
 *          }
 *      }
 * ```
 *
 * @param context the [Context] where the produced [SurfaceView] should be created.
 * @param assetManager the [AssetManager] provided by the activity or fragment.
 * @param initialProfile the initial [ThemeProfile] to which the core engine will enter.
 * @param initialMode the initial [ThemeMode] to which the core engine will enter.
 */
class CoreEngine(
    context: Context,
    assetManager: AssetManager,
    initialProfile: ThemeProfile = ThemeProfile.Firewater,
    initialMode: ThemeMode = ThemeMode.Light,
) : LifecycleEventObserver {
    /**
     * The rendering engine creates and holds a [SurfaceView] into which content of the scene is rendered. This view
     * can then be used by any activity or fragment to coordinate the desired layout.
     */
    val surfaceView = SurfaceView(context)

    // Manages SurfaceView
    private val uiHelper = UiHelper(ContextErrorPolicy.DONT_CHECK).apply {
        renderCallback = SurfaceCallback()
        attachTo(surfaceView)
    }

    // Manages the display
    private val displayHelper = DisplayHelper(context)

    // Schedules new frames
    private val choreographer = Choreographer.getInstance()

    // A renderer instance is tied to a single surface (SurfaceView, TextureView, etc.)
    private val renderer: Renderer

    // A scene holds all the renderable, lights, etc. to be drawn
    private val scene: Scene

    // A view defines a viewport, a scene and a camera for rendering
    private val view: View

    // The synthetic camera
    private val camera: Camera

    // Engine creates and destroys Filament resources
    // Each engine must be accessed from a single thread of your choosing
    // Resources cannot be shared across engines
    private val engine = Engine.create().also {
        renderer = it.createRenderer()
        scene = it.createScene()
        view = it.createView()
        camera = it.createCamera(it.entityManager.create())
    }

    // A swap chain is Filament's representation of a surface
    private var swapChain: SwapChain? = null

    // Performs the rendering and schedules new frames
    private val frameScheduler = FrameCallback()

    val animationManager = AnimationManager()

    private val meshManager = MeshManager()

    val themeManager = ThemeManager(scene, meshManager, initialProfile, initialMode)

    private val materialManager = MaterialManager(engine, assetManager)

    val cameraManager = CameraManager(camera, view, animationManager)

    val transformer: TransformManager
        get() = engine.transformManager

    init {
        setupView()
    }

    /**
     * Sets up [Camera], [Scene], and other [View]'s configurations.
     */
    private fun setupView() {
        // Set up skybox
        val color = themeManager.skyboxColor()
        // Skybox in short is the background environment of the rendering space
        scene.skybox = Skybox.Builder().color(
            color.red, color.green, color.blue, color.alpha
        ).build(engine)

        // Tell the view which camera we want to use
        view.camera = camera

        // Tell the view which scene we want to render
        view.scene = scene
    }

    fun render(visual: Visual): Int {
        val mesh = VisualAdapter.loadMesh(visual, engine, materialManager)
        meshManager.add(mesh)
        scene.addEntity(mesh.entity)
        return mesh.entity
    }

    /**
     * Should be called when the lifecycle of this [CoreEngine]'s owner transitions
     * to [Lifecycle.Event.ON_RESUME] state.
     */
    private fun resume() {
        Log.d(TAG, "lifecycle change: resume")
        choreographer.postFrameCallback(frameScheduler)
        animationManager.resume()
    }

    /**
     * Should be called when the lifecycle of this [CoreEngine]'s owner transitions
     * to [Lifecycle.Event.ON_PAUSE] state.
     */
    private fun pause() {
        Log.d(TAG, "lifecycle change: pause")
        animationManager.pause()
        choreographer.removeFrameCallback(frameScheduler)
    }

    /**
     * Should be called when the lifecycle of this [CoreEngine]'s owner transitions
     * to [Lifecycle.Event.ON_DESTROY] state.
     */
    private fun destroy() {
        Log.d(TAG, "lifecycle change: destroy")
        // Stop the animation and any pending frame
        animationManager.cancel()
        choreographer.removeFrameCallback(frameScheduler)

        // Always detach the surface before destroying the engine
        uiHelper.detach()

        // Cleans up mesh resources
        val meshes = meshManager.meshes
        meshes.forEach { (entity, indexBuffer, vertexBuffer, _, materials) ->
            engine.destroyEntity(entity)
            engine.destroyVertexBuffer(vertexBuffer)
            engine.destroyIndexBuffer(indexBuffer)
            materials.forEach { instance ->
                engine.destroyMaterialInstance(instance)
            }
        }

        // Clean up renderer
        engine.destroyRenderer(renderer)

        // Clean up materials
        materialManager.materials().forEach { engine.destroyMaterial(it) }

        // Clean up components
        engine.destroyView(view)
        engine.destroyScene(scene)
        engine.destroyCameraComponent(camera.entity)

        // Engine.destroyEntity() destroys Filament related resources only
        // (components), not the entity itself
        val entityManager = EntityManager.get()
        meshes.forEach { (entity, _, _, _, _) ->
            entityManager.destroy(entity)
        }
        entityManager.destroy(camera.entity)

        // Destroying the engine will free up any resource we may have forgotten
        // to destroy, but it's recommended to do the cleanup properly
        engine.destroy()
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_RESUME -> resume()
            Lifecycle.Event.ON_PAUSE -> pause()
            Lifecycle.Event.ON_DESTROY -> {
                destroy()
                source.lifecycle.removeObserver(this)
            }

            else -> return
        }
    }

    private inner class FrameCallback : Choreographer.FrameCallback {
        override fun doFrame(frameTimeNanos: Long) {
            // Schedule the next frame
            choreographer.postFrameCallback(this)

            // This check guarantees that we have a swap chain
            if (uiHelper.isReadyToRender) {
                // If beginFrame() returns false you should skip the frame
                // this means you are sending frames too quickly to the GPU
                if (renderer.beginFrame(swapChain ?: return, frameTimeNanos)) {
                    renderer.render(view)
                    renderer.endFrame()
                }
            }
        }
    }

    private inner class SurfaceCallback : UiHelper.RendererCallback {
        override fun onNativeWindowChanged(surface: Surface) {
            swapChain?.let { engine.destroySwapChain(it) }
            swapChain = engine.createSwapChain(surface, uiHelper.swapChainFlags)
            displayHelper.attach(renderer, surfaceView.display)
        }

        // Native surface went away, we must stop rendering
        override fun onDetachedFromSurface() {
            displayHelper.detach()
            swapChain?.let {
                engine.destroySwapChain(it)
                // Required to ensure we don't return before Filament is done executing the
                // destroySwapChain command, otherwise Android might destroy the Surface
                // too early
                engine.flushAndWait()
                swapChain = null
            }
        }

        override fun onResized(width: Int, height: Int) {
            val svp = CameraManager.STANDARD_VIEW_PORT
            if (width < height) {
                val ratio = height.toDouble() / width.toDouble()
                camera.setProjection(
                    Camera.Projection.ORTHO,
                    -svp, svp, -svp * ratio, svp * ratio, 0.0, 1.0
                )
            } else {
                val ratio = width.toDouble() / height.toDouble()
                camera.setProjection(
                    Camera.Projection.ORTHO,
                    -svp * ratio, svp * ratio, -svp, svp, 0.0, 1.0
                )
            }
            view.viewport = Viewport(0, 0, width, height)
        }
    }

    companion object {
        private const val TAG = "CoreEngine"
    }
}
