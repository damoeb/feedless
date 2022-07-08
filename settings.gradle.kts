rootProject.name = "rich-rss"

include("packages:app")
include("packages:server-core")
//include("packages:server-graph")
include("packages:server-mgmt")
include("packages:server-puppeteer")

buildscript {
  repositories {
    gradlePluginPortal()
  }
  dependencies {
    val gradleNodePluginVersion = "3.1.0"
    classpath ("com.github.node-gradle:gradle-node-plugin:$gradleNodePluginVersion")
  }
}
