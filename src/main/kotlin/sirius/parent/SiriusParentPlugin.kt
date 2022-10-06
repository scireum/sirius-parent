@file:Suppress("unused")

package sirius.parent

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.file.DuplicatesStrategy
import org.gradle.api.plugins.GroovyPlugin
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.tasks.GroovySourceDirectorySet
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.testing.Test
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.net.URI

open class SiriusParentPluginExtension {
    var ideaSettingsUri = "https://raw.githubusercontent.com/scireum/sirius-parent/master/src/main/resources"
}

class SiriusParentPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("siriusParent", SiriusParentPluginExtension::class.java)

        project.plugins.apply {
            apply(JavaPlugin::class.java)
            apply(GroovyPlugin::class.java)
            apply("kotlin")
        }

        project.repositories.apply {
            add(project.repositories.mavenLocal())
            add(project.repositories.maven {
                it.url = URI("https://mvn.scireum.com")
            })
            add(project.repositories.mavenCentral())
        }

        project.dependencies.apply {
            add("testImplementation", "org.junit.platform:junit-platform-runner:1.8.2")
            add("testImplementation", "org.junit.platform:junit-platform-suite:1.8.2")
            add("testImplementation", "org.junit.jupiter:junit-jupiter:5.8.2")
            add("testImplementation", "org.junit.jupiter:junit-jupiter-engine:5.8.2")
            add("testImplementation", "org.junit.jupiter:junit-jupiter-params:5.8.2")

            add("testImplementation", "org.jetbrains.kotlin:kotlin-stdlib:1.7.10")
            add("testImplementation", "org.jetbrains.kotlin:kotlin-test-junit:1.7.10")
            add("testImplementation", "io.mockk:mockk:1.12.3")

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

        // set source directories for groovy compilation
        val testSourceSet = project.extensions.getByType(SourceSetContainer::class.java).getByName(SourceSet.TEST_SOURCE_SET_NAME)
        testSourceSet.extensions.getByType(GroovySourceDirectorySet::class.java).setSrcDirs(listOf("src/test/groovy", "src/test/java"))
        // no source directory for compileTestJava as the groovy task already compiles both, groovy and java files.
        testSourceSet.java.setSrcDirs(emptySet<String>())

        project.tasks.apply {
            val testTaskFull = getByPath("test") as Test
            testTaskFull.setIncludes(listOf("**/*TestSuite.class"))
            testTaskFull.jvmArgs = listOf("-Ddebug=true")
            testTaskFull.useJUnitPlatform()

            register("testWithoutNightly", Test::class.java)
            val testTaskWithoutNightly = getByName("testWithoutNightly") as Test
            testTaskWithoutNightly.setIncludes(listOf("**/*TestSuite.class"))
            testTaskWithoutNightly.jvmArgs = listOf("-Ddebug=true")
            testTaskWithoutNightly.systemProperty("test.excluded.groups", "nightly")
            testTaskWithoutNightly.useJUnitPlatform { platformOptions ->
                platformOptions.excludeTags = setOf("nightly")
            }

            withType(KotlinCompile::class.java).configureEach {
                it.kotlinOptions {
                    jvmTarget = "18"
                }
            }

            register("syncIdeaSettings", SyncIdeaSettingsTask::class.java)

            getByName("build").finalizedBy(project.tasks.getByName("syncIdeaSettings"))

            addCopyMarker("copyJavaMarker", CopyJavaMarkerTask::class.java)
            addCopyMarker("copyGroovyMarker", CopyGroovyMarkerTask::class.java)
            addCopyMarker("copyKotlinMarker", CopyKotlinMarkerTask::class.java)

            getByName("jar").setProperty("duplicatesStrategy", DuplicatesStrategy.EXCLUDE)
        }

        project.afterEvaluate {
            it.tasks.withType(SyncIdeaSettingsTask::class.java).configureEach { task ->
                task.ideaSettingsUri = extension.ideaSettingsUri
            }
        }
    }

    private fun TaskContainer.addCopyMarker(name: String, clazz: Class<out CopyMarkerTask>) {
        create(name, clazz)

        getByName("processResources").finalizedBy(getByName(name))
        getByName("processTestResources").finalizedBy(getByName(name))
    }
}
