package com.flyng.kalculus.core.manager

import com.flyng.kalculus.graphics.renderable.Renderable
import com.flyng.kalculus.graphics.renderable.renderable
import com.flyng.kalculus.visual.Visual

class VisualManager {
    private val visuals = mutableMapOf<Visual, Renderable>()

    fun add(visual: Visual) {
        visuals[visual] = visual.renderable()
    }
}