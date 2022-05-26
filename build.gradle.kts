buildscript {
  repositories {
    gradlePluginPortal()
  }
  dependencies {
    classpath ("com.github.node-gradle:gradle-node-plugin:${findProperty("gradleNodePluginVersion")}")
  }
}

subprojects {
  tasks.register("lintDockerImage", Exec::class) {
    commandLine("sh", rootProject.file("lintDockerfile.sh").getAbsolutePath(), project.file("Dockerfile").getAbsolutePath())
  }
}
