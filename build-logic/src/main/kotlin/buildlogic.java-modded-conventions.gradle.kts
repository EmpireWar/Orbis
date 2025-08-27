plugins {
    id("buildlogic.java-common-conventions")
    id("com.gradleup.shadow")
    id("dev.architectury.loom")
    id("architectury-plugin")
}

architectury {
    minecraft = "1.21.8"
}

loom {
    silentMojangMappingsLicense()
}
dependencies {
    minecraft("net.minecraft:minecraft:1.21.8")
    mappings(loom.officialMojangMappings())
}
