plugins {
    id("buildlogic.java-common-conventions")
}

repositories {
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    implementation(project(":common"))
    compileOnly("io.papermc.paper:paper-api:1.21.4-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.15-SNAPSHOT")
}
