plugins {
    id("buildlogic.java-publish-conventions")
    id("buildlogic.java-platform-conventions")
}

dependencies {
    compileOnly("net.kyori:adventure-text-serializer-gson:4.16.0")
    compileOnly("org.slf4j:slf4j-api:2.0.12")
}
