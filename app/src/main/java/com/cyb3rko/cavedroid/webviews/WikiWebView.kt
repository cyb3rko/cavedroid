package com.cyb3rko.cavedroid.webviews

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.webkit.WebView

@SuppressLint("SetJavaScriptEnabled")
class WikiWebView : WebView {
    constructor(context: Context): super(context)
    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    init {
        settings.javaScriptEnabled = true
    }

    fun hidePageElements() {
        val elements = listOf(
            "navbar clearfix",
            "breadcrumbs row",
            "three columns wiki-sidebar",
            "social_icons",
            "copy",
        )
        elements.forEach {
            loadUrl("javascript:document.getElementsByClassName(\"$it\")[0]" +
                    ".setAttribute(\"style\",\"display:none;\");")
        }
    }
}