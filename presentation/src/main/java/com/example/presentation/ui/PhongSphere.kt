package com.example.presentation.ui

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class PhongSphere(
    private val textureId: Int,
    private val radius: Float = 1.0f,
    private val stacks: Int = 30,
    private val slices: Int = 30
) {
    private val vertexBuffer: FloatBuffer
    private val normalBuffer: FloatBuffer
    private val textureBuffer: FloatBuffer
    private val program: Int
    private val numVertices: Int

    init {
        val vertices = mutableListOf<Float>()
        val normals = mutableListOf<Float>()
        val textureCoords = mutableListOf<Float>()

        // Генерация вершин, нормалей и текстурных координат
        for (i in 0 until stacks) {
            val lat0 = PI * (-0.5 + i.toDouble() / stacks)
            val z0 = sin(lat0).toFloat() * radius
            val zr0 = cos(lat0).toFloat() * radius

            val lat1 = PI * (-0.5 + (i + 1).toDouble() / stacks)
            val z1 = sin(lat1).toFloat() * radius
            val zr1 = cos(lat1).toFloat() * radius

            for (j in 0..slices) {
                val lng = 2 * PI * j.toDouble() / slices
                val x = cos(lng).toFloat()
                val y = sin(lng).toFloat()

                // Вершины
                vertices.add(x * zr0)
                vertices.add(y * zr0)
                vertices.add(z0)

                vertices.add(x * zr1)
                vertices.add(y * zr1)
                vertices.add(z1)

                // Нормали
                normals.add(x * zr0 / radius)
                normals.add(y * zr0 / radius)
                normals.add(z0 / radius)

                normals.add(x * zr1 / radius)
                normals.add(y * zr1 / radius)
                normals.add(z1 / radius)

                // Текстурные координаты
                textureCoords.add(j.toFloat() / slices)
                textureCoords.add(1 - (i.toFloat() / stacks))

                textureCoords.add(j.toFloat() / slices)
                textureCoords.add(1 - ((i + 1).toFloat() / stacks))
            }
        }

        numVertices = vertices.size / 3

        vertexBuffer = createFloatBuffer(vertices)
        normalBuffer = createFloatBuffer(normals)
        textureBuffer = createFloatBuffer(textureCoords)

        val vertexShaderCode = """
            uniform mat4 uMVPMatrix;
            uniform mat4 uNormalMatrix;
            uniform vec3 uLightPos;
            uniform vec3 uCameraPos;
            attribute vec4 vPosition;
            attribute vec3 aNormal;
            attribute vec2 aTexCoord;
            varying vec2 vTexCoord;
            varying vec3 vNormalInterp;
            varying vec3 vPos;

            void main() {
                vPos = vec3(uMVPMatrix * vPosition);
                vNormalInterp = normalize(vec3(uNormalMatrix * vec4(aNormal, 0.0)));
                gl_Position = uMVPMatrix * vPosition;
                vTexCoord = aTexCoord;
            }
        """.trimIndent()

        val fragmentShaderCode = """
            precision mediump float;
            varying vec2 vTexCoord;
            varying vec3 vNormalInterp;
            varying vec3 vPos;
            uniform sampler2D uTexture;
            uniform vec3 uLightPos;
            uniform vec3 uCameraPos;
            uniform vec3 uAmbientColor;
            uniform vec3 uDiffuseColor;
            uniform vec3 uSpecularColor;
            uniform float uShininess;

            void main() {
                vec3 normal = normalize(vNormalInterp);
                vec3 lightDir = normalize(uLightPos - vPos);
                vec3 viewDir = normalize(uCameraPos - vPos);
                vec3 reflectDir = reflect(-lightDir, normal);

                vec3 ambient = uAmbientColor;

                float diff = max(dot(normal, lightDir), 0.0);
                vec3 diffuse = diff * uDiffuseColor;

                float spec = pow(max(dot(viewDir, reflectDir), 0.0), uShininess);
                vec3 specular = spec * uSpecularColor;
                vec4 texColor = texture2D(uTexture, vTexCoord);

                vec3 finalColor = ambient + diffuse * texColor.rgb + specular;
                gl_FragColor = vec4(finalColor, texColor.a);
            }
        """.trimIndent()

        // Компиляция шейдеров и создание программы
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        program = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
    }

    private fun createFloatBuffer(data: List<Float>): FloatBuffer {
        val byteBuffer = ByteBuffer.allocateDirect(data.size * 4).apply {
            order(ByteOrder.nativeOrder())
        }
        return byteBuffer.asFloatBuffer().apply {
            put(data.toFloatArray())
            position(0)
        }
    }

    fun draw(mvpMatrix: FloatArray, normalMatrix: FloatArray, lightPos: FloatArray, cameraPos: FloatArray) {
        GLES20.glUseProgram(program)

        // Вершины
        val positionHandle = GLES20.glGetAttribLocation(program, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer)

        // Нормали
        val normalHandle = GLES20.glGetAttribLocation(program, "aNormal")
        GLES20.glEnableVertexAttribArray(normalHandle)
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 12, normalBuffer)

        // Текстурные координаты
        val texCoordHandle = GLES20.glGetAttribLocation(program, "aTexCoord")
        GLES20.glEnableVertexAttribArray(texCoordHandle)
        GLES20.glVertexAttribPointer(texCoordHandle, 2, GLES20.GL_FLOAT, false, 8, textureBuffer)

        // Установка матриц и освещения
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, "uMVPMatrix"), 1, false, mvpMatrix, 0)
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(program, "uNormalMatrix"), 1, false, normalMatrix, 0)
        GLES20.glUniform3fv(GLES20.glGetUniformLocation(program, "uLightPos"), 1, lightPos, 0)
        GLES20.glUniform3fv(GLES20.glGetUniformLocation(program, "uCameraPos"), 1, cameraPos, 0)

        // Настройка цветов освещения
        GLES20.glUniform3f(GLES20.glGetUniformLocation(program, "uAmbientColor"), 0.1f, 0.1f, 0.1f)
        GLES20.glUniform3f(GLES20.glGetUniformLocation(program, "uDiffuseColor"), 1.0f, 1.0f, 1.0f)
        GLES20.glUniform3f(GLES20.glGetUniformLocation(program, "uSpecularColor"), 1.0f, 1.0f, 1.0f)
        GLES20.glUniform1f(GLES20.glGetUniformLocation(program, "uShininess"), 64.0f) // Увеличено для большего блеска

        // Текстура
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0)
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId)
        GLES20.glUniform1i(GLES20.glGetUniformLocation(program, "uTexture"), 0)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, numVertices)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(normalHandle)
        GLES20.glDisableVertexAttribArray(texCoordHandle)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }
}
