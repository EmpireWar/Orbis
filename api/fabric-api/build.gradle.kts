plugins {
    id("buildlogic.java-publish-conventions")
    id("buildlogic.java-modded-conventions")
}

architectury {
    platformSetupLoomIde()
    fabric()
}

dependencies {
    api(project(":common"))
    modImplementation("net.fabricmc:fabric-loader:0.17.2")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.132.0+1.21.8")
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
