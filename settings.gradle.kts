rootProject.name = "rich-rss-root"

include("packages:app")
include("packages:client-cli")
include("packages:server-rss-kotlin")
//include("packages:server-rich-graph")
include("packages:server-rss-node")

buildscript {
  repositories {
    gradlePluginPortal()
  }
  dependencies {
    val gradleNodePluginVersion: String = "3.1.0"
    classpath ("com.github.node-gradle:gradle-node-plugin:$gradleNodePluginVersion")
  }
}
