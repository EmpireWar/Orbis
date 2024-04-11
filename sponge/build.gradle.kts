import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    id("buildlogic.java-common-conventions")
    id("org.spongepowered.gradle.plugin") version("2.2.0")
}

repositories {
    mavenLocal()
}

dependencies {
    implementation("org.incendo:cloud-sponge:2.0.0-SNAPSHOT")
    implementation("org.incendo:cloud-annotations:2.0.0-beta.4")
    implementation("org.incendo:cloud-minecraft-extras:2.0.0-beta.5")
    implementation(project(":common"))
}

sponge {
    apiVersion("11.0.0-SNAPSHOT")
    loader {
        name(PluginLoaders.JAVA_PLAIN)
        version("1.0.0-SNAPSHOT")
    }
    plugin("orbis") {
        displayName("Orbis")
        entrypoint("org.empirewar.orbis.sponge.OrbisSponge")
        description("A modern, multi-platform region protection plugin for Minecraft: Java Edition.")
        license("GPL-3.0")
        version(project.version.toString())
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
        contributors {
            contributor("SamB440") {}
            contributor("LimeeFox") {}
            contributor("StealWonders") {}
        }
    }
}

tasks {
    shadowJar {
        relocate("org.incendo.cloud", "com.convallyria.orbis.sponge.libs.cloud")
    }
}
