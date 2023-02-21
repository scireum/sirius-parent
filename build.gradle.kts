plugins {
    kotlin("jvm") version "1.8.10"
    id("com.gradle.plugin-publish") version "1.1.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib", "1.7.10"))
    implementation(kotlin("gradle-plugin", "1.7.10"))
}

// Set up publishing details for the custom scireum maven repository
publishing {
    repositories {
        maven {
            credentials {
                username = System.getenv("MAVEN_USERNAME")
                password = System.getenv("MAVEN_PASSWORD")
            }
            name = "scireum-mvn"
            url = uri(providers.gradleProperty("mvnRepository").getOrElse("not_set"))
        }
    }
}

// Set up publishing details for the official Gradle plugin portal
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
