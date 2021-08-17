buildscript {
  repositories {
    gradlePluginPortal()
  }
  dependencies {
    val gradleNodePluginVersion: String by project
    classpath ("com.github.node-gradle:gradle-node-plugin:$gradleNodePluginVersion")
  }
}
