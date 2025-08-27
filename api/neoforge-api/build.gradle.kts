plugins {
    id("buildlogic.java-publish-conventions")
    id("buildlogic.java-modded-conventions")
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

val neoVersion = "21.8.9"

dependencies {
    api(project(":common"))

    neoForge("net.neoforged:neoforge:$neoVersion")
}

val shadowBundle by configurations.creating {
    isCanBeResolved = true
    isCanBeConsumed = false
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
}
