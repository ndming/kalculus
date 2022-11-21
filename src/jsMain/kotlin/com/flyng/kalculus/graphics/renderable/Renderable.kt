package com.flyng.kalculus.graphics.renderable

import com.flyng.kalculus.visual.Visual

actual interface Renderable {
    // TODO: Define interface so that JS's rendering engine can use to render components.
}

actual fun Visual.renderable() = object : Renderable {
    // TODO: Implements conversion from Visual to Renderable for JS target.
}
