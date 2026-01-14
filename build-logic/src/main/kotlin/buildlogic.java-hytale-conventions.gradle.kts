plugins {
    java
}

val hytaleServerJarFile: String by project
val hytaleServerJar = rootDir.resolve(hytaleServerJarFile.trim())

dependencies {
    compileOnly(files(hytaleServerJar))
}