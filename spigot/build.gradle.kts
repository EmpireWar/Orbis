plugins {
    id("buildlogic.java-common-conventions")
}

repositories {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.slf4j:slf4j-api:2.0.12")
    compileOnly("org.spigotmc:spigot-api:1.20.6-R0.1-SNAPSHOT")
    // It's really impressive how adventure-platform-bukkit is using adventure 4.13, so we have to include 4.17
    // Supporting Spigot is so much fun!
    implementation("net.kyori:adventure-api:4.17.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.17.0")
    implementation("org.spongepowered:configurate-yaml:4.1.2")
    implementation("org.incendo:cloud-paper:2.0.0-beta.5")
    implementation("org.incendo:cloud-annotations:2.0.0-beta.5")
    implementation("org.incendo:cloud-minecraft-extras:2.0.0-beta.5")
    implementation("net.kyori:adventure-platform-bukkit:4.3.3-SNAPSHOT")
    implementation(project(":bukkitlike"))
    implementation(project(":common"))
    implementation(project(":api:paper-api"))
    implementation(project(":migrators:worldguard"))
}

tasks {
    shadowJar {
        relocate("org.incendo.cloud", "org.empirewar.orbis.spigot.libs.cloud")
        relocate("net.kyori", "org.empirewar.orbis.spigot.libs.adventure")
    }

    processResources {
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }
}
