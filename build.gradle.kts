plugins {
    id("java")
}

group = "me.velikoss"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("uk.co.electronstudio.jaylib:jaylib:4.5.0-0")
    implementation("org.projectlombok:lombok:1.18.28")
    annotationProcessor("org.projectlombok:lombok:1.18.28")
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("org.apache.commons:commons-collections4:4.4")
}