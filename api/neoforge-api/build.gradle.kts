plugins {
    id("buildlogic.java-publish-conventions")
    id("buildlogic.java-modded-conventions")
}

architectury {
    platformSetupLoomIde()
    neoForge()
}

val neoVersion = "21.4.149"

dependencies {
    api(project(":common"))

    neoForge("net.neoforged:neoforge:$neoVersion")
}
