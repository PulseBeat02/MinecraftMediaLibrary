description = "minecraftmedialibrary"

dependencies {

    compileOnlyApi("org.spigotmc:spigot-api:1.16.5-R0.1-SNAPSHOT")
    implementation(
        "io.github.slimjar:slimjar:1.2.0"
    )
    compileOnly("uk.co.caprica:vlcj:4.7.1")
    compileOnly("uk.co.caprica:vlcj-natives:4.1.0")
    compileOnly("uk.co.caprica:native-streams:1.0.0")
    compileOnly("com.github.sealedtx:java-youtube-downloader:2.5.2")
    compileOnly("ws.schild:jave-core:3.0.1")
    compileOnly("io.netty:netty-all:5.0.0.Alpha2")
    compileOnly("com.mojang:authlib:1.5.25")
    compileOnly("org.jetbrains:annotations:20.1.0")
    compileOnly("com.github.pulsebeat02:jarchivelib:master-SNAPSHOT")
    compileOnly("org.tukaani:xz:1.0")
    compileOnly("com.alibaba:fastjson:1.2.73")
    compileOnly("net.java.dev.jna:jna:5.8.0")
    compileOnly("net.java.dev.jna:jna-platform:5.8.0")
    compileOnly("se.michaelthelin.spotify:spotify-web-api-java:6.5.4")
    compileOnly("com.github.kokorin.jaffree:jaffree:2021.05.26")
    compileOnly("com.google.guava:guava:30.1.1-jre")

    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testImplementation("io.github.glytching:junit-extensions:2.4.0")
    testImplementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.4.31")
    testImplementation("org.jetbrains.kotlin:kotlin-test:1.4.31")
    testImplementation("uk.co.caprica:vlcj:4.7.1")
    testImplementation("com.github.sealedtx:java-youtube-downloader:2.5.2")
    testImplementation("commons-io:commons-io:2.8.0")
    testImplementation("ws.schild:jave-core:3.0.1")
    testImplementation("com.github.kokorin.jaffree:jaffree:2021.05.26")
    testImplementation("com.google.guava:guava:30.1.1-jre")

    api(project(":api"))
    implementation(project(":v1_16_R3"))
    implementation(project(":v1_16_R2"))
    implementation(project(":v1_16_R1"))
    implementation(project(":v1_15_R1"))
    implementation(project(":v1_14_R1"))
    implementation(project(":v1_13_R2"))
    implementation(project(":v1_13_R1"))
    implementation(project(":v1_12_R1"))
    implementation(project(":v1_11_R1"))
    implementation(project(":v1_10_R1"))
    implementation(project(":v1_9_R2"))
    implementation(project(":v1_9_R1"))
    implementation(project(":v1_8_R3"))
    implementation(project(":v1_8_R2"))
    implementation(project(":v1_8_R1"))

}