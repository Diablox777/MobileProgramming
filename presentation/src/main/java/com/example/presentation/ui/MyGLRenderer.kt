package com.example.presentation.ui

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

class MyGLRenderer(private val context: Context) : GLSurfaceView.Renderer {

    private lateinit var sun: Sphere
    private lateinit var mercury: Sphere
    private lateinit var venus: Sphere
    private lateinit var earth: Sphere
    private lateinit var mars: Sphere
    private lateinit var jupiter: Sphere
    private lateinit var saturn: Sphere
    private lateinit var uranus: Sphere
    private lateinit var moon: Sphere
    private lateinit var galaxyBackground: Square
    private lateinit var saturnRing: Ring

    private val modelMatrix = FloatArray(16) //трансформация объектов
    private val projectionMatrix = FloatArray(16) //преобразование 3D координат в 2D
    private val viewMatrix = FloatArray(16) //позиционирование камеры
    private val modelViewProjectionMatrix = FloatArray(16) //матрица моедли, вида и проекции

    private val planetOrbitAngles = FloatArray(7) { 0f } // Для орбит планет
    private val planetRotationAngles = FloatArray(7) { 0f } // Для вращения планет вокруг своей оси
    private var moonOrbitAngle = 0f
    private var moonRotationAngle = 0f
    private var saturnRingRotationAngle = 0f

    private val rotationSpeed = 0.5f // Скорость вращения планет
    private val scope = CoroutineScope(Dispatchers.Main + Job()) //ассинхрон для каждой планеты


    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        // Установка цвета фона и включение Z-буфера
        GLES20.glClearColor(0f, 0f, 0f, 1f)
        //Z буффер для правильного рендеринга
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)

        // Загрузка текстур для Солнца, планет и Луны
        sun = Sphere(loadTexture(R.drawable.sun_texture))
        mercury = Sphere(loadTexture(R.drawable.mercury_texture))
        venus = Sphere(loadTexture(R.drawable.venus_texture))
        earth = Sphere(loadTexture(R.drawable.earth_texture))
        mars = Sphere(loadTexture(R.drawable.mars_texture))
        jupiter = Sphere(loadTexture(R.drawable.jupiter_texture_2))
        saturn = Sphere(loadTexture(R.drawable.saturn_texture))
        uranus = Sphere(loadTexture(R.drawable.uranus_texture))
        moon = Sphere(loadTexture(R.drawable.moon_texture))
        saturnRing = Ring(loadTexture(R.drawable.saturn_ring), outerRadius = 5f, innerRadius = 3.5f, segments = 64)
        galaxyBackground = Square(loadTexture(R.drawable.galaxy_texture))
    }

    override fun onDrawFrame(gl: GL10?) {
        // Очистка экрана и Z-буфера
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT or GLES20.GL_DEPTH_BUFFER_BIT)

        // Отрисовка фона галактики
        drawBackground()

        // Настройка камеры
        setupCamera()

        // Отрисовка Солнца
        drawSun()

        // Отрисовка вращающихся планет
        drawOrbitingPlanets()

        // Обновление углов орбит и вращения планет
        scope.launch {
            updateOrbitAndRotationAngles()
        }
    }

    private fun drawBackground() {
        // Отключаем буфер глубины для отрисовки фона
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)

        // Настраиваем матрицу модели для фона
        Matrix.setIdentityM(modelMatrix, 0)
        Matrix.scaleM(modelMatrix, 0, 20f, 40f, 0f) // Увеличиваем размер фона галактики
        Matrix.translateM(modelMatrix, 0, 0f, 0f, 0f) // Перемещаем фон вдаль по оси Z

        // Вычисляем итоговую матрицу
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewProjectionMatrix, 0)

        // Отрисовываем фон
        galaxyBackground.draw(modelViewProjectionMatrix)

        // Включаем буфер глубины обратно для отрисовки остальных объектов
        GLES20.glEnable(GLES20.GL_DEPTH_TEST)
    }



    private fun setupCamera() {
        // Камера выше по оси Y для лучшего обзора
        val eyeX = 0f
        val eyeY = 30f
        val eyeZ = 70f // Камера на удалении 70 единиц от Солнца
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
        drawPlanet(mars, 12f, planetOrbitAngles[3], planetRotationAngles[3], 0.6f)
        drawPlanet(jupiter, 15f, planetOrbitAngles[4], planetRotationAngles[4], 1.5f)
        drawSaturn(saturn, 20f, planetOrbitAngles[5], planetRotationAngles[5], 1.3f)
        drawSaturnRing()
        drawPlanet(uranus, 25f, planetOrbitAngles[6], planetRotationAngles[6], 1.1f)
    }

    private fun drawSaturnRing() {
        Matrix.setIdentityM(modelMatrix, 0)

        // Получаем текущее положение Сатурна для синхронизации кольца с его положением
        val saturnX = 20f * cos(Math.toRadians(planetOrbitAngles[5].toDouble())).toFloat()
        val saturnZ = 20f * sin(Math.toRadians(planetOrbitAngles[5].toDouble())).toFloat()

        Matrix.translateM(modelMatrix, 0, saturnX, 0f, saturnZ)

        //Matrix.rotateM(modelMatrix, 0, 45f, 1f, 1f, 0f)
        Matrix.rotateM(modelMatrix, 0, saturnRingRotationAngle, 0f, 0f, 1f)
        Matrix.scaleM(modelMatrix, 0, 0.5f, 0.5f, 0.5f)

        Matrix.multiplyMM(modelViewProjectionMatrix, 0, viewMatrix, 0, modelMatrix, 0)
        Matrix.multiplyMM(modelViewProjectionMatrix, 0, projectionMatrix, 0, modelViewProjectionMatrix, 0)

        saturnRing.draw(modelViewProjectionMatrix)
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
        planetOrbitAngles[3] += 0.5f // Марс
        planetOrbitAngles[4] += 0.2f // Юпитер
        planetOrbitAngles[5] += 0.1f // Сатурн
        planetOrbitAngles[6] += 0.07f // Уран

        // Обновляем углы вращения планет
        planetRotationAngles[0] += rotationSpeed
        planetRotationAngles[1] += -rotationSpeed
        planetRotationAngles[2] += rotationSpeed
        planetRotationAngles[3] += rotationSpeed
        planetRotationAngles[4] += rotationSpeed
        planetRotationAngles[5] += rotationSpeed
        planetRotationAngles[6] += rotationSpeed
        saturnRingRotationAngle += rotationSpeed


        // Обновляем углы орбиты и вращения Луны
        moonOrbitAngle += 1.5f // Скорость вращения Луны
        moonRotationAngle += rotationSpeed
    }


    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        //область, где будет отрисовываться графика
        GLES20.glViewport(0, 0, width, height)

        //соотношение сторон
        val aspectRatio = width.toFloat() / height.toFloat()
        //матрица проекции из 3D в 2D
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