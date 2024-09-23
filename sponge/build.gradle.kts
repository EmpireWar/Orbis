import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.model.PluginDependency

plugins {
    id("buildlogic.java-platform-conventions")
    id("org.spongepowered.gradle.plugin") version("2.2.0")
}

dependencies {
    compileOnly("org.slf4j:slf4j-api:2.0.12")
    implementation("org.incendo:cloud-sponge:2.0.0-SNAPSHOT") {
        exclude("io.leangen.geantyref")
    }
    implementation(project(":common"))
    implementation(project(":api:sponge-api"))
}

sponge {
    apiVersion("12.0.0-SNAPSHOT")
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
