
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

dependencies {
    api(project(":common"))

    // Sponge
    implementation("org.spongepowered:configurate-yaml:${Libs.CONFIGURATE}")

    // Kyori Adventure
    implementation("net.kyori:adventure-api:${Libs.ADVENTURE}")
    implementation("net.kyori:adventure-text-serializer-gson:${Libs.ADVENTURE}")

    // JOML
    implementation("org.joml:joml:${Libs.JOML}")

    // Mojang
    implementation("com.mojang:datafixerupper:${Libs.DATAFIXERUPPER}")

    // Google
    implementation("com.google.code.gson:gson:2.10.1")

    implementation("org.slf4j:slf4j-api:2.0.12")
    testImplementation("org.slf4j:slf4j-api:2.0.12")
    testImplementation("org.slf4j:slf4j-simple:2.0.17")
}

tasks {
    processResources {
        val replaceProperties = mapOf(
            "version" to project.version
        )

        inputs.properties(replaceProperties)

        filesMatching("manifest.json") {
            expand(replaceProperties)
        }
    }
}
