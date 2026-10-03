plugins {
    java
    id("com.diffplug.spotless") version "8.10.3"
}

group = "hexlet.code"
version = "0.0.1"

repositories {
    mavenCentral()
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.1.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    // В Gradle 9 запуск без единого теста считается ошибкой. Пока библиотека
    // не написана, тестов нет, поэтому отключаем проверку.
    failOnNoDiscoveredTests = false
}

tasks.jar {
    manifest {
        attributes["Implementation-Title"] = "SQL query builder"
        attributes["Implementation-Version"] = project.version
    }
}

spotless {
    java {
        // google-java-format работает через внутреннее API javac, поэтому версия
        // должна поддерживать ту JDK, на которой запущен сам Gradle. Демон
        // закреплён на JDK 25 в gradle/gradle-daemon-jvm.properties, а 1.37.0
        // на ней уже не работает, поэтому берём последнюю проверенную версию.
        googleJavaFormat("1.36.1")
    }
}