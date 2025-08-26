plugins {
    id("buildlogic.java-publish-conventions")
    id("buildlogic.java-modded-conventions")
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

val neoVersion = "21.8.38"

dependencies {
    api(project(":common"))

    neoForge("net.neoforged:neoforge:$neoVersion")
}
