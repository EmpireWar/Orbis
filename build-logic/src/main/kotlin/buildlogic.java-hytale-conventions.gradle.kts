plugins {
    java
    id("com.azuredoom.hytale-tools")
}

// The Hytale toolchain (and the plugin's decompilation tooling) requires Java 25.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

hytaleTools {
    // Resolves com.hypixel.hytale:Server from the Hytale maven for us.
    hytaleVersion = "0.+"
    patchline = "release"
    javaVersion = 25

    manifestGroup = "org.empirewar"
}
