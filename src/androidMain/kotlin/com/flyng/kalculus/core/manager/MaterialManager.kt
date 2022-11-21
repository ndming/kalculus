package com.flyng.kalculus.core.manager

import android.content.res.AssetManager
import android.util.Log
import com.flyng.kalculus.BuildConfig
import com.flyng.kalculus.graphics.renderable.material.MaterialAsset
import com.flyng.kalculus.visual.primitive.MaterialLocal
import com.google.android.filament.Engine
import com.google.android.filament.Material
import java.nio.ByteBuffer
import java.nio.channels.Channels

class MaterialManager(engine: Engine, assetManager: AssetManager) {
    private val materials = mutableMapOf<String, Material>()

    init {
        MaterialLocal.values().forEach { material ->
            val filamat = readUncompressedAsset(
                assetManager, MaterialAsset.filamat(material.name)
            ).let { data ->
                Material.Builder().payload(data, data.remaining()).build(engine)
            }
            materials[material.name] = filamat
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "add new material: ${material.name}")
            }
        }
    }

    operator fun get(material: MaterialLocal) = materials[material.name]!!

    fun materials() = materials.values.toList()

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