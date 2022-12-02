package com.flyng.kalculus.core.manager

import android.content.res.AssetManager
import android.util.Log
import com.flyng.kalculus.BuildConfig
import com.flyng.kalculus.exposition.visual.primitive.MaterialLocal
import com.flyng.kalculus.graphics.renderable.material.MaterialAsset
import com.google.android.filament.Engine
import com.google.android.filament.Material
import java.nio.ByteBuffer
import java.nio.channels.Channels
import kotlin.reflect.full.createInstance

/**
 * Manages all compiled [Material] invovled in the lifecycle of the application.
 */
class MaterialManager(engine: Engine, assetManager: AssetManager) {
    private val materials = mutableMapOf<String, Material>()

    init {
        MaterialLocal::class.sealedSubclasses.map { it.createInstance() }.forEach { ml ->
            val filamat = readUncompressedAsset(assetManager, MaterialAsset.filamat(ml.name)).let { data ->
                Material.Builder().payload(data, data.remaining()).build(engine)
            }
            materials[ml.name] = filamat
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "add new material: ${ml.name}")
            }
        }
    }

    /**
     * Obtains the compiled [Material] associated with the [materialLocal]. It is guaranteed to always have a
     * pre-compiled material available for any [MaterialLocal].
     */
    operator fun get(materialLocal: MaterialLocal) = materials[materialLocal.name]!!

    /**
     * Gets the sequence of compiled [Material]s this manager is holding.
     */
    fun materials() = materials.values.asSequence()

    private fun readUncompressedAsset(
        manager: AssetManager,
        pathToAsset: String
    ) = manager.openFd(pathToAsset).use { fd ->
        val input = fd.createInputStream()
        val dst = ByteBuffer.allocate(fd.length.toInt())

        val src = Channels.newChannel(input)
        src.read(dst)
        src.close()

        dst.apply { rewind() }
    }

    companion object {
        private const val TAG = "CoreEngineMaterialManager"
    }
}