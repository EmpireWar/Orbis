
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

val neoVersion = "21.8.9"

repositories {
    maven("https://maven.neoforged.net/releases")
    maven("https://www.jitpack.io")
}

dependencies {
    neoForge("net.neoforged:neoforge:$neoVersion")

    // Add :common as a dependency
    shadowBundle(project(":common")) {
        isTransitive = false
    }
    implementation(project(":common"))

    // JiJ all runtime dependencies from :common
    afterEvaluate {
        // Get the resolved runtimeClasspath of :common
        val commonRuntimeClasspath = project(":common").configurations.getByName("runtimeClasspath")
        val visited = mutableSetOf<String>()

        fun includeAllTransitives(dep: ResolvedDependency) {
            val group = dep.moduleGroup
            val name = dep.moduleName
            val version = dep.moduleVersion
            val notation = "$group:$name:$version"

            // Exclusions
            if (group.startsWith("com.google")) return
            if (group.startsWith("org.checkerframework")) return

            // Skip BOM/platform dependencies (they have no artifacts or all artifacts are pom)
            val isBom = dep.moduleArtifacts.all { it.type == "pom" }
            if (notation !in visited && name != "common" && !isBom) {
                println("JiJ'ing $notation")
                include(notation)
                visited.add(notation)
                dep.children.forEach { includeAllTransitives(it) }
            }
        }

        fun printDeps(dep: ResolvedDependency, indent: String = "") {
            println("$indent${dep.moduleGroup}:${dep.moduleName}:${dep.moduleVersion}")
            dep.children.forEach { printDeps(it, "$indent  ") }
        }
        commonRuntimeClasspath.resolvedConfiguration.firstLevelModuleDependencies.forEach { dep ->
            printDeps(dep)
        }

        commonRuntimeClasspath.resolvedConfiguration.firstLevelModuleDependencies.forEach { dep ->
            includeAllTransitives(dep)
        }
    }

    shadowBundle(project(":api:neoforge-api", "namedElements")) {
        isTransitive = false
    }
    implementation(project(":api:neoforge-api"))

    common(project(":platforms:modded:modded-common", "namedElements")) {
        isTransitive = false
    }
    shadowBundle(project(":platforms:modded:modded-common", "transformProductionNeoForge"))

    includeTransitively("org.spongepowered", "configurate-yaml", "4.1.2") { group, name ->
        group == "io.leangen" && name == "geantyref"
    }
    forgeRuntimeLibrary("org.spongepowered:configurate-yaml:4.1.2")

    modImplementation(include("com.github.Incendo.cloud-minecraft-modded:cloud-neoforge:3ac0d7cd94")!!)
    modImplementation(include("net.kyori:adventure-platform-neoforge:6.6.0")!!) // for Minecraft 1.21.4
}

fun Project.includeTransitively(
    group: String,
    name: String,
    version: String,
    exclude: (String, String) -> Boolean = { _, _ -> false }
) {
    val depNotation = "$group:$name:$version"
    val config = configurations.detachedConfiguration(
        dependencies.create(depNotation)
    )
    config.resolvedConfiguration.firstLevelModuleDependencies.forEach { dep ->
        fun recurse(d: ResolvedDependency) {
            if (!exclude(d.moduleGroup, d.moduleName)) {
                dependencies.include("${d.moduleGroup}:${d.moduleName}:${d.moduleVersion}")
                d.children.forEach { recurse(it) }
            }
        }
        recurse(dep)
    }
}

tasks {
    remapJar {
        inputs.file(shadowJar.get().archiveFile)
        inputFile = shadowJar.get().archiveFile
    }

    shadowJar {
        configurations = listOf(shadowBundle)
        archiveClassifier.set("dev-shadow")

        val root = "org.empirewar.orbis.${project.name}.libs"
//        relocate("org.spongepowered.configurate", "$root.configurate")
//        relocate("org.yaml.snakeyaml", "$root.snakeyaml")
//        relocate("org.checkerframework", "$root.checker")
//        relocate("com.google.errorprone", "$root.errorprone")
//        relocate("com.github.davidmoten.rtreemulti", "$root.rtree")
//        relocate("com.github.davidmoten.guavamini", "$root.rtree.guava")
    }

    processResources {
        inputs.property("version", project.version)

        val minecraftVersion = loom.minecraftVersion.get()

        val replaceProperties = mapOf(
            "minecraft_version" to minecraftVersion,
            "minecraft_version_range" to "[$minecraftVersion]",
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
