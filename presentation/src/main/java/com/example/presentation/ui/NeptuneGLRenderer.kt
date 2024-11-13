package com.example.presentation.ui

import android.content.Context
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10

class NeptuneGLRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private lateinit var neptune: WaterSphere

    private val modelMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)  // Черный фон
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)  // Глубина для 3D эффекта

        // Создаем объект WaterSphere
        neptune = WaterSphere()
    }

    override fun onDrawFrame(gl: GL10?) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)
        setupCamera()

        // Получаем текущее время для анимации
        val currentTime = (System.currentTimeMillis() % 100000L) / 1000f  // время в секундах
        drawNeptune(currentTime)  // Передаем время в функцию отрисовки
    }

    private fun setupCamera() {
        // Позиция камеры
        val eyeX = 2f
        val eyeY = 5f
        val eyeZ = 2f
        // Устанавливаем точку обзора
        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, 0f, 0f, 0f, 0f, 1f, 0f)
    }

    private fun drawNeptune(currentTime: Float) {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, 0f, 0f, 0f)
        Matrix.rotateM(modelMatrix, 0, 40f, 0f, 1f, 0f)
        Matrix.scaleM(modelMatrix, 0, 1.5f, 1.5f, 1.5f)

        // Умножаем viewMatrix и modelMatrix для получения modelViewMatrix
        val modelViewMatrix = FloatArray(16)
        Matrix.multiplyMM(modelViewMatrix, 0, viewMatrix, 0, modelMatrix, 0)

        // Подготовка нормальной матрицы (для Фонговского освещения)
        val normalMatrix = FloatArray(16)
        Matrix.invertM(normalMatrix, 0, modelViewMatrix, 0)
        Matrix.transposeM(normalMatrix, 0, normalMatrix, 0)

        // Получаем итоговую матрицу modelViewProjectionMatrix
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewMatrix, 0)

        // Передаем в WaterSphere матрицу и текущее время
        neptune.draw(modelViewProjectionMatrix)
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)

        // Устанавливаем перспективу
        val aspectRatio = width.toFloat() / height.toFloat()
        Matrix.perspectiveM(projectionMatrix, 0, 45f, aspectRatio, 1f, 100f)
    }
}