@file:Suppress("unused")

package sirius.parent

import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.*
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.publish.maven.plugins.MavenPublishPlugin
import org.gradle.api.tasks.GroovySourceDirectorySet
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.testing.Test
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
                it.name = "scireum-mvn"
                it.url = URI("https://mvn.scireum.com")
            })
            add(project.repositories.mavenCentral())
        }

        applyGradlePlugins(project)

        setupRuntimeDependencies(project)
        setupTestDependencies(project)
        setupImplementationDependenciesForJUnit4Tests(project)

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

            getByName("build").finalizedBy(register("syncIdeaSettings", SyncIdeaSettingsTask::class.java).get())

            initializeTestJarTask()
            initializeTestTask()
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

        project.extensions.getByType(JavaApplication::class.java).apply {
            mainClass.set("sirius.kernel.Setup")
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
            apply(ApplicationPlugin::class.java)
        }
    }

    private fun setupRuntimeDependencies(project: Project) {
        project.dependencies.apply {
            add("api", platform("com.fasterxml.jackson:jackson-bom:2.15.0"))
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
            add("testRuntimeOnly", "org.junit.vintage:junit-vintage-engine:5.8.2")

            add("testImplementation", platform("org.jetbrains.kotlin:kotlin-bom:1.8.21"))
            add("testImplementation", "org.jetbrains.kotlin:kotlin-stdlib")
            add("testImplementation", "org.jetbrains.kotlin:kotlin-test-junit")

            add("testImplementation", "io.mockk:mockk:1.13.5")
        }
    }

    private fun setupImplementationDependenciesForJUnit4Tests(project: Project) {
        project.dependencies.apply {
            add("testImplementation", "junit:junit:4.12")
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
        return initTestTask(testTask, emptySet())
    }

    private fun initTestTask(testTask: Test, excludedTags: Set<String>): Test {
        testTask.group = "verification"
        testTask.jvmArgs = listOf("-Ddebug=true")
        testTask.testLogging { logging ->
            logging.showStandardStreams = true
            logging.showExceptions = true
        }
        testTask.useJUnitPlatform { platformOptions ->
            platformOptions.includeEngines = setOf("junit-jupiter", "junit-vintage")
            platformOptions.excludeTags = excludedTags
        }
        return testTask
    }

    private fun TaskContainer.initializeTestTask() {
        initTestTask(getByPath("test") as Test).setIncludes(listOf("**/*TestSuite.class", "**/*Test.class"))
            .dependsOn(getByName("testClasses"))
    }

    private fun TaskContainer.initializeTestTaskWithoutNightly() {
        val testTaskWithoutNightly =
            initTestTask(register("testWithoutNightly", Test::class.java).get(), setOf("nightly"))
        testTaskWithoutNightly.dependsOn(getByName("testClasses"))
        testTaskWithoutNightly.setIncludes(listOf("**/*TestSuite.class", "**/*Test.class"))
        testTaskWithoutNightly.systemProperty("test.excluded.groups", "nightly")
    }

    private fun TaskContainer.initializeTestJarTask() {
        val testJar = register("testJar", Jar::class.java).get()
        testJar.setProperty("archiveClassifier", "tests")
        testJar.dependsOn(getByName("testClasses"))
        testJar.from(getByName("compileTestGroovy"), getByName("compileTestJava"), getByName("compileTestKotlin"))
        testJar.duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }

    private fun TaskContainer.addCopyMarkerAction(project: Project, output: String) {
        getByName("classes").doLast(CopyMarkerAction(project, output))
        getByName("testClasses").doLast(CopyMarkerAction(project, output))
    }
}
