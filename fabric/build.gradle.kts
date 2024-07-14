plugins {
    id("buildlogic.java-platform-conventions")
    id("fabric-loom") version("1.6-SNAPSHOT")
}

repositories {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    val cloudFabric = "org.incendo:cloud-fabric:2.0.0-beta.7"
    modImplementation(cloudFabric)
    include(cloudFabric)

    minecraft("com.mojang:minecraft:1.20.6")
    mappings(loom.officialMojangMappings())
    modImplementation("net.fabricmc:fabric-loader:0.15.11")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.98.0+1.20.6")

    compileOnly("org.slf4j:slf4j-api:2.0.12")
    modImplementation("net.kyori:adventure-platform-fabric:5.12.0")?.let { include(it) }
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
