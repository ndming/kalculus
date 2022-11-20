package com.flyng.kalculus.renderable

import com.flyng.kalculus.core.manager.MaterialManager
import com.flyng.kalculus.core.manager.ThemeManager
import com.flyng.kalculus.renderable.mesh.Mesh
import com.google.android.filament.Engine

actual interface Renderable {
    fun loadMesh(engine: Engine, materialManager: MaterialManager, themeManager: ThemeManager): Mesh
}
