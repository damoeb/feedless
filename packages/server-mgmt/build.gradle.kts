import com.github.gradle.node.yarn.task.YarnTask

plugins {
  id ("com.github.node-gradle.node")
}


// https://github.com/node-gradle/gradle-node-plugin/tree/master/examples/simple-node
// https://github.com/node-gradle/gradle-node-plugin/blob/master/src/test/resources/fixtures/kotlin/build.gradle.kts
node {
  val nodejsVersion: String by project
  version.set(nodejsVersion)
  npmVersion.set("")
  yarnVersion.set("")
  download.set(false)
}

val yarnInstallTask = tasks.register<YarnTask>("yarnInstall") {
  args.set(listOf("install", "--frozen-lockfile", "--ignore-scripts"))
  inputs.files("yarn.lock")
  outputs.dir("node_modules")
}

val codegenTask = tasks.register<YarnTask>("codegen") {
  args.set(listOf("codegen"))
  dependsOn(yarnInstallTask)
  inputs.files("../server-commons/mq-commons.gql")
  outputs.files("src/generated/mq.ts")
}

val lintTask = tasks.register<YarnTask>("lint") {
  args.set(listOf("lint"))
  dependsOn(yarnInstallTask, codegenTask)
//  dependsOn(yarnInstallTask, codegenTask, "lintDockerImage")
  inputs.dir("src")
  inputs.files("yarn.lock")
  outputs.upToDateWhen { true }
}

val testTask = tasks.register<YarnTask>("test") {
  args.set(listOf("test"))
  dependsOn(yarnInstallTask, codegenTask)
  inputs.dir("src")
  inputs.files("yarn.lock")
  outputs.upToDateWhen { true }
}

val prismaTask = tasks.register<YarnTask>("prisma") {
  args.set(listOf("prisma", "generate"))
  dependsOn(yarnInstallTask)
  inputs.dir(project.fileTree("prisma"))
  outputs.dir("node_modules/@generated")
}
val buildTask = tasks.register<YarnTask>("build") {
  args.set(listOf("build"))
  dependsOn(yarnInstallTask, lintTask, testTask, prismaTask)
  inputs.dir(project.fileTree("src").exclude("**/*.spec.ts"))
  inputs.dir("node_modules")
  inputs.files("yarn.lock", "tsconfig.json", "tsconfig.build.json")
  outputs.dir("dist")
}

val appBuild = tasks.findByPath(":packages:app:build")

val copyAppDist = tasks.register<Copy>("copyAppDist") {
  dependsOn(appBuild)
  from(appBuild!!.outputs.files)
  into("${project.buildDir}/app")
  println("Copied to ${project.buildDir}/app")
}

tasks.register("buildDockerImage", Exec::class) {
  dependsOn(buildTask, copyAppDist)
  commandLine("docker", "build", "-t", "damoeb/rich-rss:mgmt", ".")
}

tasks.register<YarnTask>("start") {
  args.set(listOf("start:dev"))
  dependsOn(yarnInstallTask, prismaTask, codegenTask)
}
