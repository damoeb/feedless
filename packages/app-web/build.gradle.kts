import com.github.gradle.node.yarn.task.YarnTask

plugins {
  id("com.github.node-gradle.node")
  id("org.ajoberstar.grgit")
}
// https://github.com/node-gradle/gradle-node-plugin/tree/master/examples/simple-node
// https://github.com/node-gradle/gradle-node-plugin/blob/master/src/test/resources/fixtures/kotlin/build.gradle.kts
node {
  val nodejsVersion: String by project
  version.set(nodejsVersion)
  download.set(true)
}

val prepareTask = tasks.register("prepare") {
  dependsOn(codegenTask, yarnInstallTask)
}

val yarnInstallTask = tasks.register<YarnTask>("yarnInstall") {
  args.set(listOf("install", "--frozen-lockfile", "--ignore-scripts"))
  inputs.files("yarn.lock")
  outputs.dir("node_modules")
}

val codegenTask = tasks.register<YarnTask>("codegen") {
  args.set(listOf("codegen"))
  dependsOn(yarnInstallTask)
  inputs.files("codegen.yml", "yarn.lock", "feedless-config-writer.ts", "src/app/feedless-config.ts", "../server-core/src/main/resources/schema/schema.graphqls")
  outputs.files("src/generated/graphql.ts", "feedless-config.json")
}

val lintTask = tasks.register<YarnTask>("lint") {
  args.set(listOf("lint"))
  dependsOn(prepareTask)
  inputs.dir("src")
  inputs.files(
    "angular.json",
    "yarn.lock",
    ".prettierignore",
    ".prettierrc.json",
    "tsconfig.json",
    "tsconfig.app.json",
    "tsconfig.spec.json",
    "tslint.json"
  )
  outputs.upToDateWhen { true }
}

val testTask = tasks.register<YarnTask>("test") {
  args.set(listOf("test:ci"))
  dependsOn(prepareTask)
  inputs.dir("src")
  inputs.dir("node_modules")
  inputs.files(
    "angular.json", ".browserslistrc", "tsconfig.json", "tsconfig.app.json", "tsconfig.spec.json",
    "tslint.json"
  )
  outputs.upToDateWhen { true }
}

val buildTask = tasks.register<YarnTask>("build") {
  args.set(listOf("build:prod"))
  dependsOn(prepareTask, lintTask, testTask)
  inputs.dir(project.fileTree("src").exclude("**/*.spec.ts"))
  inputs.dir("node_modules")
  inputs.files("yarn.lock", "tsconfig.json", "tsconfig.build.json")
  outputs.dir("www")
}

tasks.register("bundle", Exec::class) {
  dependsOn(buildTask)
  val gitHash = grgit.head().id
  val baseTag = findProperty("dockerImageTag")
  commandLine(
    "docker", "build",
    "-t", "$baseTag:app-latest",
    "-t", "$baseTag:app-$gitHash",
    "."
  )
}

tasks.register("clean", Exec::class) {
  commandLine(
    "rm", "-rf",
    "www", "build",
  )
}

tasks.register<YarnTask>("start") {
  args.set(listOf("start:dev"))
  dependsOn(yarnInstallTask, codegenTask)
}
