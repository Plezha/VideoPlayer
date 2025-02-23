package com.plezha.videoplayer.ui.videoplayer

import android.view.View
import android.webkit.WebChromeClient

class FullscreenableWebChromeClient(
    private val enterFullscreen: () -> Unit,
    private val exitFullscreen: () -> Unit,
) : WebChromeClient() {

    override fun onShowCustomView(view: View, callback: CustomViewCallback) {
        super.onShowCustomView(view, callback)
        enterFullscreen()
    }

    override fun onHideCustomView() {
        super.onHideCustomView()
        exitFullscreen()
    }

}