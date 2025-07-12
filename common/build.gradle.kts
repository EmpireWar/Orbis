plugins {
    id("buildlogic.java-platform-conventions")
    id("buildlogic.java-publish-conventions")
}

dependencies {
    compileOnly("org.slf4j:slf4j-api:2.0.12")
    testImplementation("org.slf4j:slf4j-api:2.0.12")
    testImplementation("org.slf4j:slf4j-simple:2.0.17")
    testImplementation("net.kyori:adventure-text-serializer-plain:4.22.0")
}
