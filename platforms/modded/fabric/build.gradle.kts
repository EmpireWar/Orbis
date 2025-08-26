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

repositories {
    maven("https://www.jitpack.io")
}

dependencies {
    modImplementation("net.fabricmc:fabric-loader:0.16.14")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.119.3+1.21.4")

    shadowBundle(project(":common"))
    implementation(project(":common"))

    shadowBundle(project(":api:fabric-api", "namedElements"))
    implementation(project(":api:fabric-api"))

    common(project(":platforms:modded:modded-common", "namedElements")) {
        isTransitive = false
    }
    shadowBundle(project(":platforms:modded:modded-common", "transformProductionFabric"))

    shadowBundle("org.spongepowered:configurate-yaml:4.1.2") {
        exclude("io.leangen.geantyref")
    }

    // Words cannot express how terrible the jar-in-jar system is
    // We have to do this hacky mess because it doesn't support transitive dependencies
    // And these are also dependencies from common-conventions that need declaring with "include"
    // I tried everything but this is the only way, all other ways would result in remapping issues or missing classes
    modImplementation(include("org.incendo:cloud-annotations:2.0.0")!!)
    modImplementation(include("org.incendo:cloud-minecraft-extras:2.0.0-beta.11")!!)
    modImplementation(include("org.incendo:cloud-brigadier:2.0.0-beta.11")!!)
    modImplementation(include("org.incendo:cloud-processors-confirmation:1.0.0-rc.1")!!)
    modImplementation(include("org.incendo:cloud-processors-common:1.0.0-rc.1")!!)
    modImplementation(include("com.github.ben-manes.caffeine:caffeine:3.1.8")!!)

    modImplementation(include("com.github.Incendo.cloud-minecraft-modded:cloud-fabric:3ac0d7cd94") {
        exclude("net.fabricmc.fabric-api")
    })
    modImplementation(include("net.kyori:adventure-platform-fabric:6.3.0")!!) // for Minecraft 1.21.4
    modImplementation(include("me.lucko:fabric-permissions-api:0.3.3")!!)

    compileOnly("org.slf4j:slf4j-api:2.0.12")
}

tasks {
    remapJar {
        inputs.file(shadowJar.get().archiveFile)
        inputFile = shadowJar.get().archiveFile
    }

    shadowJar {
        dependencies {
            exclude(dependency("org.incendo:.*"))
            exclude(dependency("com.github.ben-manes.caffeine:.*"))
        }

        configurations = listOf(shadowBundle)
        archiveClassifier.set("dev-shadow")

        val root = "org.empirewar.orbis.${project.name}.libs"
        relocate("org.spongepowered.configurate", "$root.configurate")
        relocate("org.yaml.snakeyaml", "$root.snakeyaml")
        relocate("org.checkerframework", "$root.checker")
        relocate("com.google.errorprone", "$root.errorprone")
        relocate("com.github.davidmoten.rtreemulti", "$root.rtree")
        relocate("com.github.davidmoten.guavamini", "$root.rtree.guava")
    }

    processResources {
        inputs.property("version", project.version)

        filesMatching("fabric.mod.json") {
            expand("version" to project.version)
        }
    }
}
