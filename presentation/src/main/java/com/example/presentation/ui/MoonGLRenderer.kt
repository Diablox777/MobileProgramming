package com.example.presentation.ui

import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.presentation.R
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class MoonGLRenderer(private val context: Context, ) : GLSurfaceView.Renderer {

    private lateinit var moon: Sphere
    private lateinit var moonSphere: PhongSphere

    private val modelMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)

    private val lightPos = FloatArray(4)
    private val viewPos = FloatArray(3)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        moon = Sphere(loadTexture(R.drawable.moon_texture))
        moonSphere = PhongSphere(loadTexture(R.drawable.moon_texture))

        setupLight()

    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        setupCamera()
        drawMoon()
    }


    private fun drawBackground() {
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.scaleM(modelMatrix, 0, 20f, 40f, 0f)
        Matrix.translateM(modelMatrix, 0, 0f, 0f, 0f)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewProjectionMatrix, 0)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }



    private fun setupCamera() {
        val eyeX = 2f
        val eyeY = 5f
        val eyeZ = 2f
        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, 0f, 0f, 0f, 0f, 1f, 0f)
    }

    private fun drawMoon() {

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0f, 0f, 0f)
        Matrix.rotateM(modelMatrix, 0, 40f, 0f, 1f, 0f)
        Matrix.scaleM(modelMatrix, 0, 1.5f, 1.5f, 1.5f)

        val modelViewMatrix = FloatArray(16)
        Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0)

        val normalMatrix = FloatArray(16)
        Matrix.invertM(normalMatrix, 0, modelViewMatrix, 0)
        Matrix.transposeM(normalMatrix, 0, normalMatrix, 0)

        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0)

        // Рисуем сферу с Фонговским освещением
        moonSphere.draw(
            modelViewProjectionMatrix,
            normalMatrix,
            lightPos,
            viewPos
        )
    }

    private fun setupLight() {
        lightPos[0] = 5f
        lightPos[1] = -8f
        lightPos[2] = -5f

        viewPos[0] = 2f
        viewPos[1] = -5f
        viewPos[2] = -2f
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        val aspectRatio = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 45f, aspectRatio, 1f, 100f)
    }

    private fun loadTexture(resourceId: Int): Int {
        val textureIds = IntArray(1)
        GLES20.glGenTextures(1, textureIds, 0)

        val options = BitmapFactory.Options()
        options.inScaled = false

        val bitmap = BitmapFactory.decodeResource(context.resources, resourceId, options)

        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureIds[0])
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR)
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR)

        android.opengl.GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, bitmap, 0)
        bitmap.recycle()

        return textureIds[0]
    }
}