plugins {
    id("buildlogic.java-common-conventions")
    id("com.gradleup.shadow")
    id("dev.architectury.loom")
    id("architectury-plugin")
}

val minecraftVersion = "1.21.8"

architectury {
    minecraft = minecraftVersion
}

loom {
    silentMojangMappingsLicense()
}

dependencies {
    minecraft("net.minecraft:minecraft:$minecraftVersion")
    mappings(loom.officialMojangMappings())
}
