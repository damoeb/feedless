buildscript {
  repositories {
    gradlePluginPortal()
  }
  dependencies {
    val gradleNodePluginVersion: String by project
    classpath ("com.github.node-gradle:gradle-node-plugin:$gradleNodePluginVersion")
  }
}

subprojects {
  tasks.register("lintDockerImage", Exec::class) {
    commandLine("sh", rootProject.file("lintDockerfile.sh").getAbsolutePath(), project.file("Dockerfile").getAbsolutePath())
  }
}
