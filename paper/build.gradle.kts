plugins {
    id("buildlogic.java-common-conventions")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    implementation("org.incendo:cloud-paper:2.0.0-beta.5")
    implementation("org.incendo:cloud-annotations:2.0.0-beta.5")
    implementation("org.incendo:cloud-minecraft-extras:2.0.0-beta.5")
    implementation(project(":common"))
    implementation(project(":api:paper-api"))
    implementation(project(":migrators:worldguard"))
}

tasks {
    shadowJar {
        relocate("org.incendo.cloud", "org.empirewar.orbis.paper.libs.cloud")
    }

    processResources {
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }
}
