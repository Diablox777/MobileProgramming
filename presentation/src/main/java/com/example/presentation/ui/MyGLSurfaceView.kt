package com.example.presentation.ui

import android.content.Context
import android.opengl.GLSurfaceView

class MyGLSurfaceView(context: Context) : GLSurfaceView(context) {
    private val renderer: MyGLRenderer

    init {
        setEGLContextClientVersion(2)
        renderer = MyGLRenderer(context)
        setRenderer(renderer)
    }

    fun selectNextPlanet():Int {

            renderer.selectNextPlanet()

        return renderer.returnPlanetIndex()
    }

    fun selectPreviousPlanet():Int {

            renderer.selectPreviousPlanet()


        return renderer.returnPlanetIndex()
    }


}
