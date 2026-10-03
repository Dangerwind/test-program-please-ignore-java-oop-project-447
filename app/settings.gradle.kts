rootProject.name = "app"

// Позволяет Gradle самому скачать JDK 25, если на машине его нет.
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}