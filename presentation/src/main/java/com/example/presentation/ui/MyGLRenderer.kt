package com.example.presentation.ui

import BlackHole
import android.content.Context
import android.graphics.BitmapFactory
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import android.opengl.Matrix
import com.example.presentation.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

class MyGLRenderer(private val context: Context, ) : GLSurfaceView.Renderer {

    private lateinit var sun: Sphere
    private lateinit var mercury: Sphere
    private lateinit var venus: Sphere
    private lateinit var earth: Sphere
    private lateinit var mars: Sphere
    private lateinit var jupiter: Sphere
    private lateinit var saturn: Sphere
    private lateinit var uranus: Sphere
    private lateinit var neptune: Sphere
    private lateinit var moon: Sphere
    private lateinit var galaxyBackground: Square
    private lateinit var saturnRing: Ring
    private lateinit var transparentCube: Cube

    private lateinit var blackHole: BlackHole


    private val modelMatrix = FloatArray(16)
    private val projectionMatrix = FloatArray(16)
    private val viewMatrix = FloatArray(16)
    private val modelViewProjectionMatrix = FloatArray(16)

    private val planetOrbitAngles = FloatArray(9) { 0f } // Для орбит планет
    private val planetRotationAngles = FloatArray(9) { 0f } // Для вращения планет вокруг своей оси
    private var moonOrbitAngle = 0f
    private var moonRotationAngle = 0f
    private var saturnRingRotationAngle = 0f

    private var blackHolePositionX = -30f
    private var blackHolePositionY = 0f

    private val rotationSpeed = 0.5f // Скорость вращения планет
    private val scope = CoroutineScope(Dispatchers.Main + Job())

    private var currentPlanetIndex = 2
    private lateinit var planets: List<Pair<String, Sphere>>

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        sun = Sphere(loadTexture(R.drawable.sun_texture))
        mercury = Sphere(loadTexture(R.drawable.mercury_texture))
        venus = Sphere(loadTexture(R.drawable.venus_texture))
        earth = Sphere(loadTexture(R.drawable.earth_texture))
        mars = Sphere(loadTexture(R.drawable.mars_texture))
        jupiter = Sphere(loadTexture(R.drawable.jupiter_texture))
        saturn = Sphere(loadTexture(R.drawable.saturn_texture))
        uranus = Sphere(loadTexture(R.drawable.uranus_texture))
        neptune = Sphere(loadTexture(R.drawable.neptune_texture))
        moon = Sphere(loadTexture(R.drawable.moon_texture))
        saturnRing = Ring(loadTexture(R.drawable.saturn_texture), 5f, 3.5f, 64)
        galaxyBackground = Square(loadTexture(R.drawable.galaxy_texture))
        transparentCube = Cube()

        blackHole = BlackHole(context, R.drawable.hole)

        planets = listOf(
            "Mercury" to mercury,
            "Venus" to venus,
            "Earth" to earth,
            "Moon" to moon,
            "Mars" to mars,
            "Jupiter" to jupiter,
            "Saturn" to saturn,
            "Uranus" to uranus,
            "Neptune" to neptune

        )
    }

    override fun onDrawFrame(gl: GL10?) {
        // Очистка экрана и Z-буфера
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        drawBackground()
        setupCamera()
        drawSun()
        drawOrbitingPlanets()
        drawTransparentCube()

        drawBlackHole()

        scope.launch {
            updateOrbitAndRotationAngles()
        }
    }

    fun returnPlanetIndex(): Int {
        return currentPlanetIndex;
    }
    fun selectNextPlanet() {
        currentPlanetIndex = (currentPlanetIndex + 1) % planets.size
    }

    fun selectPreviousPlanet() {
        currentPlanetIndex = if (currentPlanetIndex == 0) planets.size - 1 else currentPlanetIndex - 1
    }





    private fun drawTransparentCube() {
        Matrix.setIdentityM(modelMatrix, 0)

        if (currentPlanetIndex == 3) {
            val earthX = getPlanetRadius(2) * cos(Math.toRadians(planetOrbitAngles[2].toDouble())).toFloat()
            val earthZ = getPlanetRadius(2) * sin(Math.toRadians(planetOrbitAngles[2].toDouble())).toFloat()
            val moonX = earthX + 1.5f * cos(Math.toRadians(moonOrbitAngle.toDouble())).toFloat()
            val moonZ = earthZ + 1.5f * sin(Math.toRadians(moonOrbitAngle.toDouble())).toFloat()
            val moonY = 1.5f * sin(Math.toRadians(moonOrbitAngle.toDouble())).toFloat()
            Matrix.translateM(modelMatrix, 0, moonX, moonY, moonZ)
            Matrix.scaleM(modelMatrix, 0, 0.6f, 0.6f, 0.6f)
        } else {
            val planetX = getPlanetRadius(currentPlanetIndex) * cos(Math.toRadians(planetOrbitAngles[currentPlanetIndex].toDouble())).toFloat()
            val planetZ = getPlanetRadius(currentPlanetIndex) * sin(Math.toRadians(planetOrbitAngles[currentPlanetIndex].toDouble())).toFloat()
            val scale = getPlanetScale(currentPlanetIndex)
            Matrix.translateM(modelMatrix, 0, planetX, scale / 4f, planetZ)
            Matrix.scaleM(modelMatrix, 0, scale * 2f, scale * 2f, scale * 2f)
        }

        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewProjectionMatrix, 0)
        transparentCube.draw(modelViewProjectionMatrix, floatArrayOf(1f, 1f, 1f, 0.3f))
    }


    // Пример функции для получения масштаба планеты
    private fun getPlanetScale(planetIndex: Int): Float {
        return when (planetIndex) {
            0 -> 0.5f  // Меркурий
            1 -> 0.7f  // Венера
            2 -> 0.8f  // Земля
            4 -> 0.6f  // Марс
            5 -> 1.5f  // Юпитер
            6 -> 1.3f  // Сатурн
            7 -> 1.1f  // Уран
            8 -> 0.5f
            else -> 1.0f
        }
    }

    private fun getPlanetRadius(planetIndex:Int):Float{
        return when (planetIndex) {
            0 -> 3f // Меркурий
            1 -> 5f // Венера
            2 -> 8f // Земля
            4 -> 12f // Марс
            5 -> 15f // Юпитер
            6 -> 20f // Сатурн
            7 -> 25f // Уран
            8 -> 28f
            else -> 0f
        }
    }


    private fun drawBackground() {
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.scaleM(modelMatrix, 0, 20f, 40f, 0f)
        Matrix.translateM(modelMatrix, 0, 0f, 0f, 0f)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewProjectionMatrix, 0)
        galaxyBackground.draw(modelViewProjectionMatrix)
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }



    private fun setupCamera() {
        val eyeX = 2f
        val eyeY = 20f
        val eyeZ = 70f
        Matrix.setLookAtM(viewMatrix, 0, eyeX, eyeY, eyeZ, 0f, 0f, 0f, 0f, 1f, 0f)
    }

    private fun drawSun() {
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewProjectionMatrix, 0)
        sun.draw(modelViewProjectionMatrix)
    }

    private fun drawOrbitingPlanets() {

        drawPlanet(mercury, 3f, planetOrbitAngles[0], planetRotationAngles[0], 0.5f)
        drawPlanet(venus, 5f, planetOrbitAngles[1], -planetRotationAngles[1], 0.7f)
        drawPlanet(earth, 8f, planetOrbitAngles[2], planetRotationAngles[2], 0.8f)
        drawMoon(moon, earth, 1.5f, moonOrbitAngle, moonRotationAngle, 0.3f)
        drawPlanet(mars, 12f, planetOrbitAngles[4], planetRotationAngles[4], 0.6f)
        drawPlanet(jupiter, 15f, planetOrbitAngles[5], planetRotationAngles[5], 1.5f)
        drawSaturn(saturn, 20f, planetOrbitAngles[6], planetRotationAngles[6], 1.3f)
        drawSaturnRing()
        drawPlanet(uranus, 25f, planetOrbitAngles[7], planetRotationAngles[7], 1.1f)
        drawPlanet(neptune, 28f, planetOrbitAngles[8], planetRotationAngles[8], 0.5f)
    }

    private fun drawSaturnRing() {
        Matrix.setIdentityM(modelMatrix, 0)
        val x = 20f * cos(Math.toRadians(planetOrbitAngles[6].toDouble())).toFloat()
        val z = 20f * sin(Math.toRadians(planetOrbitAngles[6].toDouble())).toFloat()
        Matrix.translateM(modelMatrix, 0, x, 0f, z)
        Matrix.rotateM(modelMatrix, 0, saturnRingRotationAngle, 0f, 0f, 1f)
        Matrix.scaleM(modelMatrix, 0, 0.5f, 0.5f, 0.5f)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewProjectionMatrix, 0)
        saturnRing.draw(modelViewProjectionMatrix)
    }

    private fun drawBlackHole() {
        // Медленное движение черной дыры с плавным изменением координат
        blackHolePositionX += 0.05f // Медленное перемещение по X
        blackHolePositionY += 0.01f // Медленное перемещение по Y

        // Сброс позиции черной дыры при выходе за пределы экрана
        if (blackHolePositionX > 30f) {
            blackHolePositionX = -40f
            blackHolePositionY = -5f
        }

        // Устанавливаем матрицу трансформации для черной дыры
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, blackHolePositionX, blackHolePositionY, -70f) // Позиция черной дыры позади сцены
        Matrix.rotateM(modelMatrix, 0, 35f, 0f, 1f, 0f) // Угол 90 градусов для перпендикулярности взгляду камеры
        Matrix.scaleM(modelMatrix, 0, 4f, 4f, 4f) // Размер черной дыры

        // Объединение матриц и отрисовка черной дыры
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewProjectionMatrix, 0)
        blackHole.draw(modelViewProjectionMatrix, -5f)
    }

    private fun drawMoon(moon: Sphere, earth: Sphere, orbitRadius: Float, orbitAngle: Float, rotationAngle: Float, scale: Float) {
        // Положение Земли
        val earthX = 8f * cos(Math.toRadians(planetOrbitAngles[2].toDouble())).toFloat()
        val earthZ = 8f * sin(Math.toRadians(planetOrbitAngles[2].toDouble())).toFloat()

        // Положение Луны:
        // moonX и moonZ остаются прежними, moonY будет вычисляться как функция от orbitAngle для создания вертикального вращения
        val moonX = earthX + orbitRadius * cos(Math.toRadians(orbitAngle.toDouble())).toFloat()
        val moonY = orbitRadius * sin(Math.toRadians(orbitAngle.toDouble())).toFloat()
        val moonZ = earthZ + orbitRadius * sin(Math.toRadians(orbitAngle.toDouble())).toFloat()

        // Установка матрицы для Луны
        Matrix.setIdentityM(modelMatrix, 0)

        // Перемещение Луны
        Matrix.translateM(modelMatrix, 0, moonX, moonY, moonZ)

        // Вращение Луны вокруг своей оси (если нужно)
        Matrix.rotateM(modelMatrix, 0, rotationAngle, 0f, 1f, 0f)

        // Масштабирование Луны
        Matrix.scaleM(modelMatrix, 0, scale, scale, scale)

        // Итоговая матрица для Луны
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewProjectionMatrix, 0)

        // Рисуем Луну
        moon.draw(modelViewProjectionMatrix)
    }


    private fun drawPlanet(planet: Sphere, orbitRadius: Float, orbitAngle: Float, rotationAngle: Float, scale: Float) {
        val planetX = orbitRadius * cos(Math.toRadians(orbitAngle.toDouble())).toFloat()
        val planetZ = orbitRadius * sin(Math.toRadians(orbitAngle.toDouble())).toFloat()

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, planetX, 0f, planetZ)
        Matrix.rotateM(modelMatrix, 0, rotationAngle, 0f, 1f, 0f)
        Matrix.scaleM(modelMatrix, 0, scale, scale, scale)

        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewProjectionMatrix, 0)

        planet.draw(modelViewProjectionMatrix)
    }

    private fun drawSaturn(planet: Sphere, orbitRadius: Float, orbitAngle: Float, rotationAngle: Float, scale: Float) {
        val planetX = orbitRadius * cos(Math.toRadians(orbitAngle.toDouble())).toFloat()
        val planetZ = orbitRadius * sin(Math.toRadians(orbitAngle.toDouble())).toFloat()

        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.translateM(modelMatrix, 0, planetX, 0f, planetZ)
        Matrix.rotateM(modelMatrix, 0, rotationAngle, -0.3f, 1f, 0f)
        Matrix.scaleM(modelMatrix, 0, scale, scale, scale)

        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewProjectionMatrix, 0)

        planet.draw(modelViewProjectionMatrix)
    }

    private fun updateOrbitAndRotationAngles() {
        // Обновляем углы орбит планет
        planetOrbitAngles[0] += 1.2f // Меркурий
        planetOrbitAngles[1] += 0.9f // Венера
        planetOrbitAngles[2] += 0.6f // Земля
        planetOrbitAngles[4] += 0.5f // Марс
        planetOrbitAngles[5] += 0.2f // Юпитер
        planetOrbitAngles[6] += 0.1f // Сатурн
        planetOrbitAngles[7] += 0.07f // Уран
        planetOrbitAngles[8] += 0.1f

        // Обновляем углы вращения планет
        planetRotationAngles[0] += rotationSpeed
        planetRotationAngles[1] += -rotationSpeed
        planetRotationAngles[2] += rotationSpeed
        planetRotationAngles[4] += rotationSpeed
        planetRotationAngles[5] += rotationSpeed
        planetRotationAngles[6] += rotationSpeed
        planetRotationAngles[7] += rotationSpeed
        saturnRingRotationAngle += rotationSpeed


        // Обновляем углы орбиты и вращения Луны
        moonOrbitAngle += 1.5f // Скорость вращения Луны
        moonRotationAngle += rotationSpeed
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