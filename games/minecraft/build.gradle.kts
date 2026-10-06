plugins {
    id("buildlogic.java-platform-conventions")
    id("buildlogic.java-publish-conventions")
}

object Libs {
    // Dependencies
    const val CLOUD = "2.0.0"
    const val CLOUD_EXTRAS = "2.0.0-beta.11"
    const val CLOUD_CONFIRMATION = "1.0.0-rc.1"
}

dependencies {
    api(project(":common"))

    // Cloud
    api("org.incendo:cloud-annotations:${Libs.CLOUD}") {
        exclude("io.leangen.geantyref")
    }
    testImplementation("org.incendo:cloud-annotations:${Libs.CLOUD}") {
        exclude("io.leangen.geantyref")
    }
    annotationProcessor("org.incendo:cloud-annotations:${Libs.CLOUD}") {
        exclude("io.leangen.geantyref")
    }
    api("org.incendo:cloud-minecraft-extras:${Libs.CLOUD_EXTRAS}") {
        exclude("io.leangen.geantyref")
    }
    testImplementation("org.incendo:cloud-minecraft-extras:${Libs.CLOUD_EXTRAS}") {
        exclude("io.leangen.geantyref")
    }
    api("org.incendo:cloud-brigadier:${Libs.CLOUD_EXTRAS}") {
        exclude("io.leangen.geantyref")
    }
    testImplementation("org.incendo:cloud-brigadier:${Libs.CLOUD_EXTRAS}") {
        exclude("io.leangen.geantyref")
    }
    api("org.incendo:cloud-processors-confirmation:${Libs.CLOUD_CONFIRMATION}") {
        exclude("io.leangen.geantyref")
    }
    testImplementation("org.incendo:cloud-processors-confirmation:${Libs.CLOUD_CONFIRMATION}") {
        exclude("io.leangen.geantyref")
    }

    compileOnly("org.slf4j:slf4j-api:2.0.12")
    testImplementation("org.slf4j:slf4j-api:2.0.12")
    testImplementation("org.slf4j:slf4j-simple:2.0.17")
    testImplementation("net.kyori:adventure-text-serializer-plain:4.22.0")
    testImplementation("net.kyori:adventure-text-minimessage:4.22.0")
}
