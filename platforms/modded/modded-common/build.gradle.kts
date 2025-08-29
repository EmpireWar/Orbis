plugins {
    id("buildlogic.java-modded-conventions")
}

architectury {
    common(listOf("fabric", "neoforge"))
}

dependencies {
    // We depend on Fabric Loader here to use the Fabric @Environment annotations,
    // which get remapped to the correct annotations on each platform.
    // Do NOT use other classes from Fabric Loader.
    modImplementation("net.fabricmc:fabric-loader:0.17.2")

    modCompileOnly("net.kyori:adventure-platform-mod-shared-fabric-repack:6.6.0") // for Minecraft 1.21.8

    // Architectury API. This is optional, and you can comment it out if you don't need it.
//    modImplementation("dev.architectury:architectury:${rootProject.extra["architectury_api_version"]}")
}

dependencies {
    // Mixin annotations
    compileOnly("org.spongepowered:mixin:0.8.7")

    compileOnly(project(":common"))
}
