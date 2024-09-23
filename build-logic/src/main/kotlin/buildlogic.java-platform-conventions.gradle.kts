plugins {
    // Apply the common convention plugin for shared build configuration between library and application projects.
    id("buildlogic.java-common-conventions")
}

dependencies {
    implementation("org.incendo:cloud-annotations:2.0.0-rc.2")
    annotationProcessor("org.incendo:cloud-annotations:2.0.0-rc.2")
    implementation("org.incendo:cloud-minecraft-extras:2.0.0-beta.9")
    implementation("org.incendo:cloud-brigadier:2.0.0-beta.9")
    implementation("org.incendo:cloud-processors-confirmation:1.0.0-beta.3")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")
}

tasks {
    shadowJar {
        val root = "org.empirewar.orbis.${project.name}.libs"
        relocate("org.incendo.cloud", "$root.cloud")
        relocate("io.leangen", "$root.leangen")
        relocate("org.checkerframework", "$root.checker")
        relocate("com.github.benmanes.caffeine.cache", "$root.caffeine")
        relocate("com.google", "$root.google")
    }
}
