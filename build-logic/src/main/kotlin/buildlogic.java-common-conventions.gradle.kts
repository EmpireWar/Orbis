plugins {
    // Apply the java Plugin to add support for Java.
    java
    id("com.gradleup.shadow")
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

project.version = "1.0.0-SNAPSHOT"

dependencies {
    constraints {
        // Define dependency versions as constraints
        compileOnly("org.joml:joml:1.10.5")
        testImplementation("org.joml:joml:1.10.5")
        compileOnly("net.kyori:adventure-api:4.17.0")
        testImplementation("net.kyori:adventure-api:4.17.0")
    }

    compileOnly("org.spongepowered:configurate-yaml:4.1.2")
    testImplementation("org.spongepowered:configurate-yaml:4.1.2")
    compileOnly("net.kyori:adventure-api")
    testImplementation("net.kyori:adventure-api")
    compileOnly("org.joml:joml")
    testImplementation("org.joml:joml")
    compileOnly("com.mojang:datafixerupper:8.0.16")
    testImplementation("com.mojang:datafixerupper:8.0.16")
    compileOnly("com.google.code.gson:gson:2.10.1")
    testImplementation("com.google.code.gson:gson:2.10.1")
}

testing {
    suites {
        // Configure the built-in test suite
        val test by getting(JvmTestSuite::class) {
            // Use JUnit Jupiter test framework
            useJUnitJupiter("5.10.1")
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
        dependsOn(shadowJar)
    }

    shadowJar {
        mergeServiceFiles()
        archiveBaseName.set("orbis-${project.name}")
        archiveClassifier.set("")
    }
}