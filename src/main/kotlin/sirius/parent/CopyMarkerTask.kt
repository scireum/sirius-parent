package sirius.parent

import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.TaskAction


open class CopyMarkerTask(private val output: String) : Copy() {

    init {
        from("${project.buildDir}/resources/") {
            include("**/*.marker")
        }

        into("${project.buildDir}/classes/$output")
    }

    @TaskAction
    fun copyMarker() {
        super.copy()
    }

}
