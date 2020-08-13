import com.jfrog.bintray.gradle.BintrayExtension.PackageConfig
import com.jfrog.bintray.gradle.BintrayExtension.VersionConfig
import java.io.FileNotFoundException
import java.io.IOException
import java.util.*

plugins {
    kotlin("jvm") version "1.3.72"

    `maven-publish`
    java

    id("com.github.johnrengelman.shadow") version "6.0.0"
    id("org.jetbrains.dokka") version "0.10.1"
    id("com.jfrog.bintray") version "1.8.5"
    id("io.franzbecker.gradle-lombok") version "3.2.0"
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
version = "1.0.3"

repositories {
    mavenLocal()
    mavenCentral()
    jcenter()

    maven("https://jitpack.io")
    maven("https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
}

dependencies {

    fun depend(dependency: Any) {
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

    depend("org.projectlombok:lombok:1.18.12")

    // kotlin coroutines
    depend("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.3.8")
    depend("org.jetbrains.kotlinx:kotlinx-coroutines-core-common:1.3.8")

    // the spigot api
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

    compileKotlin { kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString() }
    compileTestKotlin { kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString() }

    compileJava {
        options.encoding = "UTF-8"
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
    }
    compileTestJava {
        options.encoding = "UTF-8"
        sourceCompatibility = JavaVersion.VERSION_1_8.toString()
        targetCompatibility = JavaVersion.VERSION_1_8.toString()
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

    dokkaJar.run { }

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

            pom.withXml {
                val parent = this.asNode().appendNode("dependencies")

                configurations.compileOnly.allDependencies.forEach {
                    val node = parent.appendNode("dependency")

                    node.appendNode("groupId", it.group)
                    node.appendNode("artifactId", it.name)
                    node.appendNode("version", it.version)
                    node.appendNode("scope", "provided")
                }
            }

            artifactId = artifactName

            artifact(tasks.kotlinSourcesJar.get())
            artifact(dokkaJar)
        }
    }
}