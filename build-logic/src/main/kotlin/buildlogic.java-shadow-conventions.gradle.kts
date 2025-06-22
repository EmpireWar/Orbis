plugins {
    id("com.gradleup.shadow")
    id("buildlogic.java-common-conventions")
}

tasks {
    build {
        if (tasks.findByName("remapJar") == null) {
            dependsOn(shadowJar)
        }
    }

    shadowJar {
        mergeServiceFiles()
        archiveBaseName.set("orbis-${project.name}")
        archiveClassifier.set("")

        val root = "org.empirewar.orbis.${project.name}.libs"
        relocate("org.incendo.cloud", "$root.cloud")
        relocate("org.checkerframework", "$root.checker")
        relocate("com.github.benmanes.caffeine.cache", "$root.caffeine")
        relocate("com.google.errorprone", "$root.errorprone")
        relocate("com.github.davidmoten.rtreemulti", "$root.rtree")
        relocate("com.github.davidmoten.guavamini", "$root.rtree.guava")
    }
}
