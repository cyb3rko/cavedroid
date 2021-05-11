package com.cyb3rko.cavedroid;

import android.webkit.JavascriptInterface;

class JavascriptInterface {
    var html: String? = null

    @JavascriptInterface
    fun showHTML(_html: String) {
        html = _html
    }
}
