package com.flyng.kalculus.io

import android.content.res.AssetManager
import android.content.res.Resources
import androidx.annotation.RawRes
import javax.xml.parsers.DocumentBuilderFactory

object SvgLoader {
    fun fromAssets(assets: AssetManager, file: String) = assets.open(file).use {
        val document = builder.parse(it)

        val dimens = document
            .getElementsByTagName("svg")
            .item(0)
            .attributes
            .getNamedItem("viewBox")
            .nodeValue.split(" ")

        val width  = dimens[2].toFloat()
        val height = dimens[3].toFloat()

        val path = document
            .getElementsByTagName("path")
            .item(0)
            .attributes
            .getNamedItem("d")
            .nodeValue

        SvgData(width, height, path)
    }

    fun fromResources(res: Resources, @RawRes id: Int) = res.openRawResource(id).use {
        val document = builder.parse(it)

        val vectorAttrs = document
            .getElementsByTagName("vector")
            .item(0)
            .attributes

        val width = vectorAttrs
            .getNamedItem("android:viewportWidth")
            .nodeValue
            .toFloat()

        val height = vectorAttrs
            .getNamedItem("android:viewportHeight")
            .nodeValue.toFloat()

        val path = document
            .getElementsByTagName("path")
            .item(0)
            .attributes
            .getNamedItem("android:pathData")
            .nodeValue

        SvgData(width, height, path)

    }

    private val builder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
}