package com.flyng.kalculus.core.manager

import com.flyng.kalculus.renderable.Renderable
import com.flyng.kalculus.renderable.bridge
import com.flyng.kalculus.visual.Visual

class VisualManager {
    private val visuals = mutableMapOf<Visual, Renderable>()

    fun add(visual: Visual) {
        visuals[visual] = visual.bridge()
    }
}