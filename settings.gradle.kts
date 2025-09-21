rootProject.name = "feedless"

include("packages:app-web")
include("packages:server-core")
include("packages:agent")

buildscript {
  repositories {
    gradlePluginPortal()
  }
  dependencies {
    val gradleNodePluginVersion = "5.0.0"
    classpath("com.github.node-gradle:gradle-node-plugin:$gradleNodePluginVersion")
    classpath("org.ajoberstar.grgit:grgit-gradle:5.0.0")
  }
}

// https://docs.gradle.org/current/userguide/build_cache.html
buildCache {
  local {
    directory = File(rootDir, "build-cache")
    removeUnusedEntriesAfterDays = 30
  }
}
