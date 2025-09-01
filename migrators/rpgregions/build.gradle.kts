plugins {
    id("buildlogic.java-common-conventions")
}

repositories {
    maven("https://repo.convallyria.com/releases")
}

dependencies {
    implementation(project(":common"))
    compileOnly("net.islandearth.rpgregions:api:1.4.92")
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
}
