group = "com.scireum"
version = "1.0-SNAPSHOT"

plugins {
    kotlin("jvm") version "1.3.70"
    id("java-gradle-plugin")
    id("maven-publish")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib", "1.3.70"))
    implementation(kotlin("gradle-plugin", "1.3.70"))
}

gradlePlugin {
    plugins {
        create("syncIdeaSettings") {
            id = "sirius-parent"
            implementationClass = "sirius.parent.SyncIdeaSettingsPlugin"
        }
    }
}
