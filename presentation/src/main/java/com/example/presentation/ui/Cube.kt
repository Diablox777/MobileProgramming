package com.example.presentation.ui

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer

class Cube {

    private val vertexBuffer: FloatBuffer
    private val indexBuffer: ShortBuffer
    private val program: Int

    private val coords = floatArrayOf(
        -0.5f,  0.5f,  0.5f,
        -0.5f, -0.5f,  0.5f,
        0.5f, -0.5f,  0.5f,
        0.5f,  0.5f,  0.5f,
        -0.5f,  0.5f, -0.5f,
        -0.5f, -0.5f, -0.5f,
        0.5f, -0.5f, -0.5f,
        0.5f,  0.5f, -0.5f
    )

    private val indices = shortArrayOf(
        0, 1, 2, 0, 2, 3,
        4, 5, 6, 4, 6, 7,
        0, 3, 7, 0, 7, 4,
        1, 2, 6, 1, 6, 5,
        0, 1, 5, 0, 5, 4,
        3, 2, 6, 3, 6, 7
    )

    init {
        val bb = ByteBuffer.allocateDirect(coords.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer().apply {
            put(coords)
            position(0)
        }

        val ib = ByteBuffer.allocateDirect(indices.size * 2)
        ib.order(ByteOrder.nativeOrder())
        indexBuffer = ib.asShortBuffer().apply {
            put(indices)
            position(0)
        }

        val vertexShaderCode = """
            uniform mat4 uMVPMatrix;
            attribute vec4 vPosition;
            void main() {
                gl_Position = uMVPMatrix * vPosition;
            }
        """.trimIndent()

        val fragmentShaderCode = """
            precision mediump float;
            uniform vec4 vColor;  // Цвет, включая прозрачность
            void main() {
                gl_FragColor = vColor;  // Используем переданный цвет  
            }
        """.trimIndent()

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
    }

    fun draw(mvpMatrix: FloatArray, color: FloatArray) {
        GLES20.glUseProgram(program)

        // Получаем и устанавливаем позицию вершин
        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)

        // Передаем матрицу преобразований в шейдер
        val mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        // Передаем цвет в шейдер
        val colorHandle = GLES20.glGetUniformLocation(program, "vColor")
        GLES20.glUniform4fv(colorHandle, 1, color, 0)

        // Включаем режим смешивания для прозрачности
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        // Рисуем куб
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, indices.size, GLES20.GL_UNSIGNED_SHORT, indexBuffer)

        // Отключаем атрибуты после отрисовки
        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisable(GLES20.GL_BLEND)  // Отключаем режим смешивания после отрисовки
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }
}