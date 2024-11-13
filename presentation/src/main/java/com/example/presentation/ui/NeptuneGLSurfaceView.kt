package com.example.presentation.ui

import android.content.Context
import android.opengl.GLSurfaceView

class NeptuneGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: NeptuneGLRenderer

    init {
        setEGLContextClientVersion(2)
        renderer = NeptuneGLRenderer(context)
        setRenderer(renderer)
    }
}