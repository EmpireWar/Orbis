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
    modImplementation("net.fabricmc:fabric-loader:0.16.14")

    // Fabric API. This is technically optional, but you probably want it anyway.
    modImplementation("net.fabricmc.fabric-api:fabric-api:0.119.3+1.21.4")
}
