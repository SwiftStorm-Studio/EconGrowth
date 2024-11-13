package net.rk4z.s1.swiftbase.core

import org.jetbrains.annotations.NotNull
import org.json.JSONArray
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.yaml.snakeyaml.Yaml
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.math.BigDecimal
import java.math.BigInteger
import java.net.HttpURLConnection
import java.net.URI
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.Locale
import kotlin.io.path.notExists

/**
 * The core class of the SwiftBase.
 *
 * This class contains the methods that are common to all platforms.
 */
@Suppress("CanBeParameter", "PropertyName", "unused", "DEPRECATION")
class Core private constructor(
    @NotNull
    val packageName: String,

    val isDebug: Boolean,

    val dataFolder: File,
    val configFile: File? = null,
    val configResourceRoot: String,

    val availableLang: List<String>? = null,
    val langDir: File? = null,
    val langResourceRoot: String,

    val executor: S0Executor,
    val logger: Logger,

    val modrinthID: String,
    val version: String,
) {
    companion object {
        internal lateinit var instance: Core
        internal lateinit var languageManager: LanguageManager<*, *>

        lateinit var helper: ResourceHelper

        var logger: Logger = LoggerFactory.getLogger("SwiftBase")

        /**
         * Initializes the Core.
         * This method should be called only once.
         *
         * @param packageName The packageName is used to detect registered language keys.
         * @param isDebug Whether the debug mode is enabled. If true, debug messages will be printed.
         * @param dataFolder Specify the dataFolder (resource output destination for Mods and Plugins).
         * @param configFile Specify the configFile. If null, the config system will be disabled.
         * @param configResourceRoot Determines where in the dataFolder the config files are output.
         * @param availableLang A list of available languages like "en". If null, languageSystem will be disabled.
         * @param langDir Determines where in the dataFolder the language files are output.
         * @param langResourceRoot Determines where in the resources the language files are located.
         * @param executor The executor object.
         * @param logger The logger object.
         * @param modrinthID The Modrinth ID of the project.
         * @param version The version of the project.
         * @param languageManagerInfo The LanguageManagerInfo object. If null, the languageSystem will be disabled.
         * @return The Core instance.
         * @throws IllegalStateException If the Core has already been initialized or if a required parameter is null.
         */
        @JvmStatic
        fun <P : IPlayer, C> initialize(
            @NotNull
            packageName: String,
            isDebug: Boolean = false,
            dataFolder: File,
            configFile: File? = null,
            configResourceRoot: String = "assets/config",
            availableLang: List<String>? = null,
            langDir: File? = null,
            langResourceRoot: String = "assets/lang",
            executor: S0Executor,
            logger: Logger = this.logger,
            modrinthID: String = "",
            version: String = "0",
            languageManagerInfo: LanguageManagerInfo<P, C>? = null,
        ): Core {
            if (::instance.isInitialized) {
                throw IllegalStateException("Core has already been initialized.")
            }

            languageManagerInfo?.run {
                languageManager = SBHelper.crateLanguageManager(textComponentFactory, expectedType)
            }

            instance = Core(
                packageName,
                isDebug,
                dataFolder,
                configFile,
                configResourceRoot,
                availableLang,
                langDir,
                langResourceRoot,
                executor,
                logger,
                modrinthID,
                version,
            )
            return instance
        }

        @JvmStatic
        fun get(): Core {
            if (!::instance.isInitialized) {
                throw IllegalStateException("Core has not been initialized.")
            }

            return instance
        }
    }

    init {
        Companion.logger = this.logger
        helper = ResourceHelper(dataFolder)
    }

    val MODRINTH_API_URL = "https://api.modrinth.com/v2/project/${modrinthID}/version"
    val MODRINTH_DOWNLOAD_URL = "https://modrinth.com/plugin/${modrinthID}/versions/"

    val yaml = Yaml()

    var onCheckUpdate: () -> Unit = {}
    var onAllVersionsRetrieved: (versionCount: Int) -> Unit = {}
    var onNewVersionFound: (latestVersion: String, newerVersionCount: Int) -> Unit = { _, _ -> }
    var onNoNewVersionFound: () -> Unit = {}
    var onUpdateCheckFailed: (responseCode: Int) -> Unit = {}
    var onUpdateCheckError: (e: Exception) -> Unit = {}

    /**
     * Loads a value from the config file.
     * The value will be cast to the type T.
     *
     * @param key The key of the value.
     * @return The value of the key.
     * @throws IllegalStateException If the config file is not set.
     */
    inline fun <reified T> lc(key: String): T? {
        checkNotNull(configFile) { "Config file is not set but you try to load a value from it." }

        val config: Map<String, Any> = Files.newInputStream(configFile.toPath()).use { yaml.load(it) }
        val value = config[key]
        return parseValue(value)
    }

    inline fun <reified T> parseValue(value: Any?): T? {
        return when (T::class) {
            String::class -> value as? T
            Int::class -> value?.toString()?.toIntOrNull() as? T
            Boolean::class -> value?.toString()?.toBooleanOrNull() as? T
            Double::class -> value?.toString()?.toDoubleOrNull() as? T
            Short::class -> value?.toString()?.toShortOrNull() as? T
            Long::class -> value?.toString()?.toLongOrNull() as? T
            Float::class -> value?.toString()?.toFloatOrNull() as? T
            Byte::class -> value?.toString()?.toByteOrNull() as? T
            Char::class -> (value as? String)?.singleOrNull() as? T
            List::class -> value as? List<*> as? T
            Array::class -> (value as? List<*>)?.toTypedArray() as? T
            Map::class -> value as? Map<*, *> as? T
            BigInteger::class -> (value?.toString())?.let { BigInteger(it) } as? T
            BigDecimal::class -> (value?.toString())?.let { BigDecimal(it) } as? T
            else -> value as? T
        }
    }

    fun initializeDirectories() {
        if (dataFolder.toPath().notExists()) dataFolder.mkdirs()
        configFile?.let { createConfigIfNotExists() }
        if (availableLang != null) initializeLanguageFiles()
    }

    fun updateLanguageFilesIfNeeded() {
        availableLang?.forEach { lang ->
            val langFile = File(langDir, "$lang.yml")
            val langResource = "$langResourceRoot/$lang.yml"

            helper.getResource(langResource)?.use { resourceStream ->
                val resourceBytes = resourceStream.readBytes()

                val jarLangVersion = readLangVersion(resourceBytes.inputStream())
                val installedLangVersion = if (langFile.exists()) {
                    Files.newInputStream(langFile.toPath()).use { inputStream ->
                        readLangVersion(inputStream)
                    }
                } else {
                    "0"
                }

                if (isVersionNewer(jarLangVersion, installedLangVersion)) {
                    logger.info("Replacing old $lang language file (version: $installedLangVersion) with newer version: $jarLangVersion")
                    resourceBytes.inputStream().use { byteArrayStream ->
                        Files.copy(
                            byteArrayStream,
                            langFile.toPath(),
                            StandardCopyOption.REPLACE_EXISTING
                        )
                    }
                } else {
                    logger.info("Language file for $lang is up to date.")
                }
            } ?: logger.warn("Resource file '$langResource' not found in the Jar.")
        }
    }

    fun checkUpdate() {
        onCheckUpdate()
        try {
            val connection = createConnection()
            if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                val response = executor.submit { connection.inputStream.bufferedReader().readText() }.get()
                val (latestVersion, versionCount, newerVersionCount) = extractVersionInfo(response)
                onAllVersionsRetrieved(versionCount)
                if (isVersionNewer(latestVersion, version)) {
                    onNewVersionFound(latestVersion, newerVersionCount)
                } else {
                    onNoNewVersionFound()
                }
            } else {
                onUpdateCheckFailed(connection.responseCode)
            }
        } catch (e: Exception) {
            onUpdateCheckError(e)
        }
    }

    fun loadLanguageFiles() {
        if (!availableLang.isNullOrEmpty()) {
            availableLang.forEach { lang ->
                requireNotNull(langDir) { "Available languages are set but langDir is null." }

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
        } else {
            Logger.warn("No languages are available. Aborting language file loading process.")
        }
    }

    private fun createConfigIfNotExists() {
        val defaultConfigLang = Locale.getDefault().language
        val configResource = "$configResourceRoot/${defaultConfigLang}.yml"
        helper.getResource(configResource)?.use { inputStream ->
            val targetConfigFile = File(dataFolder, "config.yml")
            if (!targetConfigFile.exists()) Files.copy(inputStream, targetConfigFile.toPath())
        }
    }

    private fun initializeLanguageFiles() {
        checkNotNull(langDir) { "Language directory (langDir) is not set, but available languages are." }

        if (!langDir.exists()) langDir.mkdirs()
        availableLang?.forEach { lang ->
            langDir.resolve("$lang.yml").apply {
                if (this.toPath().notExists()) helper.saveResource("$langResourceRoot/$lang.yml", false, langDir)
            }
        }
    }

    private fun readLangVersion(stream: InputStream): String {
        return InputStreamReader(stream, StandardCharsets.UTF_8).use { reader ->
            val langData: Map<String, Any> = yaml.load(reader)
            langData["langVersion"]?.toString() ?: "0"
        }
    }

    private fun createConnection(): HttpURLConnection {
        return URI(MODRINTH_API_URL).toURL().openConnection() as HttpURLConnection
    }

    private fun extractVersionInfo(response: String): Triple<String, Int, Int> {
        val jsonArray = JSONArray(response)
        var latestVersion = ""
        var latestDate = ""
        val versionCount = jsonArray.length()
        var newerVersionCount = 0

        for (i in 0 until jsonArray.length()) {
            val versionObject = jsonArray.getJSONObject(i)
            val versionNumber = versionObject.getString("version_number")
            val releaseDate = versionObject.getString("date_published")

            if (isVersionNewer(versionNumber, version)) newerVersionCount++

            if (releaseDate > latestDate) {
                latestDate = releaseDate
                latestVersion = versionNumber
            }
        }
        return Triple(latestVersion, versionCount, newerVersionCount)
    }

    private fun isVersionNewer(version1: String, version2: String): Boolean {
        val v1Parts = version1.split(".").map { it.toIntOrNull() ?: 0 }
        val v2Parts = version2.split(".").map { it.toIntOrNull() ?: 0 }
        val maxLength = maxOf(v1Parts.size, v2Parts.size)
        val v1Padded = v1Parts + List(maxLength - v1Parts.size) { 0 }
        val v2Padded = v2Parts + List(maxLength - v2Parts.size) { 0 }

        for (i in 0 until maxLength) {
            if (v1Padded[i] > v2Padded[i]) return true
            if (v1Padded[i] < v2Padded[i]) return false
        }

        return false
    }
}
