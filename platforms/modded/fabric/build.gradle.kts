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
    modImplementation("net.fabricmc:fabric-loader:0.17.2")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.132.0+1.21.8")

    // Add :common as a dependency
    shadowBundle(project(":games:minecraft")) {
        isTransitive = false
    }
    implementation(project(":games:minecraft"))

    // JiJ all runtime dependencies from :common
    afterEvaluate {
        // Get the resolved runtimeClasspath of :common
        val commonRuntimeClasspath = project(":games:minecraft").configurations.getByName("runtimeClasspath")
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

    shadowBundle(project(":api:fabric-api", "namedElements")) {
        isTransitive = false
    }
    implementation(project(":api:fabric-api"))

    common(project(":platforms:modded:modded-common", "namedElements")) {
        isTransitive = false
    }
    shadowBundle(project(":platforms:modded:modded-common", "transformProductionFabric"))

    includeTransitively("org.spongepowered", "configurate-yaml", "4.1.2") { group, name ->
        group == "io.leangen" && name == "geantyref"
    }

    modImplementation(include("org.incendo:cloud-fabric:2.0.0-beta.12") {
        exclude("net.fabricmc.fabric-api")
    })
    modImplementation(include("net.kyori:adventure-platform-fabric:6.7.0")!!)
    modImplementation(include("me.lucko:fabric-permissions-api:0.4.1")!!)

    compileOnly("org.slf4j:slf4j-api:2.0.12")
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
//        relocate("org.incendo.cloud", "$root.cloud")
//        relocate("com.github.benmanes.caffeine.cache", "$root.caffeine")
//        relocate("org.spongepowered.configurate", "$root.configurate")
//        relocate("org.yaml.snakeyaml", "$root.snakeyaml")
//        relocate("org.checkerframework", "$root.checker")
//        relocate("com.google.errorprone", "$root.errorprone")
//        relocate("com.github.davidmoten.rtreemulti", "$root.rtree")
//        relocate("com.github.davidmoten.guavamini", "$root.rtree.guava")
    }

    processResources {
        val minecraftVersion = loom.minecraftVersion.get()

        val replaceProperties = mapOf(
            "version" to project.version,
            "minecraft_version" to minecraftVersion
        )

        inputs.properties(replaceProperties)

        filesMatching("fabric.mod.json") {
            expand(replaceProperties)
        }
    }
}
