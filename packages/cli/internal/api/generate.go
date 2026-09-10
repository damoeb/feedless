// Package api holds the generated Feedless HTTP API client.
//
// client.gen.go is produced by oapi-codegen from
// packages/http-api/src/main/resources/openapi/openapi.yaml, the single
// source of truth for the /api/v1 contract. Never hand-edit client.gen.go —
// regenerate it with `go generate ./...` (or `./gradlew :packages:cli:lint`,
// which regenerates and fails the build if the checked-in file drifted from
// the spec).
package api

//go:generate go tool oapi-codegen -config cfg.yaml ../../../http-api/src/main/resources/openapi/openapi.yaml
