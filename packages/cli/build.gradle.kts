// feedctl is a Go module; Gradle shells out to `go` rather than modeling the
// build with a Java/Kotlin plugin (mirrors packages/agent/build.gradle.kts,
// which shells out to yarn).
val openapiSpec = "../http-api/src/main/resources/openapi/openapi.yaml"

val goVetTask = tasks.register<Exec>("goVet") {
  commandLine("go", "vet", "./...")

  inputs.dir("cmd")
  inputs.dir("internal")
  inputs.files("go.mod", "go.sum")
  outputs.upToDateWhen { true }
}

val golangciLintTask = tasks.register<Exec>("golangciLint") {
  commandLine("go", "tool", "golangci-lint", "run", "./...")

  inputs.dir("cmd")
  inputs.dir("internal")
  inputs.files("go.mod", "go.sum", ".golangci.yml")
  outputs.upToDateWhen { true }
}

// A spec change that nobody regenerated the client for must fail the gate,
// not surface as a silent drift bug later. `go generate` rewrites
// internal/api/client.gen.go from openapiSpec; if that leaves a diff, the
// checked-in client is stale.
val generateDriftCheckTask = tasks.register<Exec>("generateDriftCheck") {
  commandLine("sh", "-c", "go generate ./... && git diff --exit-code -- internal/api")

  inputs.dir("internal/api")
  inputs.file(openapiSpec)
  outputs.upToDateWhen { true }
}

val lintTask = tasks.register("lint") {
  dependsOn(goVetTask, golangciLintTask, generateDriftCheckTask)
}

val testTask = tasks.register<Exec>("test") {
  commandLine("go", "test", "./...")

  inputs.dir("cmd")
  inputs.dir("internal")
  inputs.files("go.mod", "go.sum")
  outputs.upToDateWhen { true }
}

val buildTask = tasks.register<Exec>("build") {
  val feedlessVersion = (findProperty("feedlessVersion") as String?) ?: "dev"

  commandLine(
    "go", "build",
    "-ldflags", "-X main.version=$feedlessVersion",
    "-o", "build/feedctl",
    "./cmd/feedctl"
  )

  inputs.dir("cmd")
  inputs.dir("internal")
  inputs.files("go.mod", "go.sum")
  inputs.property("feedlessVersion", feedlessVersion)
  outputs.file("build/feedctl")
}

tasks.register<Delete>("clean") {
  delete("build")
}
