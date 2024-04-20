plugins {
    id("buildlogic.java-common-conventions")
}

dependencies {
    compileOnly("net.kyori:adventure-text-serializer-gson:4.16.0")
    compileOnly("org.incendo:cloud-annotations:2.0.0-beta.4")
    annotationProcessor("org.incendo:cloud-annotations:2.0.0-beta.4")
    compileOnly("org.incendo:cloud-minecraft-extras:2.0.0-beta.5")
    compileOnly("org.incendo:cloud-brigadier:2.0.0-beta.4")
    compileOnly("org.slf4j:slf4j-api:2.0.12")
}

java {
    withSourcesJar()
    withJavadocJar()
}
