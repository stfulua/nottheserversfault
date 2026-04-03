plugins {
    `java-library`
    id("com.gradleup.shadow") version "8.3.5"
}

group = "xyz.vprolabs.nottheserversfault"
version = "1.1.0"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
    options.compilerArgs.addAll(listOf("-Xlint:deprecation", "-Xlint:unchecked", "-Xlint:all", "-parameters"))
}

repositories {
    mavenCentral()
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-public/")
}

dependencies {
    compileOnly("org.spigotmc:spigot-api:1.21.1-R0.1-SNAPSHOT")
    compileOnly("com.github.retrooper:packetevents-spigot:2.11.2")
    compileOnly("org.popcraft:chunky-common:1.4.10")
}

tasks {
    shadowJar {
        archiveFileName.set("NotTheServersFault-1.1.0.jar")
        
        minimize()
        
        exclude("META-INF/maven/**")
        exclude("META-INF/proguard/**")
        exclude("META-INF/versions/**")
        exclude("LICENSE", "NOTICE", "about.html")
    }

    build {
        dependsOn(shadowJar)
    }
}
