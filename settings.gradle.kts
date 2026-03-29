pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
        maven("https://repo.papermc.io/repository/maven-public/")
    }

    plugins {
        id("io.papermc.paperweight.userdev") version "1.7.1"
        id("com.gradleup.shadow") version "8.3.5"
        kotlin("jvm") version "2.0.0"
    }
}

rootProject.name = "NotTheServersFault"
