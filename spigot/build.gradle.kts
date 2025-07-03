plugins {
    id("buildlogic.java-platform-conventions")
}

repositories {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.slf4j:slf4j-api:2.0.12")
    compileOnly("org.spigotmc:spigot-api:1.21.4-R0.1-SNAPSHOT")
    // Make sure to keep the adventure api the same version that adventure-platform-bukkit uses.
    implementation("net.kyori:adventure-api:4.21.0")
    implementation("net.kyori:adventure-text-serializer-legacy:4.21.0")
    implementation("org.spongepowered:configurate-yaml:4.1.2")
    implementation("org.incendo:cloud-paper:2.0.0-beta.10")
    implementation("net.kyori:adventure-platform-bukkit:4.4.0")
    implementation("net.kyori:adventure-text-minimessage:4.21.0")
    implementation(project(":bukkitlike"))
    implementation(project(":common"))
    implementation(project(":api:paper-api"))
    implementation(project(":migrators:worldguard"))
}

tasks {
    shadowJar {
        relocate("net.kyori", "org.empirewar.orbis.spigot.libs.adventure")
    }

    processResources {
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }
}
