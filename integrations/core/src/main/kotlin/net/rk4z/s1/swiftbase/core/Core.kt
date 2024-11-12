package net.rk4z.s1.swiftbase.core

import org.jetbrains.annotations.NotNull
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files

/**
 * The core class of the SwiftBase.
 *
 * This class contains the methods that are common to all platforms.
 */
class Core private constructor(
    @NotNull
    val packageName: String,
    val isDebug: Boolean,
    val langDir: File,
    val availableLang: List<String>? = null,
    val Logger: Logger
) {
    companion object {
        lateinit var instance: Core
            private set
        lateinit var languageManager: LanguageManager<*, *>

        var logger: Logger = LoggerFactory.getLogger("SwiftBase")

        @JvmStatic
        fun initialize(
            packageName: String,
            isDebug: Boolean,
            langDir: File,
            availableLang: List<String>? = null
        ): Core {
            instance = Core(
                packageName,
                isDebug,
                langDir,
                availableLang,
                logger
            )
            return instance
        }

        @JvmStatic
        fun get(): Core {
            return instance
        }
    }

    private val yaml = Yaml()

    fun loadLanguageFiles() {
        availableLang?.let {
            it.forEach { lang ->
                val langFile = langDir.resolve("$lang.yml")
                if (Files.exists(langFile.toPath())) {
                    Files.newBufferedReader(langFile.toPath(), StandardCharsets.UTF_8).use { reader ->
                        val data: Map<String, Any> = yaml.load(reader)
                        languageManager.processYamlAndMapMessageKeys(data, lang)
                    }
                } else {
                    Logger.warn("Language file for '$lang' not found.")
                }
            }
        } ?: Logger.warn("No languages are available. Aborting language file loading process.")
    }
}