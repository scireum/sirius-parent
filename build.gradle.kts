plugins {
    kotlin("jvm") version "1.8.21"
    id("com.gradle.plugin-publish") version "1.2.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("gradle-plugin"))
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
    website.set("https://scireum.de")
    vcsUrl.set("https://github.com/scireum/sirius-parent")

    plugins {
        create("siriusParent") {
            id = "com.scireum.sirius-parent"
            displayName = "SIRIUS Parent"
            description = "Provides basic setup and configuration for all SIRIUS libraries"
            implementationClass = "sirius.parent.SiriusParentPlugin"
            tags.set(listOf("scireum", "sirius", "setup", "parent"))
        }
    }
}
