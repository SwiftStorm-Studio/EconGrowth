package net.rk4z.s1.swiftbase.core

import org.jetbrains.annotations.NotNull
import java.io.*

/**
 * Utility class for handling resources embedded within the plugin JAR. This class provides
 * methods to save resources from the JAR to a specified data folder and retrieve resources
 * as input streams.
 *
 * @property dataFolder The root directory where resources will be saved.
 */
class ResourceHelper internal constructor(
    private val dataFolder: File
) {

    /**
     * Saves a resource from the plugin JAR to a specified output path or the default data folder.
     *
     * @param resourcePath The path to the resource within the JAR file. Must not be empty or null.
     * @param outPath The optional directory where the resource should be saved.
     *                If not specified, the `dataFolder` is used by default.
     * @param replace Whether to overwrite the file if it already exists at the target location.
     *
     * @throws IllegalArgumentException If `resourcePath` is empty or if the resource could not be found in the JAR.
     * @throws IOException If there is an error during the file save process.
     *
     * This function will create any necessary directories within `outPath` or `dataFolder` if they do not already exist.
     * If `replace` is false and the target file already exists, a warning is logged, and the file is not overwritten.
     */
    fun saveResource(
        @NotNull resourcePath: String,
        replace: Boolean,
        outPath: File? = null,
    ) {
        if (resourcePath.isEmpty()) {
            throw IllegalArgumentException("ResourcePath cannot be null or empty")
        }

        val sanitizedPath = resourcePath.replace('\\', '/')
        val inputStream = getResource(sanitizedPath)
            ?: throw IllegalArgumentException("The embedded resource '$sanitizedPath' cannot be found in the JAR.")

        val destinationDir = outPath ?: dataFolder
        val fileName = sanitizedPath.substringAfterLast('/')
        val outFile = File(destinationDir, fileName)

        if (!destinationDir.exists()) {
            destinationDir.mkdirs()
        }

        try {
            if (!outFile.exists() || replace) {
                FileOutputStream(outFile).use { out ->
                    inputStream.use { input ->
                        input.copyTo(out)
                    }
                }
                Logger.info("Saved resource $sanitizedPath to $outFile")
            } else {
                Logger.warn("Could not save $outFile because it already exists.")
            }
        } catch (ex: IOException) {
            Logger.warn("Could not save $outFile: ${ex.message}")
        }
    }

    /**
     * Retrieves an embedded resource from the JAR as an [InputStream].
     *
     * @param filename The name of the resource file within the JAR.
     * @return An [InputStream] for the resource, or `null` if the resource is not found.
     * @throws IllegalArgumentException If the filename is empty.
     */
    fun getResource(filename: String): InputStream? {
        if (filename.isEmpty()) {
            throw IllegalArgumentException("Filename cannot be null or empty")
        }

        return try {
            val url = this::class.java.classLoader.getResource(filename) ?: return null
            url.openConnection().apply { useCaches = false }.getInputStream()
        } catch (_: IOException) {
            null
        }
    }
}