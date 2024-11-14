package net.rk4z.s1.swiftbase.paper

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.rk4z.s1.swiftbase.bstats.Metrics
import net.rk4z.s1.swiftbase.core.Core
import net.rk4z.s1.swiftbase.core.LanguageManagerInfo
import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin
import org.jetbrains.annotations.NotNull
import org.slf4j.Logger
import java.io.File

open class PluginEntry(
    @NotNull
    val id: String,
    @NotNull
    val packageName: String,

    val isDebug: Boolean = false,

    val configFile: String? = null,
    val configResourceRoot: String = "assets/config",

    val languageManagerInfo: LanguageManagerInfo<PaperPlayer, TextComponent>? = LanguageManagerInfo<PaperPlayer, TextComponent>(
        textComponentFactory = paperTextComponentFactory,
        expectedType = PaperMessageKey::class,
    ),
    val availableLang: List<String>? = null,
    val langDir: String? = null,
    val langResourceRoot: String = "assets/lang",

    val logger: Logger,

    var enableUpdateChecker: Boolean = true,
    val modrinthID: String = "",
    val serviceId: Int? = null,
) : JavaPlugin() {
    companion object {
        lateinit var core: Core

        private lateinit var metrics: Metrics
        private lateinit var instance: PluginEntry

        @JvmStatic
        lateinit var key: NamespacedKey
            private set

        fun <I : PluginEntry> get() : I? {
            return instance as? I
        }

        val paperTextComponentFactory = { text: String -> Component.text(text) }
    }

    override fun onLoad() {
        core = Core.initialize<PaperPlayer, TextComponent>(
            packageName,
            isDebug,
            dataFolder,
            configFile,
            configResourceRoot,
            availableLang,
            langDir,
            langResourceRoot,
            S1Executor(this),
            logger,
            modrinthID,
            description.version,
            languageManagerInfo
        )
        instance = getPlugin(this::class.java)
        key = NamespacedKey(this, id)

        onLoadPre()

        core.initializeDirectories()
        if (languageManagerInfo != null) {
        if (!isDebug) {
            core.updateLanguageFilesIfNeeded()
        }
            core.loadLanguageFiles()
        }

        onLoadPost()
    }

    override fun onEnable() {
        onEnablePre()

        if (serviceId != null) {
            metrics = Metrics(this, serviceId)
        } else {
            throw IllegalStateException("Service ID must be provided to enable metrics")
        }

        if (enableUpdateChecker) {
            core.checkUpdate()
        }

        onEnablePost()
    }

    override fun onDisable() {
        onDisablePre()

        core.executor.shutdown()

        onDisablePost()
    }

    // This is a wrapper for the core's lc method
    // (I just don't want to write `core.lc<T>(key)` every time)
    inline fun <reified T> lc(key: String): T? {
        return core.lc<T>(key)
    }

    open fun onLoadPre() {}
    open fun onLoadPost() {}
    open fun onEnablePre() {}
    open fun onEnablePost() {}
    open fun onDisablePre() {}
    open fun onDisablePost() {}
}