group = "com.scireum"
version = "1.0-RC7"

plugins {
    kotlin("jvm") version "1.7.10"
    id("com.gradle.plugin-publish") version "1.0.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib", "1.7.10"))
    implementation(kotlin("gradle-plugin", "1.7.10"))
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
