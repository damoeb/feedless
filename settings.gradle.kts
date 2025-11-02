rootProject.name = "feedless"

include("packages:app-web")
include("packages:server-core")
include("packages:agent")
include("packages:nominatim-proxy")
include("packages:mail-adapter")
include("packages:domain")
include("packages:graphql-api")
include("packages:github-connector")

pluginManagement {
  plugins {
    id("com.github.ben-manes.versions") version "0.53.0"
  }
}


buildscript {
  repositories {
    gradlePluginPortal()
  }
  dependencies {
    classpath("org.ajoberstar.grgit:grgit-gradle:5.0.0")
  }
}

// https://docs.gradle.org/current/userguide/build_cache.html
buildCache {
  local {
    directory = File(rootDir, "build-cache")
  }
}
