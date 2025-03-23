plugins {
    id("java")
    id("io.papermc.paperweight.userdev") version "2.0.0-beta.16"
    id("xyz.jpenilla.run-paper") version "2.3.1"
    id("com.gradleup.shadow") version "8.3.6"
}

group = "com.github.mertunctuncer"
version = "0.1.0-SNAPSHOT"
description = "A simple player vault plugin."

java {
    toolchain.languageVersion = JavaLanguageVersion.of(21)
}

repositories {
    mavenCentral()
}

dependencies {
    paperweight.paperDevBundle("1.21.4-R0.1-SNAPSHOT")
    implementation("org.xerial:sqlite-jdbc:3.49.1.0")
    implementation("com.zaxxer:HikariCP:5.1.0")
}
tasks {
    compileJava {
        options.release = 21
    }

    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
}