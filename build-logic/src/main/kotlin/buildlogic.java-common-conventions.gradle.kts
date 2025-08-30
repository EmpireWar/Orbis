plugins {
    // Apply the java Plugin to add support for Java.
    java
    id("com.diffplug.spotless")
    id("net.kyori.indra.licenser.spotless")
}

spotless {
    java {
        toggleOffOn("@formatter:off", "@formatter:on")
        palantirJavaFormat().style("AOSP")
        leadingTabsToSpaces(4)
        trimTrailingWhitespace()
        formatAnnotations()
        removeUnusedImports()
        endWithNewline()
        toggleOffOn()
    }

    kotlinGradle {
        endWithNewline()
        leadingTabsToSpaces(4)
        trimTrailingWhitespace()
    }
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") // Paper
    maven("https://repo.empirewar.org/snapshots/")
}

object Libs {
    // Dependencies
    const val JOML = "1.10.5"
    const val ADVENTURE = "4.22.0"
    const val CONFIGURATE = "4.1.2"
    const val DATAFIXERUPPER = "8.0.16"
    const val RTREE_MULTI = "0.1"
    const val CLOUD = "2.0.0"
    const val CLOUD_EXTRAS = "2.0.0-beta.11"
    const val CLOUD_CONFIRMATION = "1.0.0-rc.1"
    const val CAFFEINE = "3.1.8"
    
    // Test
    const val JUNIT_JUPITER = "5.10.1"
}

project.version = "1.0.1-SNAPSHOT"

dependencies {
    constraints {
        // Define dependency versions as constraints
        compileOnly("org.joml:joml:${Libs.JOML}")
        testImplementation("org.joml:joml:${Libs.JOML}")
        compileOnly("net.kyori:adventure-api:${Libs.ADVENTURE}")
        testImplementation("net.kyori:adventure-api:${Libs.ADVENTURE}")
    }

    // Sponge
    compileOnly("org.spongepowered:configurate-yaml:${Libs.CONFIGURATE}")
    testImplementation("org.spongepowered:configurate-yaml:${Libs.CONFIGURATE}")
    
    // Kyori Adventure
    compileOnly("net.kyori:adventure-api:${Libs.ADVENTURE}")
    testImplementation("net.kyori:adventure-api:${Libs.ADVENTURE}")
    compileOnly("net.kyori:adventure-text-serializer-gson:${Libs.ADVENTURE}")
    testImplementation("net.kyori:adventure-text-serializer-gson:${Libs.ADVENTURE}")
    
    // JOML
    compileOnly("org.joml:joml:${Libs.JOML}")
    testImplementation("org.joml:joml:${Libs.JOML}")
    
    // Mojang
    compileOnly("com.mojang:datafixerupper:${Libs.DATAFIXERUPPER}")
    testImplementation("com.mojang:datafixerupper:${Libs.DATAFIXERUPPER}")
    
    // Google
    compileOnly("com.google.code.gson:gson:2.10.1")
    testImplementation("com.google.code.gson:gson:2.10.1")

    // RTree
    implementation("com.github.davidmoten:rtree-multi:${Libs.RTREE_MULTI}")
    testImplementation("com.github.davidmoten:rtree-multi:${Libs.RTREE_MULTI}")

    // Cloud
    implementation("org.incendo:cloud-annotations:${Libs.CLOUD}") {
        exclude("io.leangen.geantyref")
    }
    testImplementation("org.incendo:cloud-annotations:${Libs.CLOUD}") {
        exclude("io.leangen.geantyref")
    }
    annotationProcessor("org.incendo:cloud-annotations:${Libs.CLOUD}") {
        exclude("io.leangen.geantyref")
    }
    implementation("org.incendo:cloud-minecraft-extras:${Libs.CLOUD_EXTRAS}") {
        exclude("io.leangen.geantyref")
    }
    testImplementation("org.incendo:cloud-minecraft-extras:${Libs.CLOUD_EXTRAS}") {
        exclude("io.leangen.geantyref")
    }
    implementation("org.incendo:cloud-brigadier:${Libs.CLOUD_EXTRAS}") {
        exclude("io.leangen.geantyref")
    }
    testImplementation("org.incendo:cloud-brigadier:${Libs.CLOUD_EXTRAS}") {
        exclude("io.leangen.geantyref")
    }
    implementation("org.incendo:cloud-processors-confirmation:${Libs.CLOUD_CONFIRMATION}") {
        exclude("io.leangen.geantyref")
    }
    testImplementation("org.incendo:cloud-processors-confirmation:${Libs.CLOUD_CONFIRMATION}") {
        exclude("io.leangen.geantyref")
    }
    
    // Caffeine
    implementation("com.github.ben-manes.caffeine:caffeine:${Libs.CAFFEINE}")
    testImplementation("com.github.ben-manes.caffeine:caffeine:${Libs.CAFFEINE}")
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter(Libs.JUNIT_JUPITER)
        }
    }
}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

tasks {
    build {
        dependsOn(spotlessApply)
    }
}