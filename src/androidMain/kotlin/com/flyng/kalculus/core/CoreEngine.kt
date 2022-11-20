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
import com.flyng.kalculus.BuildConfig
import com.flyng.kalculus.core.manager.MaterialManager
import com.flyng.kalculus.core.manager.ThemeManager
import com.flyng.kalculus.renderable.bridge
import com.flyng.kalculus.renderable.mesh.Mesh
import com.flyng.kalculus.theme.ThemeMode
import com.flyng.kalculus.theme.ThemeProfile
import com.flyng.kalculus.visual.Visual
import com.google.android.filament.*
import com.google.android.filament.android.DisplayHelper
import com.google.android.filament.android.UiHelper
import com.google.android.filament.android.UiHelper.ContextErrorPolicy

/**
 * The core rendering engine, backed by [Filament]. The core should be created when the owner activity or fragment into
 * which graphics is rendered enters [Lifecycle.State.CREATED] state.
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
 *          coreEngine = CoreEngine(context = this, owner = this, assetManager = assets)
 *      }
 * ```
 *
 * The engine will create an instance of [SurfaceView] which can then be used to embed to Android's View layout or
 * `AndroidView` composable. The engine will also automatically bind to the provided lifecycle owner so no further
 * lifecycle-binding steps are required.
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
 * Note that this version of the engine will be destroyed and recreated with the same lifecycle sequence of the host
 * Activity/Fragment.
 *
 * @param context the [Context] where the produced [SurfaceView] should be created.
 * @param owner the [LifecycleOwner] to which the lifecycle of the engine will bind.
 * @param assetManager the [AssetManager] provided by the activity or fragment.
 * @param initialProfile the initial [ThemeProfile] to which state the core engine will enter.
 * @param initialMode the initial [ThemeMode] to which state the core engine will enter.
 */
class CoreEngine(
    context: Context,
    owner: LifecycleOwner,
    assetManager: AssetManager,
    initialProfile: ThemeProfile = ThemeProfile.Firewater,
    initialMode: ThemeMode = ThemeMode.Light,
) {
    /**
     * The rendering engine creates and holds a [SurfaceView] into which content of the scene is rendered. This view
     * can then be used by any activity or fragment to coordinate the desired layout.
     */
    val surfaceView = SurfaceView(context)

    // Schedules new frames
    private val choreographer = Choreographer.getInstance()

    // Manages SurfaceView
    private val uiHelper = UiHelper(ContextErrorPolicy.DONT_CHECK).apply {
        renderCallback = SurfaceCallback()
        attachTo(surfaceView)
    }

    // Manages the display
    private val displayHelper = DisplayHelper(context)

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

    private val themeManager = ThemeManager(initialProfile, initialMode)

    private val meshes = mutableListOf<Mesh>()

    private val materialManager = MaterialManager(engine, assetManager)

    init {
        setupView()

        // bound the lifecycle of the engine to the lifecycle of the owner
        if (owner.lifecycle.currentState != Lifecycle.State.DESTROYED) {
            owner.lifecycle.addObserver(object : LifecycleEventObserver {
                override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
                    if (source == owner) {
                        when (event) {
                            Lifecycle.Event.ON_RESUME -> resume()
                            Lifecycle.Event.ON_PAUSE -> pause()
                            Lifecycle.Event.ON_DESTROY -> {
                                destroy()
                                owner.lifecycle.removeObserver(this)
                            }

                            else -> return
                        }
                    }
                }
            })
        }
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

    /**
     * Call this function when the global [ThemeProfile] of the application change.
     * @param profile the new [ThemeProfile] to update.
     */
    fun setThemeProfile(profile: ThemeProfile) {
        scene.skybox?.let { skybox ->
            themeManager.setThemeProfile(profile, skybox, meshes.flatMap { mesh -> mesh.materials })
        }
    }

    /**
     * Call this function when the global [ThemeMode] of the application change.
     * @param mode the new [ThemeMode] to update.
     */
    fun setThemeMode(mode: ThemeMode) {
        scene.skybox?.let { skybox ->
            themeManager.setThemeMode(mode, skybox, meshes.flatMap { mesh -> mesh.materials })
        }
    }

    fun render(visual: Visual) {
        val mesh = visual.bridge().loadMesh(engine, materialManager, themeManager)
        meshes.add(mesh)
        scene.addEntity(mesh.entity)
    }

    /**
     * Should be called when the lifecycle of this [CoreEngine]'s owner transitions
     * to [Lifecycle.Event.ON_RESUME] state.
     */
    internal fun resume() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "lifecycle change: resume")
        }
        choreographer.postFrameCallback(frameScheduler)
    }

    /**
     * Should be called when the lifecycle of this [CoreEngine]'s owner transitions
     * to [Lifecycle.Event.ON_PAUSE] state.
     */
    internal fun pause() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "lifecycle change: pause")
        }
        choreographer.removeFrameCallback(frameScheduler)
    }

    /**
     * Should be called when the lifecycle of this [CoreEngine]'s owner transitions
     * to [Lifecycle.Event.ON_DESTROY] state.
     */
    internal fun destroy() {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "lifecycle change: destroy")
        }

        // Stop the animation and any pending frame
        choreographer.removeFrameCallback(frameScheduler)

        // Always detach the surface before destroying the engine
        uiHelper.detach()

        // Cleans up mesh resources
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
            val standard = 1.0
            val zoomOut = 5.0
            if (width < height) {
                val ratio = height.toDouble() / width.toDouble()
                camera.setProjection(
                    Camera.Projection.ORTHO,
                    -standard * zoomOut, standard * zoomOut,
                    -standard * ratio * zoomOut, standard * ratio * zoomOut,
                    0.0, 10.0
                )
            } else {
                val ratio = width.toDouble() / height.toDouble()
                camera.setProjection(
                    Camera.Projection.ORTHO,
                    -standard * ratio * zoomOut, standard * ratio * zoomOut,
                    -standard * zoomOut, standard * zoomOut,
                    0.0, 10.0
                )
            }
            view.viewport = Viewport(0, 0, width, height)
        }
    }

    companion object {
        private const val TAG = "CoreEngine"
    }
}
