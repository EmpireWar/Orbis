
plugins {
    id("buildlogic.java-modded-conventions")
}

architectury {
    platformSetupLoomIde()
    neoForge()
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

    // Adjust 'developmentNeoForge' if it's a valid configuration
    configurations.findByName("developmentNeoForge")!!.extendsFrom(common)
}

base {
    archivesName.set("orbis-${project.name}")
}

val neoVersion = "21.4.149"

repositories {
    maven("https://maven.neoforged.net/releases")
    maven("https://www.jitpack.io")
}

dependencies {
    neoForge("net.neoforged:neoforge:$neoVersion")

    shadowBundle(project(":common"))
    implementation(project(":common"))

    shadowBundle(project(":api:neoforge-api", "namedElements"))
    implementation(project(":api:neoforge-api"))

    common(project(":platforms:modded:modded-common", "namedElements")) {
        isTransitive = false
    }
    shadowBundle(project(":platforms:modded:modded-common", "transformProductionNeoForge"))

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

    modImplementation(include("com.github.Incendo.cloud-minecraft-modded:cloud-neoforge:3ac0d7cd94")!!)
    modImplementation(include("net.kyori:adventure-platform-neoforge:6.3.0")!!) // for Minecraft 1.21.4
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

        val replaceProperties = mapOf(
            "minecraft_version" to "1.21.4",
            "minecraft_version_range" to "[1.21.4]",
            "neo_version" to neoVersion,
            "loader_version_range" to "[1,)",
            "mod_id" to rootProject.name.lowercase(),
            "mod_name" to rootProject.name,
            "mod_version" to project.version.toString(),
            "mod_authors" to "SamB440, LimeeFox, StealWonders",
            "mod_description" to "A modern, multi-platform region protection plugin for Minecraft: Java Edition."
        )

        inputs.properties(replaceProperties)

        filesMatching("META-INF/neoforge.mods.toml") {
            expand(replaceProperties)
        }
    }
}
