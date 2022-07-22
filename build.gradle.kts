group = "com.scireum"
version = "1.0-RC"

plugins {
    kotlin("jvm") version "1.6.21"
    id("java-gradle-plugin")
    id("maven-publish")
    id("com.gradle.plugin-publish") version "0.11.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib", "1.6.21"))
    implementation(kotlin("gradle-plugin", "1.6.21"))
}

gradlePlugin {
    plugins {
        create("siriusParent") {
            id = "com.scireum.sirius-parent"
            displayName = "SIRIUS Parent"
            description = "Provides basic setup and configuration for all SIRIUS libraries"
            implementationClass = "sirius.parent.SiriusParentPlugin"
        }
    }
}

pluginBundle {
    website = "https://scireum.de"
    vcsUrl = "https://github.com/scireum/sirius-parent"
    tags = listOf("scireum", "sirius", "setup", "parent")
}
