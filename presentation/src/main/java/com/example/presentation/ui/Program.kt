package com.example.presentation.ui

import android.opengl.GLES20

object Program {
    var programId: Int = 0

    fun initShaders(vertexShaderCode: String, fragmentShaderCode: String) {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        programId = GLES20.glCreateProgram().apply {
            GLES20.glAttachShader(this, vertexShader)
            GLES20.glAttachShader(this, fragmentShader)
            GLES20.glLinkProgram(this)
        }
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).apply {
            GLES20.glShaderSource(this, shaderCode)
            GLES20.glCompileShader(this)
        }
    }
}