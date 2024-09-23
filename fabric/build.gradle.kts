plugins {
    id("buildlogic.java-platform-conventions")
    id("fabric-loom") version("1.7-SNAPSHOT")
}

repositories {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    modImplementation("org.incendo:cloud-fabric:2.0.0-beta.9")?.let { include(it) }

    minecraft("com.mojang:minecraft:1.21.1")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.16.2")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.102.1+1.21.1")

    compileOnly("org.slf4j:slf4j-api:2.0.12")
    // Loom project
    modCompileOnly("net.kyori:adventure-platform-mod-shared-fabric-repack:6.0.0") // for Minecraft 1.21-1.21.1
    modImplementation("me.lucko:fabric-permissions-api:0.3.1")?.let { include(it) }
    implementation("org.incendo:cloud-annotations:2.0.0-rc.2")
    implementation("org.incendo:cloud-minecraft-extras:2.0.0-beta.9")
    implementation(project(":common"))
    implementation(project(":api:fabric-api"))
}

tasks {
    processResources {
        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }
}
