
plugins {
    id("buildlogic.java-platform-conventions")
    id("buildlogic.java-publish-conventions")
    id("buildlogic.java-hytale-conventions")
}

object Libs {
    // Dependencies
    const val JOML = "1.10.5"
    const val ADVENTURE = "4.22.0"
    const val CONFIGURATE = "4.1.2"
    const val DATAFIXERUPPER = "8.0.16"
}

hytaleTools {
    modId = "Orbis"
    mainClass = "org.empirewar.orbis.hytale.OrbisHytale"
    modDescription = "A modern, multi-platform region protection plugin for Hytale & Minecraft."
    modUrl = "https://github.com/EmpireWar/Orbis"
    modCredits = "SamB440||https://github.com/SamB440"
    manifestServerVersion = ">=0.0.1"

    // Orbis ships its own UI/language assets under src/main/resources.
    includesPack = true

    // Orbis has no runtime plugin dependencies.
    manifestDependencies = ""
}

configurations {
    // The Hytale server bundles its own (newer) Gson. Shipping or staging ours puts an older
    // copy of com.google.gson ahead of the server's, which breaks its own JSON loaders.
    runtimeClasspath { exclude(group = "com.google.code.gson", module = "gson") }
}

dependencies {
    api(project(":common"))

    // Sponge
    implementation("org.spongepowered:configurate-yaml:${Libs.CONFIGURATE}")

    // Kyori Adventure
    implementation("net.kyori:adventure-api:${Libs.ADVENTURE}")
    implementation("net.kyori:adventure-text-serializer-gson:${Libs.ADVENTURE}")
    implementation("net.kyori:adventure-text-serializer-plain:${Libs.ADVENTURE}")

    // JOML
    implementation("org.joml:joml:${Libs.JOML}")

    // Mojang
    implementation("com.mojang:datafixerupper:${Libs.DATAFIXERUPPER}")

    // Google
    // The Hytale server bundles its own (newer) Gson - shipping ours would shadow it.
    compileOnly("com.google.code.gson:gson:2.10.1")

    implementation("org.slf4j:slf4j-api:2.0.12")
    testImplementation("org.slf4j:slf4j-api:2.0.12")
    testImplementation("org.slf4j:slf4j-simple:2.0.17")
}

tasks {
    shadowJar {
        val root = "org.empirewar.orbis.${project.name}.libs"
        relocate("org.joml", "$root.joml")
        relocate("net.kyori", "$root.adventure")
        relocate("org.spongepowered.configurate", "$root.configurate")
        relocate("com.mojang", "$root.mojang")
        relocate("com.google.common", "$root.google.common")
        relocate("org.slf4j", "$root.slf4j")
        relocate("org.yaml.snakeyaml", "$root.snakeyaml")
        relocate("it.unimi.dsi.fastutil", "$root.fastutil")
        relocate("io.leangen.geantyref", "$root.geantyref")
    }
}
