plugins {
    id("buildlogic.java-modded-conventions")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

val common by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

val shadowBundle by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
}

configurations {
    compileClasspath {
        extendsFrom(common)
    }
    runtimeClasspath {
        extendsFrom(common)
    }
    // Adjust 'developmentFabric' if it's a valid configuration
    configurations.findByName("developmentFabric")?.extendsFrom(common)
}

base {
    archivesName = "orbis-${project.name}"
}

dependencies {
    modImplementation("org.incendo:cloud-fabric:2.0.0-beta.12") {
        exclude("net.fabricmc.fabric-api")
    }

    modImplementation("net.fabricmc:fabric-loader:0.16.14")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.119.3+1.21.4")

    compileOnly("org.slf4j:slf4j-api:2.0.12")
    // Loom project
    modImplementation(include("net.kyori:adventure-platform-fabric:6.3.0")!!) // for Minecraft 1.21.4
    modImplementation(include("me.lucko:fabric-permissions-api:0.3.3")!!)
    implementation("org.spongepowered:configurate-yaml:4.1.2")
    include(project(":common"))?.let { include(it) }
    implementation(project(":common"))
//    implementation(project(":api:fabric-api", "jar"))

    common(project(":platforms:modded:modded-common", "namedElements")) {
        isTransitive = false
    }
    shadowBundle(project(":platforms:modded:modded-common", "transformProductionFabric"))
}

tasks {
    remapJar {
        inputs.file(shadowJar.get().archiveFile)
        inputFile = shadowJar.get().archiveFile
    }

    shadowJar {
        configurations = listOf(shadowBundle)
        archiveClassifier.set("dev-shadow")
    }

    processResources {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }
}
