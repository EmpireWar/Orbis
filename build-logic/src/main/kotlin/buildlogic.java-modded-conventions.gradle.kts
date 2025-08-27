import gradle.kotlin.dsl.accessors._91768c27ec96c45229685dfdd2df225b.architectury
import gradle.kotlin.dsl.accessors._91768c27ec96c45229685dfdd2df225b.loom
import gradle.kotlin.dsl.accessors._91768c27ec96c45229685dfdd2df225b.mappings
import gradle.kotlin.dsl.accessors._91768c27ec96c45229685dfdd2df225b.minecraft

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
