plugins {
    id("buildlogic.java-common-conventions")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    implementation("org.incendo:cloud-paper:2.0.0-beta.7")
    implementation("org.incendo:cloud-annotations:2.0.0-rc.1")
    implementation("org.incendo:cloud-minecraft-extras:2.0.0-beta.7")
    implementation(project(":bukkitlike"))
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
