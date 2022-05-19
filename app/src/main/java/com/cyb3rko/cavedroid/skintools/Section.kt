package com.cyb3rko.cavedroid.skintools

sealed class Section(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
) {
    class Head : Section(8, 8, 8, 8)
    class HeadOverlay : Section(40, 8, 8, 8)
}