plugins {
    id("java-library")
    id("org.jetbrains.kotlin.jvm") version "1.9.0" // Убедитесь, что версия Kotlin соответствует
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}
