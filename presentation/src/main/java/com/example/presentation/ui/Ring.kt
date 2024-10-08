package com.example.presentation.ui

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.cos
import kotlin.math.sin

class Ring(private val textureId: Int, private val outerRadius: Float, private val innerRadius: Float, private val segments: Int) {

    private val vertexBuffer: FloatBuffer
    private val textureBuffer: FloatBuffer
    private val program: Int

    init {
        val coords = generateRingCoords()
        val textureCoords = generateTextureCoords()

        val bb = ByteBuffer.allocateDirect(coords.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer().apply {
            put(coords)
            position(0)
        }

        val tb = ByteBuffer.allocateDirect(textureCoords.size * 4)
        tb.order(ByteOrder.nativeOrder())
        textureBuffer = tb.asFloatBuffer().apply {
            put(textureCoords)
            position(0)
        }

        val vertexShaderCode = """
            uniform mat4 uMVPMatrix;
            attribute vec4 vPosition;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            void main() {
                gl_Position = uMVPMatrix * vPosition;
                vTexCoord = aTexCoord;
            }
        """.trimIndent()

        val fragmentShaderCode = """
            precision mediump float;
            varying vec2 vTexCoord;
            uniform sampler2D uTexture;
            void main() {
                gl_FragColor = texture2D(uTexture, vTexCoord);
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

    private fun generateRingCoords(): FloatArray {
        val coords = mutableListOf<Float>()
        val angleStep = 2 * Math.PI / segments

        for (i in 0..segments) {
            val angle = i * angleStep
            val cosAngle = cos(angle).toFloat()
            val sinAngle = sin(angle).toFloat()

            coords.add(cosAngle * outerRadius)
            coords.add(sinAngle * outerRadius)
            coords.add(0.0f)

            coords.add(cosAngle * innerRadius)
            coords.add(sinAngle * innerRadius)
            coords.add(0.0f)
        }

        return coords.toFloatArray()
    }

    private fun generateTextureCoords(): FloatArray {
        val textureCoords = mutableListOf<Float>()
        val angleStep = 2 * Math.PI / segments

        for (i in 0..segments) {
            val angle = i * angleStep
            val cosAngle = cos(angle).toFloat()
            val sinAngle = sin(angle).toFloat()

            textureCoords.add((cosAngle * 0.5f) + 0.5f)
            textureCoords.add((sinAngle * 0.5f) + 0.5f)

            textureCoords.add((cosAngle * 0.25f) + 0.5f)
            textureCoords.add((sinAngle * 0.25f) + 0.5f)
        }

        return textureCoords.toFloatArray()
    }

    fun draw(mvpMatrix: FloatArray) {
        GLES20.glUseProgram(program)

        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)

        val texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 8, textureBuffer)

        val mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)

        val textureHandle = GLES20.glGetUniformLocation(program, "uTexture")
        GLES20.glUniform1i(textureHandle, 0)

        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, (segments + 1) * 2)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }
}
