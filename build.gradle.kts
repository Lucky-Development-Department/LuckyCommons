plugins {
    kotlin("jvm") version "1.3.72"
    id("com.github.johnrengelman.shadow") version "6.0.0"
}

val artifact = "LuckyCommons"
group = "dev.luckynetwork.alviann.commons"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()

    maven("https://jitpack.io")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {

    fun depend(dependency: String) {
        this.compileOnly(dependency)
        this.testCompileOnly(dependency)
    }
    
    // annotations
    depend("org.jetbrains:annotations:19.0.0")
    // kotlin libraries
    depend("org.jetbrains.kotlin:kotlin-stdlib")
    depend("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    depend("org.jetbrains.kotlin:kotlin-stdlib-common")
    depend("org.jetbrains.kotlin:kotlin-reflect:1.3.72")

    // kotlin coroutines
    depend("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")
    depend("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.8")

    // the spigot api
    depend("org.spigotmc:spigot-api:1.8.8-R0.1-SNAPSHOT")
    depend("org.spigotmc:spigot-api:1.12.2-R0.1-SNAPSHOT")
    // the bungeecord api
    depend("net.md-5:bungeecord-api:1.16-R0.3")

    // extra libraries
    depend("com.github.Alviannn:LuckyInjector:1.6.2")
    depend("com.github.Alviannn:SQLHelper:2.5.6")
}

tasks {
    compileKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }
    compileTestKotlin {
        kotlinOptions.jvmTarget = "1.8"
    }

    jar {
        archiveFileName.set("$artifact-${project.version}.jar")
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}