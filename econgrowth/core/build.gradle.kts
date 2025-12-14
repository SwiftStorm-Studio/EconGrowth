import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar.Companion.shadowJar
import dev.swiftstorm.akkaradb.plugin.akkara

dependencies {
    akkara("0.3.1", "implementation")
}

tasks.shadowJar {
}