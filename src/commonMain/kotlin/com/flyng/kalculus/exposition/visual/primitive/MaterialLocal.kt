package com.flyng.kalculus.exposition.visual.primitive

import com.flyng.kalculus.exposition.visual.Visual
import com.flyng.kalculus.exposition.visual.vertex.ColorAttribute
import com.flyng.kalculus.exposition.visual.vertex.Vertex
import com.flyng.kalculus.exposition.visual.vertex.VertexAttribute

/**
 * Enumerate all pre-compiled materials bundled with the application. The entry names of this enum exactly match the
 * name registered for `.mat` materials.
 */
@Suppress("EnumEntryName")
enum class MaterialLocal {
    /**
     * This material supports a static baseColor color that can be set only once. The color data is expected to
     * interleave with other [VertexAttribute]s. Any implementation of the [Visual] interface that uses this material
     * should include [ColorAttribute] in the [Vertex.data] list.
     */
    plain_color,

    /**
     * This material supports a single baseColor color that can be set at runtime. The color data is expected to set
     * during the material instance construction phase and later on can be modified thorugh the `baseColor` parameter
     * retrieved from the same material instance. For this reason, the [Vertex] data of [Visual]s that use this material
     * is not required to include [ColorAttribute].
     */
    dynamic_color
}