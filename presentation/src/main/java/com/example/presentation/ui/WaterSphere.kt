package com.example.presentation.ui

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer

class WaterSphere {

    private val vertexShaderCode = """
        uniform mat4 uMVPMatrix;
        uniform float uTime;

        attribute vec4 aPosition;
        attribute vec3 aNormal;
        varying vec3 vNormal;
        varying vec3 vPosition;

        void main() {
            float wave1 = sin(aPosition.x * 10.0 + uTime * 2.0) * 0.05;
            float wave2 = cos(aPosition.z * 12.0 + uTime * 1.7) * 0.04;
            float wave3 = sin((aPosition.x + aPosition.z) * 15.0 + uTime * 2.5) * 0.03;
            
            float totalWaveHeight = wave1 + wave2 + wave3;
            vec4 position = aPosition;
            position.y += totalWaveHeight;

            vNormal = aNormal;
            vPosition = vec3(position);
            gl_Position = uMVPMatrix * position;
        }
    """

    private val fragmentShaderCode = """
        precision mediump float;
        varying vec3 vNormal;
        varying vec3 vPosition;
        
        uniform float uTime;
        
        // Свет и цвет
        const vec3 lightDir = normalize(vec3(0.5, 1.0, 0.3));
        const vec3 lightColor = vec3(1.0, 1.0, 1.0);
        const vec3 waterColor = vec3(0.0, 0.3, 0.6);

        void main() {
            // Расчет нормализованной нормали и направления к источнику света
            vec3 normal = normalize(vNormal);
            vec3 viewDir = normalize(-vPosition);

            // Диффузное освещение
            float diff = max(dot(normal, lightDir), 0.0);
            vec3 diffuse = waterColor * diff;

            // Спекулярное освещение (блик)
            vec3 reflectDir = reflect(-lightDir, normal);
            float spec = pow(max(dot(viewDir, reflectDir), 0.0), 32.0); // 32.0 - показатель резкости блика
            vec3 specular = lightColor * spec * 0.5; // коэффициент блика

            // Эффект глубины, основанный на высоте волн
            float depthEffect = 0.05 * sin(vPosition.y * 15.0 + uTime * 0.5);
            vec3 depthWaterColor = waterColor + vec3(0.0, depthEffect, depthEffect * 0.6);
            vec3 finalColor = mix(depthWaterColor, diffuse, diff) + specular;

            gl_FragColor = vec4(finalColor, 1.0);
        }
    """

    private var program: Int
    private val vertexBuffer: FloatBuffer

    init {
        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)
        program = GLES20.glCreateProgram().apply {
            GLES20.glAttachShader(this, vertexShader)
            GLES20.glAttachShader(this, fragmentShader)
            GLES20.glLinkProgram(this)
        }

        // Увеличение детализации для более гладкой анимации волн
        val sphereVertices = createSphereVertices(latitudeBands = 60, longitudeBands = 60)
        vertexBuffer = ByteBuffer.allocateDirect(sphereVertices.size * 4)
            .order(ByteOrder.nativeOrder())
            .asFloatBuffer()
            .put(sphereVertices)
        vertexBuffer.position(0)
    }

    fun draw(mvpMatrix: FloatArray) {
        GLES20.glUseProgram(program)

        val positionHandle = GLES20.glGetAttribLocation(program, "aPosition")
        val normalHandle = GLES20.glGetAttribLocation(program, "aNormal")
        val mvpMatrixHandle = GLES20.glGetUniformLocation(program, "uMVPMatrix")
        val timeHandle = GLES20.glGetUniformLocation(program, "uTime")

        GLES20.glUniformMatrix4fv(mvpMatrixHandle, 1, false, mvpMatrix, 0)
        GLES20.glUniform1f(timeHandle, (System.currentTimeMillis() % 100000L) / 1000f)

        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(positionHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        GLES20.glEnableVertexAttribArray(normalHandle)
        GLES20.glVertexAttribPointer(normalHandle, 3, GLES20.GL_FLOAT, false, 0, vertexBuffer)

        GLES20.glDrawArrays(GLES20.GL_TRIANGLES, 0, vertexBuffer.capacity() / 3)

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(normalHandle)
    }

    private fun loadShader(type: Int, shaderCode: String): Int {
        return GLES20.glCreateShader(type).also { shader ->
            GLES20.glShaderSource(shader, shaderCode)
            GLES20.glCompileShader(shader)
        }
    }

    private fun createSphereVertices(radius: Float = 1.0f, latitudeBands: Int = 60, longitudeBands: Int = 60): FloatArray {
        val vertices = mutableListOf<Float>()

        for (latNumber in 0..latitudeBands) {
            val theta = latNumber * Math.PI / latitudeBands
            val sinTheta = Math.sin(theta)
            val cosTheta = Math.cos(theta)

            for (longNumber in 0..longitudeBands) {
                val phi = longNumber * 2 * Math.PI / longitudeBands
                val sinPhi = Math.sin(phi)
                val cosPhi = Math.cos(phi)

                val x = (cosPhi * sinTheta).toFloat()
                val y = cosTheta.toFloat()
                val z = (sinPhi * sinTheta).toFloat()

                vertices.add(x * radius)
                vertices.add(y * radius)
                vertices.add(z * radius)
            }
        }

        val sphereVertices = mutableListOf<Float>()
        for (latNumber in 0 until latitudeBands) {
            for (longNumber in 0 until longitudeBands) {
                val first = (latNumber * (longitudeBands + 1)) + longNumber
                val second = first + longitudeBands + 1

                sphereVertices.add(vertices[3 * first])
                sphereVertices.add(vertices[3 * first + 1])
                sphereVertices.add(vertices[3 * first + 2])

                sphereVertices.add(vertices[3 * second])
                sphereVertices.add(vertices[3 * second + 1])
                sphereVertices.add(vertices[3 * second + 2])

                sphereVertices.add(vertices[3 * (first + 1)])
                sphereVertices.add(vertices[3 * (first + 1) + 1])
                sphereVertices.add(vertices[3 * (first + 1) + 2])

                sphereVertices.add(vertices[3 * second])
                sphereVertices.add(vertices[3 * second + 1])
                sphereVertices.add(vertices[3 * second + 2])

                sphereVertices.add(vertices[3 * (second + 1)])
                sphereVertices.add(vertices[3 * (second + 1) + 1])
                sphereVertices.add(vertices[3 * (second + 1) + 2])

                sphereVertices.add(vertices[3 * (first + 1)])
                sphereVertices.add(vertices[3 * (first + 1) + 1])
                sphereVertices.add(vertices[3 * (first + 1) + 2])
            }
        }

        return sphereVertices.toFloatArray()
    }
}