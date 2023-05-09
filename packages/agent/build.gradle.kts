import com.github.gradle.node.yarn.task.YarnTask

plugins {
  id ("com.github.node-gradle.node")
}


// https://github.com/node-gradle/gradle-node-plugin/tree/master/examples/simple-node
// https://github.com/node-gradle/gradle-node-plugin/blob/master/src/test/resources/fixtures/kotlin/build.gradle.kts
node {
  val nodejsVersion = findProperty("nodejsVersion") as String
  version.set(nodejsVersion)
  npmVersion.set("")
  yarnVersion.set("")
  download.set(false)
}

val libBuild = tasks.findByPath(":packages:client-lib:build")

val copyLibDist = tasks.register<Copy>("copyLibDist") {
  dependsOn(libBuild)
  from(libBuild!!.outputs.files)
  into("${project.buildDir}/client-lib")
  doLast {
    println("Copied to ${project.buildDir}/client-lib")
    println(libBuild.outputs.files.files.toList().joinToString(" "))
  }
}

val yarnCleanTask = tasks.register<YarnTask>("yarnClean") {
  dependsOn(yarnInstallTask, libBuild)
  args.set(listOf("clean"))
}

val gradleCleanTask = tasks.register<Delete>("clean") {
  dependsOn(yarnCleanTask)
  delete(project.buildDir)
}

val yarnInstallTask = tasks.register<YarnTask>("yarnInstall") {
  dependsOn(libBuild)
  args.set(listOf("install", "--frozen-lockfile", "--ignore-scripts"))
  inputs.files("yarn.lock")
  outputs.dir("node_modules")
}

val lintTask = tasks.register<YarnTask>("lint") {
  args.set(listOf("lint"))
  dependsOn(yarnInstallTask)
  inputs.dir("src")
  inputs.files("yarn.lock")
  outputs.upToDateWhen { true }
}

val testTask = tasks.register<YarnTask>("test") {
  args.set(listOf("test"))
  dependsOn(yarnInstallTask)
  inputs.dir("src")
  inputs.files("yarn.lock")
  outputs.upToDateWhen { true }
}

val buildTask = tasks.register<YarnTask>("build") {
  args.set(listOf("build"))
  dependsOn(yarnInstallTask, lintTask, testTask)
  inputs.dir(project.fileTree("src").exclude("**/*.spec.ts"))
  inputs.dir("node_modules")
  inputs.files("yarn.lock", "tsconfig.json", "tsconfig.build.json")
  outputs.dir("dist")
}

tasks.register("buildDockerImage", Exec::class) {
  dependsOn(buildTask, copyLibDist)
  val major = findProperty("majorVersion") as String
  val agentVersion = findProperty("agentVersion") as String
  val majorMinorPatch = "${major}.${agentVersion}"
  val majorMinor = "${major}.${agentVersion.split(".")[0]}"

  val imageName = "${findProperty("dockerImageTag")}:agent"

  commandLine("docker", "build",
    "-t", "${imageName}-${majorMinorPatch}",
    "-t", "${imageName}-${majorMinor}",
    "-t", "${imageName}-${major}",
    "-t", imageName,
    ".")
}


tasks.register<YarnTask>("start") {
  args.set(listOf("start:dev"))
  dependsOn(yarnInstallTask)
}
