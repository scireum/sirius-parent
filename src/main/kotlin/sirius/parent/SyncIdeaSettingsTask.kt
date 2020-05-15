package sirius.parent

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL


open class SyncIdeaSettingsTask : DefaultTask() {

    @get:Input
    var ideaSettingsUri: String = ""

    init {
        group = "project setup"
        description = "Downloads generic IntelliJ Idea project setup files to the projects .idea folder"
    }

    @TaskAction
    fun syncIdeaSettings() {
        val projectDir = project.projectDir

        if (!project.file("$projectDir/.idea").exists()) {
            logger.lifecycle("Skipping Idea Folder sync as .idea folder does not exist.")
            return
        }

        logger.lifecycle("Downloading files from $ideaSettingsUri.")

        project.mkdir("$projectDir/.idea/codeStyles")
        project.mkdir("$projectDir/.idea/inspectionProfiles")
        project.mkdir("$projectDir/.idea/copyright")
        project.mkdir("$projectDir/.idea/scopes")

        downloadFile(
                uri = "$ideaSettingsUri/.idea/codeStyleSettings.xml",
                target = File("$projectDir/.idea/codeStyleSettings.xml")
        )
        downloadFile(
                uri = "$ideaSettingsUri/.idea/encodings.xml",
                target = File("$projectDir/.idea/encodings.xml")
        )
        downloadFile(
                uri = "$ideaSettingsUri/.idea/kotlinc.xml",
                target = File("$projectDir/.idea/kotlinc.xml")
        )
        downloadFile(
                uri = "$ideaSettingsUri/.idea/misc.xml",
                target = File("$projectDir/.idea/misc.xml")
        )

        // codeStyles settings
        downloadFile(
                uri = "$ideaSettingsUri/.idea/codeStyles/Project.xml",
                target = File("$projectDir/.idea/codeStyles/Project.xml")
        )
        downloadFile(
                uri = "$ideaSettingsUri/.idea/codeStyles/codeStyleConfig.xml",
                target = File("$projectDir/.idea/codeStyles/codeStyleConfig.xml")
        )

        // copyright settings
        downloadFile(
                uri = "$ideaSettingsUri/.idea/copyright/profiles_settings.xml",
                target = File("$projectDir/.idea/copyright/profiles_settings.xml")
        )
        downloadFile(
                uri = "$ideaSettingsUri/.idea/copyright/scireum.xml",
                target = File("$projectDir/.idea/copyright/scireum.xml")
        )

        // copyright settings
        downloadFile(
                uri = "$ideaSettingsUri/.idea/inspectionProfiles/Sirius.xml",
                target = File("$projectDir/.idea/inspectionProfiles/Sirius.xml")
        )
        downloadFile(
                uri = "$ideaSettingsUri/.idea/inspectionProfiles/Sirius_Heavy.xml",
                target = File("$projectDir/.idea/inspectionProfiles/Sirius_Heavy.xml")
        )
        downloadFile(
                uri = "$ideaSettingsUri/.idea/inspectionProfiles/profiles_settings.xml",
                target = File("$projectDir/.idea/inspectionProfiles/profiles_settings.xml")
        )

        // scopes settings
        downloadFile(
                uri = "$ideaSettingsUri/.idea/scopes/sirius.xml",
                target = File("$projectDir/.idea/scopes/sirius.xml")
        )
    }

    private fun downloadFile(uri: String, target: File) {
        try {
            val connection = URL(uri).openConnection() as HttpURLConnection
            with(connection) {
                requestMethod = "GET"
                doOutput = true
                connect()
            }

            val buffer = ByteArray(1024)
            var bufferLength: Int

            FileOutputStream(target).use {
                while (connection.inputStream.read(buffer).also { length -> bufferLength = length } > 0) {
                    it.write(buffer, 0, bufferLength)
                }
            }
            logger.lifecycle("Downloaded file $target.")
        } catch (e: IOException) {
            var message = e.message
            if (message == null) {
                message = "Could not download file"
            }
            throw IllegalStateException(message, e)
        }
    }

}
