plugins {
    // Apply the java Plugin to add support for Java.
    java
    id("io.github.goooler.shadow")
    id("com.diffplug.spotless")
    id("net.kyori.indra.licenser.spotless")
}

spotless {
    java {
        toggleOffOn("@formatter:off", "@formatter:on")
        palantirJavaFormat().style("AOSP")
        indentWithSpaces(4)
        trimTrailingWhitespace()
        formatAnnotations()
        removeUnusedImports()
        endWithNewline()
    }

    kotlinGradle {
        endWithNewline()
        indentWithSpaces(4)
        trimTrailingWhitespace()
    }
}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/") // Paper
}

project.version = "1.0.0-SNAPSHOT"

dependencies {
    constraints {
        // Define dependency versions as constraints
        implementation("org.apache.commons:commons-text:1.11.0")
        implementation("org.joml:joml:1.10.5")
        testImplementation("org.joml:joml:1.10.5")
        compileOnly("net.kyori:adventure-api:4.16.0")
    }

    compileOnly("net.kyori:adventure-api")
    compileOnly("org.joml:joml")
    testImplementation("org.joml:joml")
    compileOnly("com.mojang:datafixerupper:6.0.8")
    testImplementation("com.mojang:datafixerupper:6.0.8")
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
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
}