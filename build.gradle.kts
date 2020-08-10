import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig
import com.jfrog.bintray.gradle.BintrayExtension.VersionConfig
import java.util.*
import java.io.*

plugins {
    kotlin("jvm") version "1.3.72"

    `maven-publish`
    java

    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("org.jetbrains.dokka") version "0.10.1"
    id("com.jfrog.bintray") version "1.8.5"
}

val artifactName = "LuckyCommons"
val publicationName = "bintray"
val projectDescription = "The commonly used codes in Lucky Network projects"

/** provided the bintray deploy config/credentials */
val deployConfig = Properties()
    get() {
        if (field.isEmpty) {
            val file = File("$projectDir/deploy.properties")
            if (!file.exists())
                throw FileNotFoundException("Cannot find ${file.name}!")

            try {
                file.bufferedReader().use {
                    field.load(it)
                }
            } catch (e: Exception) {
                throw IOException("Cannot load ${file.name}!")
            }
        }

        return field
    }

group = "dev.luckynetwork.alviann.commons"
version = "1.0.1"

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
    depend("org.jetbrains.kotlin:kotlin-stdlib:1.3.72")
    depend("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.3.72")
    depend("org.jetbrains.kotlin:kotlin-stdlib-common:1.3.72")
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

/** creates the docs jar */
val dokkaJar by tasks.creating(Jar::class) {
    group = JavaBasePlugin.DOCUMENTATION_GROUP
    description = "Assembles Kotlin docs with Dokka"
    archiveClassifier.set("javadoc")
    from(tasks.dokka)
}

tasks {

    compileKotlin { kotlinOptions.jvmTarget = "1.8" }
    compileTestKotlin { kotlinOptions.jvmTarget = "1.8" }

    compileJava {
        options.encoding = "UTF-8"
    }

    jar {
        archiveFileName.set("$artifactName-${project.version}.jar")

        from(project.projectDir) {
            include("LICENSE.txt")
            into("META-INF")
        }

        manifest {
            attributes(
                mapOf(
                    "Author" to "Alviann",
                    "Organization" to "Lucky Network",
                    "Team" to "Lucky Development Department"
                )
            )
        }
    }

    kotlinSourcesJar { }

    dokkaJar.run {  }

}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
    targetCompatibility = JavaVersion.VERSION_1_8
}

bintray {
    user = deployConfig.getProperty("bintray.username")
    key = deployConfig.getProperty("bintray.api-key")

    override = true
    publish = true
    setPublications(publicationName)

    pkg(delegateClosureOf<PackageConfig> {
        repo = "maven"

        name = "LuckyCommons"
        setLicenses("AGPL-V3")

        vcsUrl = "https://github.com/Lucky-Development-Department/LuckyCommons.git"
        websiteUrl = "https://github.com/Lucky-Development-Department/"
        githubRepo = "Lucky-Development-Department/LuckyCommons"

        setLabels("library", "commons", "lucky network", "public")
        desc = projectDescription

        publicDownloadNumbers = true

        version(delegateClosureOf<VersionConfig> {
            name = "v${project.version}"
            desc = ""
            released = Date().toString()
            vcsTag = project.version.toString()
        })
    })
}

@Suppress("DEPRECATION")
publishing {
    publications {
        create<MavenPublication>(publicationName) {
            from(components["java"])

            artifactId = artifactName

            artifact(tasks.kotlinSourcesJar.get())
            artifact(dokkaJar)
        }
    }
}