plugins {
    // Apply the common convention plugin for shared build configuration between library and application projects.
    id("buildlogic.java-common-conventions")
}

dependencies {
    implementation("org.incendo:cloud-annotations:2.0.0-rc.2")
    annotationProcessor("org.incendo:cloud-annotations:2.0.0-rc.2")
    implementation("org.incendo:cloud-minecraft-extras:2.0.0-beta.9")
    implementation("org.incendo:cloud-brigadier:2.0.0-beta.9")
}

tasks {
    shadowJar {
        relocate("org.incendo.cloud", "org.empirewar.orbis.${project.name}.libs.cloud")
    }
}
