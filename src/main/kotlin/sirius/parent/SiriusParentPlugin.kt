@file:Suppress("unused")

package sirius.parent

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.GroovySourceDirectorySet
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.testing.Test
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.jvm.tasks.Jar
import org.gradle.testing.jacoco.tasks.JacocoReport
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

open class SiriusParentPluginExtension {
    var ideaSettingsUri = "https://raw.githubusercontent.com/scireum/sirius-parent/master/src/main/resources"
}

class SiriusParentPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("siriusParent", SiriusParentPluginExtension::class.java)

        project.repositories.apply {
            add(project.repositories.mavenLocal())
            add(project.repositories.maven {
                it.url = URI("https://mvn.scireum.com")
            })
            add(project.repositories.mavenCentral())
        }

        applyGradlePlugins(project)

        setupTestDependencies(project)
        setupTestDependenciesForJUnit4(project)

        // set source directories for groovy compilation
        val testSourceSet =
            project.extensions.getByType(SourceSetContainer::class.java).getByName(SourceSet.TEST_SOURCE_SET_NAME)
        testSourceSet.extensions.getByType(GroovySourceDirectorySet::class.java)
            .setSrcDirs(listOf("src/test/groovy", "src/test/java"))
        // Configure source set for Kotlin tests
        testSourceSet.java.setSrcDirs(listOf("src/test/kotlin"))

        project.tasks.apply {
            withType(KotlinCompile::class.java).configureEach {
                it.kotlinOptions {
                    jvmTarget = "18"
                }
            }

            register("syncIdeaSettings", SyncIdeaSettingsTask::class.java)

            getByName("build").finalizedBy(project.tasks.getByName("syncIdeaSettings"))

            initializeTestJarTask()
            initializeTestTask()
            initializeTestWithoutFilterTask()
            initializeTestTaskWithoutNightly()

            addCopyMarkerAction(project, "java")
            addCopyMarkerAction(project, "groovy")
            addCopyMarkerAction(project, "kotlin")

            getByName("jar").setProperty("duplicatesStrategy", DuplicatesStrategy.EXCLUDE)

            val jacocoReport = getByName("jacocoTestReport") as JacocoReport
            getByName("test").finalizedBy(jacocoReport)
            jacocoReport.dependsOn(getByName("test"))
            jacocoReport.reports { reports ->
                reports.xml.required.set(true)
            }
        }

        project.afterEvaluate {
            it.tasks.withType(SyncIdeaSettingsTask::class.java).configureEach { task ->
                task.ideaSettingsUri = extension.ideaSettingsUri
            }
        }

        project.extensions.getByType(JavaPluginExtension::class.java).apply {
            sourceCompatibility = JavaVersion.VERSION_18
            targetCompatibility = JavaVersion.VERSION_18
            withSourcesJar()
        }

        project.extensions.getByType(PublishingExtension::class.java).apply {
            publications {
                it.create(project.name, MavenPublication::class.java) { publication ->
                    publication.from(project.components.getByName("java"))
                    publication.artifact(project.tasks.getByName("testJar"))
                }
            }

            repositories {
                it.maven { repository ->
                    repository.url = URI(project.providers.gradleProperty("mvnRepository").getOrElse("not_set"))
                    repository.credentials { credentials ->
                        credentials.username = System.getenv("MAVEN_USERNAME")
                        credentials.password = System.getenv("MAVEN_PASSWORD")
                    }
                }
            }
        }
    }

    private fun applyGradlePlugins(project: Project) {
        project.plugins.apply {
            apply(JavaPlugin::class.java)
            apply(GroovyPlugin::class.java)
            apply("kotlin")
            apply("jacoco")
            apply(MavenPublishPlugin::class.java)
        }
    }

    private fun setupTestDependencies(project: Project) {
        project.dependencies.apply {
            add("testImplementation", platform("org.junit:junit-bom:5.9.3"))
            add("testImplementation", "org.junit.platform:junit-platform-runner")
            add("testImplementation", "org.junit.platform:junit-platform-suite")
            add("testImplementation", "org.junit.jupiter:junit-jupiter")
            add("testImplementation", "org.junit.jupiter:junit-jupiter-params")
            add("testRuntimeOnly", "org.junit.jupiter:junit-jupiter-engine")

            add("testImplementation", platform("org.jetbrains.kotlin:kotlin-bom:1.8.21"))
            add("testImplementation", "org.jetbrains.kotlin:kotlin-stdlib")
            add("testImplementation", "org.jetbrains.kotlin:kotlin-test-junit")

            add("testImplementation", "io.mockk:mockk:1.13.5")
        }
    }

    private fun setupTestDependenciesForJUnit4(project: Project) {
        project.dependencies.apply {
            add("testImplementation", "junit:junit:4.12")
            add("testRuntimeOnly", "org.junit.vintage:junit-vintage-engine:5.8.2")
            // For legacy junit4 and scenario support. Include JUNIT-Toolbox for testing.
            add("testImplementation", "com.googlecode.junit-toolbox:junit-toolbox:2.4")
            // Include Spock for testing.
            add("testImplementation", "org.spockframework:spock-core:1.3-groovy-2.4")
            // Include bytebuddy and objenesis for advanced mocking at spockframework.
            add("testImplementation", "net.bytebuddy:byte-buddy:1.12.4")
            add("testImplementation", "org.objenesis:objenesis:3.2")
        }
    }

    private fun initTestTask(testTask: Test): Test {
        testTask.group = "verification"
        testTask.jvmArgs = listOf("-Ddebug=true")
        testTask.testLogging { logging ->
            logging.events = setOf(TestLogEvent.FAILED, TestLogEvent.STANDARD_OUT, TestLogEvent.STANDARD_ERROR)
        }
        testTask.useJUnitPlatform()
        return testTask
    }

    private fun TaskContainer.initializeTestTask() {
        initTestTask(getByPath("test") as Test).setIncludes(listOf("**/*TestSuite.class", "**/*Test.class"))
    }

    private fun TaskContainer.initializeTestWithoutFilterTask() {
        initTestTask(register("testWithoutFilter", Test::class.java).get()).dependsOn(getByName("testClasses"))
    }

    private fun TaskContainer.initializeTestTaskWithoutNightly() {
        val testTaskWithoutNightly = initTestTask(register("testWithoutNightly", Test::class.java).get())
        testTaskWithoutNightly.dependsOn(getByName("testClasses"))
        testTaskWithoutNightly.setIncludes(listOf("**/*TestSuite.class", "**/*Test.class"))
        testTaskWithoutNightly.systemProperty("test.excluded.groups", "nightly")
        testTaskWithoutNightly.useJUnitPlatform { platformOptions ->
            platformOptions.excludeTags = setOf("nightly")
        }
    }

    private fun TaskContainer.initializeTestJarTask() {
        register("testJar", Jar::class.java)
        val testJar = getByPath("testJar") as Jar
        testJar.setProperty("archiveClassifier", "tests")
        testJar.from(getByName("compileTestGroovy"))
    }

    private fun TaskContainer.addCopyMarkerAction(project: Project, output: String) {
        getByName("testClasses").doLast(CopyMarkerAction(project, output))
    }
}
