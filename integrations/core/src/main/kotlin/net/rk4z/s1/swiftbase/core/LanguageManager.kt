package net.rk4z.s1.swiftbase.core

import org.reflections.Reflections
import org.reflections.scanners.Scanners
import org.reflections.util.ClasspathHelper
import org.reflections.util.ConfigurationBuilder
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@Deprecated("This class is not meant to be used directly. Use the SBHelper instead. If you using a SBHelper, you can ignore this warning.")
@Suppress("UNCHECKED_CAST", "DEPRECATION")
class LanguageManager<P : IPlayer, C>(
    val textComponentFactory: (String) -> C,
    val expectedType: KClass<out MessageKey<P, C>>,
) {
    companion object {
        internal lateinit var instance: LanguageManager<*, *>

        fun <P : IPlayer, C> get(): LanguageManager<P, C> {
            return instance as LanguageManager<P, C>
        }
    }

    val messages: MutableMap<String, MutableMap<MessageKey<P, C>, String>> = mutableMapOf()

    //TODO: テストする。こいつを
    fun processYamlAndMapMessageKeys(
        data: Map<String, Any>,
        lang: String
    ) {
        Logger.logIfDebug("Starting to process YAML and map message keys for language: $lang")

        val messageKeyMap: MutableMap<String, MessageKey<P, C>> = mutableMapOf()
        val messageMap: MutableMap<MessageKey<P, C>, String> = mutableMapOf()

        // クラス構造に基づいてメッセージキーを探索
        scanForMessageKeys(messageKeyMap, expectedType)
        Logger.logIfDebug("MessageKey map generated with ${messageKeyMap.size} keys for language: $lang")

        // YAMLデータをマッピング
        processYamlData("", data, messageKeyMap, messageMap)
        Logger.logIfDebug("YAML data processed for language: $lang with ${messageMap.size} entries")

        // messagesにマップを格納
        messages[lang] = messageMap
        Logger.logIfDebug("Message map stored for language: $lang")
    }

    private fun scanForMessageKeys(
        messageKeyMap: MutableMap<String, MessageKey<P, C>>,
        expectedType: KClass<out MessageKey<P, C>>
    ) {
        Logger.logIfDebug("Starting scan for message keys of expected type: ${expectedType.simpleName}")

        val reflections = Reflections(
            ConfigurationBuilder()
                .setUrls(ClasspathHelper.forPackage(Core.get().packageName))
                .setScanners(Scanners.SubTypes)
        )

        val messageKeyClasses = reflections.getSubTypesOf(MessageKey::class.java)
        Logger.logIfDebug("Found ${messageKeyClasses.size} potential MessageKey classes to examine")

        messageKeyClasses.forEach { clazz ->
            Logger.logIfDebug("Examining class: ${clazz.kotlin.qualifiedName}")
            mapMessageKeys(clazz.kotlin, expectedType, "", messageKeyMap)
        }

        Logger.logIfDebug("Completed scanning for message keys; total keys mapped: ${messageKeyMap.size}")
    }

    private fun mapMessageKeys(
        clazz: KClass<out MessageKey<*, *>>,
        expectedType: KClass<out MessageKey<P, C>>,
        currentPath: String = "",
        messageKeyMap: MutableMap<String, MessageKey<P, C>>
    ) {
        val className = if (clazz == expectedType) "" else clazz.simpleName?.lowercase() ?: return
        val fullPath = if (currentPath.isEmpty()) className else "$currentPath.$className"

        Logger.logIfDebug("Mapping keys for class: ${clazz.simpleName}, fullPath: $fullPath")

        if (clazz == expectedType || clazz.isSubclassOf(expectedType)) {
            clazz.nestedClasses.forEach { nestedClass ->
                if (nestedClass.isSubclassOf(expectedType)) {
                    Logger.logIfDebug("Found nested class of expected type: ${nestedClass.simpleName}")
                    mapMessageKeys(nestedClass as KClass<out MessageKey<*, *>>, expectedType, fullPath, messageKeyMap)
                }
            }
            return
        }

        val objectInstance = clazz.objectInstance
        if (expectedType.isInstance(objectInstance)) {
            Logger.logIfDebug("Adding object instance to messageKeyMap: $fullPath -> ${clazz.simpleName}")
            messageKeyMap[fullPath] = objectInstance as MessageKey<P, C>
        } else {
            Logger.logIfDebug("Skipping ${clazz.simpleName}: not an instance of expected type", LogLevel.WARN)
        }

        clazz.nestedClasses.forEach { nestedClass ->
            if (nestedClass.isSubclassOf(expectedType)) {
                Logger.logIfDebug("Exploring nested class: ${nestedClass.simpleName} under $fullPath")
                mapMessageKeys(nestedClass as KClass<out MessageKey<*, *>>, expectedType, fullPath, messageKeyMap)
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

    fun <P : IPlayer> getMessage(player: P, key: MessageKey<*, *>): C {
        require(key::class.isSubclassOf(expectedType)) { "Unexpected MessageKey type: ${key::class}. Expected: $expectedType" }
        val lang = player.getLanguage()
        val message = messages[lang]?.get(key) ?: key.rc()
        return textComponentFactory(message)
    }
}
