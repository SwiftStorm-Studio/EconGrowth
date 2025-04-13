package net.ririfa.econgrowth.paper.specializations

import net.kyori.adventure.text.TextComponent
import net.rk4z.s1.econgrowth.paper.skills.Skill
import net.rk4z.s1.swiftbase.core.Logger
import net.rk4z.s1.swiftbase.core.logIfDebug
import net.rk4z.s1.swiftbase.paper.PaperMessageKey
import net.rk4z.s1.swiftbase.paper.PaperPlayer
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

@Suppress("unused", "CanBeParameter")
abstract class Specializations(
    private val name: PaperMessageKey,
    private val description: PaperMessageKey,
    val skills: List<Skill>? = null,
    // 全職業は、基本的に最大レベルは1200として設定する
    val maxLevel: Int = 1200,
) {
    private val descriptionSubclasses: List<PaperMessageKey>

    init {
        descriptionSubclasses = findDescriptionSubclasses(description::class)
    }

    fun getName(player: PaperPlayer): TextComponent {
        return name.t(player)
    }

    fun getDescription(player: PaperPlayer): TextComponent {
        return descriptionSubclasses.random().t(player)
    }

    private fun findDescriptionSubclasses(descriptionClass: KClass<out PaperMessageKey>): List<PaperMessageKey> {
        Logger.logIfDebug("Searching for subclasses of: ${descriptionClass.simpleName}")

        // 渡されたクラス自体が object インスタンスかチェック
        val selfInstance = descriptionClass.objectInstance
        if (selfInstance != null) {
            Logger.logIfDebug("Found a single object instance for ${descriptionClass.simpleName}")
            return listOf(selfInstance) // オブジェクトインスタンスがあれば、それだけを返す
            // これは、Descが一個のみの場合に機能する。
        }

        // ネストされたクラスを検索
        val subclasses = descriptionClass.nestedClasses
            .filter { it.objectInstance != null && it.isSubclassOf(PaperMessageKey::class) }
            .mapNotNull { it.objectInstance as? PaperMessageKey }

        Logger.logIfDebug("Found ${subclasses.size} subclasses for ${descriptionClass.simpleName}")
        return subclasses
    }
}