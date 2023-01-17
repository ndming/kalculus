package com.flyng.kalculus.io

import android.content.res.AssetManager
import javax.xml.parsers.DocumentBuilderFactory

object SvgLoader {
    fun fromAssets(am: AssetManager, file: String) = am.open(file).use {
        val factory = DocumentBuilderFactory.newInstance()
        val builder = factory.newDocumentBuilder()
        val doc = builder.parse(it)

        val width = doc.documentElement.getAttribute("width").toFloat()
        val height = doc.documentElement.getAttribute("height").toFloat()
        val path = doc.getElementsByTagName("path").item(0).attributes.getNamedItem("d").nodeValue

        SvgData(width, height, path)
    }
}