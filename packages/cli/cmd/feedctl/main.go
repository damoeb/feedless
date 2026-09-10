// Command feedctl is a gh-style CLI over the Feedless HTTP API (/api/v1).
package main

import (
	"os"

	"github.com/damoeb/feedless/packages/cli/internal/cmd"
)

// version is set at build time via -ldflags "-X main.version=<version>"
// (see the Gradle `build` task in packages/cli/build.gradle.kts). It stays
// "dev" for a plain `go build`/`go run`.
var version = "dev"

func main() {
	if err := cmd.NewRootCmd(version).Execute(); err != nil {
		os.Exit(1)
	}
}
