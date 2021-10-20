package com.cyb3rko.cavedroid.skintools

import android.graphics.Bitmap

fun Bitmap.extractSection(section: Section) = Bitmap.createBitmap(this, section.x, section.y, section.width, section.height)