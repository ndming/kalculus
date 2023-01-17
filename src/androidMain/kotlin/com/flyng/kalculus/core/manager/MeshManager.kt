package com.flyng.kalculus.core.manager

import com.flyng.kalculus.exposition.visual.primitive.ColorType
import com.flyng.kalculus.graphics.renderable.mesh.Mesh
import com.google.android.filament.Entity
import com.google.android.filament.MaterialInstance

class MeshManager {
    private val _meshes = mutableListOf<Mesh>()

    val meshes: List<Mesh>
        get() = _meshes


    val instances: List<Pair<MaterialInstance, ColorType>>
        get() = _meshes.flatMap { mesh -> mesh.instances }

    fun add(mesh: Mesh) {
        _meshes.add(mesh)
    }

    fun remove(@Entity entity: Int) {
        _meshes.removeIf { it.entity == entity }
    }

    operator fun get(@Entity entity: Int) = _meshes.find { it.entity == entity }
}