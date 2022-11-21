package com.flyng.kalculus.graphics.renderable

import com.flyng.kalculus.visual.Visual

expect interface Renderable

expect fun Visual.renderable(): Renderable
