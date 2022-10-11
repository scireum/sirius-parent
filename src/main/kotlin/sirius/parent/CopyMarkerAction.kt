package sirius.parent

import org.gradle.api.Action
import org.gradle.api.Project
import org.gradle.api.Task


open class CopyMarkerAction(private val project: Project, private val output: String) : Action<Task> {

    override fun execute(t: Task) {
        project.copy { copySpec ->
            copySpec.from("${project.buildDir}/resources/")
            copySpec.into("${project.buildDir}/classes/$output")
            copySpec.include("**/*.marker")
        }
    }

}
