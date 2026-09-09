plugins {
    id("buildlogic.java-common-conventions")
    id("buildlogic.java-minecraft-platform-conventions")
}

repositories {
    maven("https://maven.enginehub.org/repo/")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.21.11-R0.1-SNAPSHOT")
    compileOnly("com.sk89q.worldguard:worldguard-bukkit:7.0.15-SNAPSHOT")
}
