rootProject.name = "rich-rss"

include("packages:app")
include("packages:server-core")
//include("packages:server-graph")
include("packages:server-mgmt")

buildscript {
  repositories {
    gradlePluginPortal()
  }
  dependencies {
    val gradleNodePluginVersion = "3.1.0"
    classpath ("com.github.node-gradle:gradle-node-plugin:$gradleNodePluginVersion")
  }
}

plugins {
  // See https://jmfayard.github.io/refreshVersions
  id("de.fayard.refreshVersions") version "0.23.0"
}
