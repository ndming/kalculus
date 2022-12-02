package com.flyng.kalculus.graphics.renderable

import com.flyng.kalculus.core.manager.MaterialManager
import com.flyng.kalculus.exposition.visual.Visual
import com.flyng.kalculus.graphics.renderable.mesh.Mesh
import com.google.android.filament.Engine

actual interface Renderable {
    fun loadMesh(visual: Visual, engine: Engine, materialManager: MaterialManager): Mesh
}
