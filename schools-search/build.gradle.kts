import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.4.10"
    application
}

group = "me.johnnychen"
version = ""

repositories {
    mavenCentral()

    // kotlin-cli
    maven ("https://kotlin.bintray.com/kotlinx")
}

dependencies {
    testImplementation(kotlin("test-junit"))
    implementation("org.jetbrains.kotlinx", "kotlinx-cli", "0.3")
    implementation("org.slf4j", "slf4j-api", "1.7.30")
    implementation("org.slf4j", "slf4j-simple", "1.7.30")
    implementation("com.github.doyaaaaaken:kotlin-csv-jvm:0.13.0")

}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "MainKt"
    }
//    configurations["compileClasspath"].forEach {file ->
//        from(zipTree(file.absolutePath))
//    }
}

application {
    mainClassName = "MainKt"
}

distributions {
    main {
        distributionBaseName.set("schools-bin")
        contents {
            from("README.md")
            from("src/main/resources/school_data.csv")
        }
    }
}