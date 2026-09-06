plugins {
    java
}

repositories {
    maven("https://maven.hytale.com/release")
}

dependencies {
    compileOnly("com.hypixel.hytale:Server:+")
}