package com.cyb3rko.cavedroid;

import android.webkit.JavascriptInterface;

public class JavaScriptInterface {
    public String html;

    @JavascriptInterface
    public void showHTML(String _html) {
        html = _html;
    }
}
