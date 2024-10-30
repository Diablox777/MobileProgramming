package com.example.presentation.ui

import android.content.Context
import android.opengl.GLSurfaceView

class MoonGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: MoonGLRenderer

    init {
        setEGLContextClientVersion(2)
        renderer = MoonGLRenderer(context)
        setRenderer(renderer)
    }
}