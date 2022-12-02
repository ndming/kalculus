package com.flyng.kalculus.exposition.visual.primitive

import com.flyng.kalculus.exposition.visual.Visual
import com.flyng.kalculus.exposition.visual.vertex.ColorAttribute
import com.flyng.kalculus.exposition.visual.vertex.Vertex
import com.flyng.kalculus.exposition.visual.vertex.VertexAttribute

/**
 * Keeps track of all pre-compiled materials bundled with the application. The property [name] of the class matches
 * the name registered for `.mat` materials.
 */
sealed class MaterialLocal(val name: String)

/**
 * This material supports a static baseColor color that can be set only once. The color data is expected to
 * interleave with other [VertexAttribute]s. Any implementation of the [Visual] interface that uses this material
 * should include [ColorAttribute] in the [Vertex.data] list.
 */
class PlainColor : MaterialLocal("plain_color")

/**
 * This material supports a single [baseColor] color that can be set at runtime. The color data is set during the
 * material instance construction phase and later on can be modified thorugh the `baseColor` parameter retrieved from
 * the same material instance. The color is expected to be in SRGB space.
 */
class DynamicColor(val baseColor: Color = Color.default) : MaterialLocal("dynamic_color")
