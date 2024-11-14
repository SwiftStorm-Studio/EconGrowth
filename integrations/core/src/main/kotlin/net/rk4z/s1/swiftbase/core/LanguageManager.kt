package net.rk4z.s1.swiftbase.core

import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import java.util.Locale
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@Deprecated("This class is not meant to be used directly. Use the SBHelper instead. If you using a SBHelper, you can ignore this warning.")
@Suppress("UNCHECKED_CAST", "DEPRECATION")
class LanguageManager<P : IPlayer<C>, C>(
    val textComponentFactory: (String) -> C,
    val expectedMKType: KClass<out MessageKey<P, C>>
) {
    companion object {
        internal lateinit var instance: LanguageManager<*, *>

        @JvmStatic
        fun <P : IPlayer<C>, C> get(): LanguageManager<P, C> {
            return instance as LanguageManager<P, C>
        }

        @Deprecated("If you are not API developer, you have to use [get] function instead of this function.")
        @JvmStatic
        fun getUnsafe(): LanguageManager<*, *> {
            return instance
        }

        @JvmStatic
        fun isInitialized(): Boolean {
            return this::instance.isInitialized
        }
    }

    val messages: MutableMap<String, MutableMap<MessageKey<P, C>, String>> = mutableMapOf()

    fun findMissingKeys(lang: String): List<String> {
        val messageKeyMap: MutableMap<String, MessageKey<P, C>> = mutableMapOf()
        scanForMessageKeys(messageKeyMap)
        val currentMessages = messages[lang] ?: return emptyList()
        val missingKeys = mutableListOf<String>()

        messageKeyMap.forEach { (path, key) ->
            if (!currentMessages.containsKey(key)) {
                missingKeys.add(path)
                Logger.warn("Missing key: $path for language: $lang")
            }
        }

        return missingKeys
    }

    //TODO: テストする。こいつを
    fun processYamlAndMapMessageKeys(
        data: Map<String, Any>,
        // default fallback
        lang: String = "en"
    ) {
        Logger.logIfDebug("Starting to process YAML and map message keys for language: $lang")

        val messageKeyMap: MutableMap<String, MessageKey<P, C>> = mutableMapOf()
        val messageMap: MutableMap<MessageKey<P, C>, String> = mutableMapOf()

        // クラス構造に基づいてメッセージキーを探索
        scanForMessageKeys(messageKeyMap)
        Logger.logIfDebug("MessageKey map generated with ${messageKeyMap.size} keys for language: $lang")

        // YAMLデータをマッピング
        processYamlData("", data, messageKeyMap, messageMap)
        Logger.logIfDebug("YAML data processed for language: $lang with ${messageMap.size} entries")

        // messagesにマップを格納
        messages[lang] = messageMap
        Logger.logIfDebug("Message map stored for language: $lang")
    }

    // Private helper functions
    private fun scanForMessageKeys(
        messageKeyMap: MutableMap<String, MessageKey<P, C>>
    ) {
        Logger.logIfDebug("Starting scan for message keys of expected type: ${expectedMKType.simpleName}")

        val reflections = Reflections(
            ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(Core.get().packageName))
                .setScanners(Scanners.SubTypes)
        )

        val messageKeyClasses = reflections.getSubTypesOf(MessageKey::class.java)
        Logger.logIfDebug("Found ${messageKeyClasses.size} potential MessageKey classes to examine")

        messageKeyClasses.forEach { clazz ->
            Logger.logIfDebug("Examining class: ${clazz.kotlin.qualifiedName}")
            mapMessageKeys(clazz.kotlin, "", messageKeyMap)
        }

        Logger.logIfDebug("Completed scanning for message keys; total keys mapped: ${messageKeyMap.size}")
    }

    private fun mapMessageKeys(
        clazz: KClass<out MessageKey<*, *>>,
        currentPath: String = "",
        messageKeyMap: MutableMap<String, MessageKey<P, C>>
    ) {
        val className = if (clazz == expectedMKType) "" else clazz.simpleName?.lowercase() ?: return
        val fullPath = if (currentPath.isEmpty()) className else "$currentPath.$className"

        Logger.logIfDebug("Mapping keys for class: ${clazz.simpleName}, fullPath: $fullPath")

        if (clazz == expectedMKType || clazz.isSubclassOf(expectedMKType)) {
            clazz.nestedClasses.forEach { nestedClass ->
                if (nestedClass.isSubclassOf(expectedMKType)) {
                    Logger.logIfDebug("Found nested class of expected type: ${nestedClass.simpleName}")
                    mapMessageKeys(nestedClass as KClass<out MessageKey<*, *>>, fullPath, messageKeyMap)
                }
            }
            return
        }

        val objectInstance = clazz.objectInstance
        if (expectedMKType.isInstance(objectInstance)) {
            Logger.logIfDebug("Adding object instance to messageKeyMap: $fullPath -> ${clazz.simpleName}")
            messageKeyMap[fullPath] = objectInstance as MessageKey<P, C>
        } else {
            Logger.logIfDebug("Skipping ${clazz.simpleName}: not an instance of expected type", LogLevel.WARN)
        }

        clazz.nestedClasses.forEach { nestedClass ->
            if (nestedClass.isSubclassOf(expectedMKType)) {
                Logger.logIfDebug("Exploring nested class: ${nestedClass.simpleName} under $fullPath")
                mapMessageKeys(nestedClass as KClass<out MessageKey<*, *>>, fullPath, messageKeyMap)
            }
        }
    }


    private fun processYamlData(
        prefix: String,
        data: Map<String, Any>,
        messageKeyMap: Map<String, MessageKey<P, C>>,
        messageMap: MutableMap<MessageKey<P, C>, String>
    ) {
        Logger.logIfDebug("Starting YAML data processing with prefix: '$prefix'")

        for ((key, value) in data) {
            val currentPrefix = if (prefix.isEmpty()) key else "$prefix.$key"
            Logger.logIfDebug("Processing key: $key, currentPrefix: $currentPrefix")

            when (value) {
                is String -> {
                    val messageKey = messageKeyMap[currentPrefix]
                    if (messageKey != null) {
                        Logger.logIfDebug("Mapping message: $currentPrefix -> $value")
                        messageMap[messageKey] = value
                    } else {
                        Logger.logIfDebug("No message key found for YAML path: $currentPrefix", LogLevel.WARN)
                    }
                }
                is Map<*, *> -> {
                    Logger.logIfDebug("Encountered nested structure at path: $currentPrefix; diving deeper")
                    processYamlData(currentPrefix, value as Map<String, Any>, messageKeyMap, messageMap)
                }
                else -> {
                    Logger.logIfDebug("Unexpected value type at path $currentPrefix: ${value::class.simpleName}")
                }
            }
        }

        Logger.logIfDebug("Completed YAML data processing for prefix: '$prefix'")
    }

    fun getSysMessage(key: MessageKey<P, C>, vararg args: Any): String {
        val lang = Locale.getDefault().language
        val message = messages[lang]?.get(key)
        val text = message?.let { String.format(it, *args) } ?: return key.rc()
        return text
    }
}
