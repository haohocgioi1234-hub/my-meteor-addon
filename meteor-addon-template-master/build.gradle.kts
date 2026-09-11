buildscript {
    repositories {
        maven("https://maven.fabricmc.net/")
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath("net.fabricmc:fabric-loom:1.6-SNAPSHOT")
    }
}

apply(plugin = "fabric-loom")
apply(plugin = "java")

val archivesBaseName = providers.gradleProperty("archives_base_name").getOrElse("base-detector-addon")
val archiveVersion = providers.gradleProperty("archive_version").getOrElse("1.0.0")
val mavenGroup = providers.gradleProperty("maven_group").getOrElse("com.example.addon")

val minecraftVersion = providers.gradleProperty("minecraft_version").getOrElse("1.21.1")
val yarnMappings = providers.gradleProperty("yarn_mappings").getOrElse("1.21.1+build.1")
val loaderVersion = providers.gradleProperty("loader_version").getOrElse("0.16.2")
val meteorVersion = providers.gradleProperty("meteor_version").getOrElse("0.5.8")

version = archiveVersion
group = mavenGroup

repositories {
    mavenCentral()
    maven("https://maven.meteordev.org/releases")
    maven("https://maven.meteordev.org/snapshots")
    maven("https://maven.fabricmc.net/")
}

dependencies {
    "minecraft"("com.mojang:minecraft:$minecraftVersion")
    "mappings"("net.fabricmc:yarn:$yarnMappings:v2")
    "modImplementation"("net.fabricmc:fabric-loader:$loaderVersion")
    "modImplementation"("meteordevelopment:meteor-client:$meteorVersion")
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(21)
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}
