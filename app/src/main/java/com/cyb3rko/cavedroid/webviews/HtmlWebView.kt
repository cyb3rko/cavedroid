package com.cyb3rko.cavedroid.webviews

import android.annotation.SuppressLint
import android.content.Context
import android.webkit.WebView
import com.cyb3rko.cavedroid.webviews.JavascriptInterface

@SuppressLint("SetJavaScriptEnabled")
class HtmlWebView(context: Context) : WebView(context) {
    val javascriptInterface: JavascriptInterface

    init {
        settings.javaScriptEnabled = true
        javascriptInterface = JavascriptInterface()
        addJavascriptInterface(javascriptInterface, "HtmlViewer")
    }

    fun fetchHmtl() = loadUrl("javascript:window.HtmlViewer.showHTML('<head>'+document.getElementsByTagName('html')[0].innerHTML+'</head>');")
}