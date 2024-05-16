plugins {
    id("buildlogic.java-publish-conventions")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.6-R0.1-SNAPSHOT")
    api(project(":common"))
}
