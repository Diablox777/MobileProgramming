package com.example.presentation.ui

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

open class Sphere(private val textureId: Int, private val radius: Float = 1.0f, private val stacks: Int = 30, private val slices: Int = 30) {

    private val vertexBuffer: FloatBuffer
    private val textureBuffer: FloatBuffer
    private val program: Int
    private var numVertices: Int

    init {
        val vertices = ArrayList<Float>()
        val textureCoords = ArrayList<Float>()

        for (i in 0 until stacks) {
            val lat0 = PI * (-0.5 + i.toDouble() / stacks)
            val z0 = sin(lat0).toFloat() * radius
            val zr0 = cos(lat0).toFloat() * radius

            val lat1 = PI * (-0.5 + (i + 1).toDouble() / stacks)
            val z1 = sin(lat1).toFloat() * radius
            val zr1 = cos(lat1).toFloat() * radius

            for (j in 0 until slices) {
                val lng = 2 * PI * j.toDouble() / slices
                val x = cos(lng).toFloat()
                val y = sin(lng).toFloat()

                vertices.add(x * zr0)
                vertices.add(y * zr0)
                vertices.add(z0)

                vertices.add(x * zr1)
                vertices.add(y * zr1)
                vertices.add(z1)

                textureCoords.add(j.toFloat() / slices)
                textureCoords.add(1 - (i.toFloat() / stacks))

                textureCoords.add(j.toFloat() / slices)
                textureCoords.add(1 - ((i + 1).toFloat() / stacks))

            }
        }

        numVertices = vertices.size / 3

        val bb = ByteBuffer.allocateDirect(vertices.size * 4)
        bb.order(ByteOrder.nativeOrder())
        vertexBuffer = bb.asFloatBuffer().apply {
            put(vertices.toFloatArray())
            position(0)
        }

        val tb = ByteBuffer.allocateDirect(textureCoords.size * 4)
        tb.order(ByteOrder.nativeOrder())
        textureBuffer = tb.asFloatBuffer().apply {
            put(textureCoords.toFloatArray())
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


        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, numVertices)

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