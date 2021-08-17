rootProject.name = "rich-multiproject"

include("packages:app")
include("packages:server-rich-rss")
//include("packages:server-rich-graph")
include("packages:server-graphql")

buildscript {
  repositories {
    gradlePluginPortal()
  }
  dependencies {
    val gradleNodePluginVersion: String = "3.1.0"
    classpath ("com.github.node-gradle:gradle-node-plugin:$gradleNodePluginVersion")
  }
}
