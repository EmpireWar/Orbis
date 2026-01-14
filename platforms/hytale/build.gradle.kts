plugins {
    id("buildlogic.java-platform-conventions")
    id("buildlogic.java-hytale-conventions")
}

dependencies {
    implementation(project(":api:hytale-api"))
}
