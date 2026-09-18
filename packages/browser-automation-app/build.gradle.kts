import com.github.gradle.node.yarn.task.YarnTask

plugins {
  alias(libs.plugins.node)
  id("org.ajoberstar.grgit")
}


// https://github.com/node-gradle/gradle-node-plugin/tree/master/examples/simple-node
// https://github.com/node-gradle/gradle-node-plugin/blob/master/src/test/resources/fixtures/kotlin/build.gradle.kts
node {
  val nvmrcFile = rootProject.file(".nvmrc")
  val nodeVersion = if (nvmrcFile.exists()) {
    nvmrcFile.readText().trim()
  } else {
    throw IllegalStateException(".nvmrc file not found")
  }
  version.set(nodeVersion)
  download.set(true)
}

val yarnCleanTask = tasks.register<YarnTask>("yarnClean") {
  dependsOn(yarnInstallTask)
  args.set(listOf("clean"))
}

val gradleCleanTask = tasks.register<Delete>("clean") {
  dependsOn(yarnCleanTask)
  delete(project.buildDir)
}

val yarnInstallTask = tasks.register<YarnTask>("yarnInstall") {
  args.set(listOf("install", "--frozen-lockfile", "--ignore-scripts"))

  inputs.file(rootProject.file(".nvmrc"))
  inputs.files("yarn.lock")
  outputs.dir("node_modules")
}

// Gradle reruns a task without outputs every time; a stamp lets check-only tasks be up to date.
fun Task.stampOutput() {
  val stamp = layout.buildDirectory.file("stamps/$name")
  outputs.file(stamp)
  doLast { stamp.get().asFile.run { parentFile.mkdirs(); writeText("") } }
}

val lintTask = tasks.register<YarnTask>("lint") {
  dependsOn(codegenTask)
  args.set(listOf("lint"))

  inputs.file(rootProject.file(".nvmrc"))
  inputs.dir("src")
  inputs.files("yarn.lock")
  stampOutput()
}

val codegenTask = tasks.register<YarnTask>("codegen") {
  args.set(listOf("codegen"))
  dependsOn(yarnInstallTask)

  inputs.file(rootProject.file(".nvmrc"))
  inputs.files(fileTree("src") { include("**/*.graphql") })
  inputs.files("codegen.yml", "yarn.lock", "../graphql-api/src/main/resources/schema/schema.graphqls")
  outputs.file("src/generated/graphql.ts")
}

val prepareTask = tasks.register("prepare") {
  dependsOn(yarnInstallTask, codegenTask)
}

val testTask = tasks.register<YarnTask>("test") {
  args.set(listOf("test"))
  dependsOn(prepareTask)
  inputs.dir("src")
  // jest's config lives in package.json
  inputs.files("package.json", "yarn.lock", "tsconfig.json")
  stampOutput()
}

val buildTask = tasks.register<YarnTask>("build") {
  args.set(listOf("build"))
  dependsOn(prepareTask, lintTask, testTask)

  inputs.file(rootProject.file(".nvmrc"))
  inputs.dir(project.fileTree("src").exclude("**/*.spec.ts"))
  inputs.files("yarn.lock", "tsconfig.json", "tsconfig.build.json")
  outputs.dir("dist")
}

val systemTest = tasks.register("systemTest", Exec::class) {
  commandLine(
    "./test/system/validate-agent-container.sh"
  )
}

tasks.register("bundle", Exec::class) {
  dependsOn(buildTask)
  finalizedBy(systemTest)
  val semver = findProperty("feedlessVersion") as String
  val baseTag = findProperty("dockerImageTag")

  val gitHash = grgit.head().id.take(7)

  inputs.property("baseTag", findProperty("dockerImageTag"))
  inputs.property("gitHash", gitHash)
  inputs.property("semver", semver)

  commandLine(
    "docker", "build",
    "--build-arg", "APP_VERSION=$semver",
    "--build-arg", "APP_GIT_COMMIT=$gitHash",
    "-t", "$baseTag:browser-automation-app-latest",
    "-t", "$baseTag:browser-automation-app-$gitHash",
    "."
  )
}


tasks.register<YarnTask>("start") {
  args.set(listOf("start:dev"))
  dependsOn(prepareTask)
}
