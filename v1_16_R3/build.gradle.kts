description = "v1_16_R3"

repositories {
    mavenLocal()
}

dependencies {
    compileOnly("org.spigotmc:spigot:1.16.4-R0.1-SNAPSHOT")
    compileOnly(project(":api"))
}
