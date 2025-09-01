plugins {
    id("buildlogic.java-common-conventions")
}

repositories {
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
}

dependencies {
    compileOnly("org.slf4j:slf4j-api:2.0.12")
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("net.kyori:adventure-api:4.21.0")
    implementation("org.incendo:cloud-paper:2.0.0-beta.11")
    implementation("org.incendo:cloud-annotations:2.0.0")
    implementation("org.incendo:cloud-minecraft-extras:2.0.0-beta.11")
    implementation(project(":common"))
    implementation(project(":api:paper-api"))
    implementation(project(":migrators:worldguard"))
    implementation(project(":migrators:rpgregions"))
}
