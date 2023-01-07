package com.flyng.kalculus.core.manager

import com.flyng.kalculus.graphics.renderable.mesh.Mesh
import com.google.android.filament.Entity
import com.google.android.filament.MaterialInstance

class MeshManager {
    private val _meshes = mutableListOf<Mesh>()

    val meshes: List<Mesh>
        get() = _meshes

    val materialInstances: List<MaterialInstance>
        get() = _meshes.flatMap { mesh -> mesh.materials }

    fun add(mesh: Mesh) {
        _meshes.add(mesh)
    }

    operator fun get(@Entity entity: Int) = _meshes.find { it.entity == entity }
}