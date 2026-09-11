//go:build e2e

package e2e

import (
	"context"
	"crypto/rand"
	"encoding/hex"
	"io"
	"io/fs"
	"os"
	"path/filepath"
	"strings"
	"testing"
	"time"

	"github.com/moby/moby/api/types/container"
	"github.com/testcontainers/testcontainers-go"
	"github.com/testcontainers/testcontainers-go/network"
	"github.com/testcontainers/testcontainers-go/wait"
)

// The core and agent images come from the environment, so a run can test
// images built from the working tree; the defaults are the published ones.
const (
	EnvCoreImage      = "FEEDCTL_E2E_CORE_IMAGE"
	EnvAgentImage     = "FEEDCTL_E2E_AGENT_IMAGE"
	defaultCoreImage  = "damoeb/feedless:core-latest"
	defaultAgentImage = "damoeb/feedless:agent-latest"

	// Same as docker-compose.yml.
	postgisImage = "postgis/postgis:17-3.5-alpine"
	fixtureImage = "nginx:alpine"

	// Templated into /cli/install.sh; unlike CoreURL, not the published port.
	coreAPIGatewayURL = "http://localhost:8080"

	coreStartupTimeout  = 5 * time.Minute
	agentConnectTimeout = 2 * time.Minute
	logTailLines        = 400
)

// Stack runs on a private Docker network; only the core is published, on a random port.
type Stack struct {
	// A loopback URL, which feedctl accepts over plain http.
	CoreURL string
	// The core's APP_ROOT_EMAIL / APP_ROOT_SECRET_KEY, also used by the agent.
	RootEmail     string
	RootSecretKey string
}

func SkipWithoutDocker(t *testing.T) {
	t.Helper()

	testcontainers.SkipIfProviderIsNotHealthy(t)
}

// StartStack waits until the agent has registered; if the test failed, it dumps each container's log tail.
func StartStack(ctx context.Context, t *testing.T) *Stack {
	t.Helper()

	rootEmail := "e2e-root@feedless.test"
	rootSecretKey := randomHex(t)
	dbPassword := randomHex(t)
	jwtSecret := randomHex(t)
	actuatorPassword := randomHex(t)

	for _, secret := range []string{rootSecretKey, dbPassword, jwtSecret, actuatorPassword} {
		RegisterSecret(secret)
	}

	nw, err := network.New(ctx)
	if err != nil {
		t.Fatalf("creating the stack network: %v", err)
	}

	t.Cleanup(func() {
		if err := nw.Remove(context.Background()); err != nil {
			t.Logf("removing the stack network: %v", err)
		}
	})

	onNetwork := func(alias string) (networks []string, aliases map[string][]string) {
		return []string{nw.Name}, map[string][]string{nw.Name: {alias}}
	}

	networks, aliases := onNetwork("postgis")
	startContainer(ctx, t, "postgis", testcontainers.ContainerRequest{
		Image:          postgisImage,
		Networks:       networks,
		NetworkAliases: aliases,
		Env: map[string]string{
			"POSTGRES_DB":       "feedless",
			"POSTGRES_USER":     "feedless",
			"POSTGRES_PASSWORD": dbPassword,
		},
		// The image's init script restarts the server once, so the first
		// "ready" line is not the one to wait for.
		WaitingFor: wait.ForLog("database system is ready to accept connections").
			WithOccurrence(2).WithStartupTimeout(3 * time.Minute),
	})

	// Resolved by the agent's Chromium inside the network, never by the host.
	networks, aliases = onNetwork("fixture")
	startContainer(ctx, t, "fixture", testcontainers.ContainerRequest{
		Image:          fixtureImage,
		Networks:       networks,
		NetworkAliases: aliases,
		ExposedPorts:   []string{"80/tcp"},
		Files:          fixtureSiteFiles(t),
		WaitingFor:     wait.ForHTTP("/items.html").WithPort("80/tcp"),
	})

	networks, aliases = onNetwork("core")
	core := startContainer(ctx, t, "core", testcontainers.ContainerRequest{
		Image:          imageFromEnv(EnvCoreImage, defaultCoreImage),
		Networks:       networks,
		NetworkAliases: aliases,
		ExposedPorts:   []string{"8080/tcp"},
		Env: map[string]string{
			"APP_DATABASE_URL":  "jdbc:postgresql://postgis:5432/feedless",
			"POSTGRES_USER":     "feedless",
			"POSTGRES_PASSWORD": dbPassword,
			// selfHosted alone doesn't start: repositoryLayer, analytics and order are needed too.
			// An empty plausible URL keeps analytics from sending anything.
			"APP_ACTIVE_PROFILES":     "database,selfHosted,metrics,repositoryLayer,analytics,order",
			"APP_PLAUSIBLE_URL":       "",
			"APP_PLAUSIBLE_API_KEY":   "",
			"APP_AUTHENTICATION":      "authRoot",
			"APP_ROOT_EMAIL":          rootEmail,
			"APP_ROOT_SECRET_KEY":     rootSecretKey,
			"APP_JWT_SECRET":          jwtSecret,
			"APP_ACTUATOR_PASSWORD":   actuatorPassword,
			"APP_API_GATEWAY_URL":     coreAPIGatewayURL,
			"APP_HOST_URL":            "http://localhost:4200",
			"APP_WHITELISTED_HOSTS":   "core",
			"APP_MAIL_SENDER":         "noreply@feedless.test",
			"APP_DEFAULT_DATE_FORMAT": "dd-MM-yyyy",
			"APP_DEFAULT_TIME_FORMAT": "HH:mm",
			"APP_TIMEZONE":            "UTC",
			"APP_LOG_LEVEL":           "info",
			// Without an OAuth2 client registration SecurityConfig can't start; deployments always have one. sso stays off.
			"SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GITHUB_CLIENTID":     "feedctl-e2e",
			"SPRING_SECURITY_OAUTH2_CLIENT_REGISTRATION_GITHUB_CLIENTSECRET": "feedctl-e2e",
			// StripeUseCase (plan & apiLayer) resolves these placeholders at
			// startup; it only calls Stripe when a payment is made.
			"STRIPE_API_KEY":        "sk_test_feedctl_e2e",
			"STRIPE_WEBHOOK_SECRET": "whsec_feedctl_e2e",
		},
		WaitingFor: wait.ForHTTP("/actuator/health").WithPort("8080/tcp").WithStartupTimeout(coreStartupTimeout),
	})

	networks, aliases = onNetwork("agent")
	startContainer(ctx, t, "agent", testcontainers.ContainerRequest{
		Image:          imageFromEnv(EnvAgentImage, defaultAgentImage),
		Networks:       networks,
		NetworkAliases: aliases,
		Env: map[string]string{
			"APP_EMAIL":                    rootEmail,
			"APP_SECRET_KEY":               rootSecretKey,
			"APP_HOST":                     "core:8080",
			"APP_SECURE":                   "false",
			"APP_PRERENDER_TIMEOUT_MILLIS": "40000",
		},
		// Sandboxed Chromium needs SYS_ADMIN, as in validate-agent-container.sh.
		HostConfigModifier: func(hc *container.HostConfig) {
			hc.CapAdd = append(hc.CapAdd, "SYS_ADMIN")
		},
	})

	// A harvest queued before the agent registered would fail as "No agents
	// available", so the stack is only ready once the core has seen it.
	err = wait.ForLog("Adding Agent").WithStartupTimeout(agentConnectTimeout).WaitUntilReady(ctx, core)
	if err != nil {
		t.Fatalf("waiting for the agent to register with the core: %v", err)
	}

	coreURL, err := core.PortEndpoint(ctx, "8080/tcp", "http")
	if err != nil {
		t.Fatalf("resolving the core's URL: %v", err)
	}

	return &Stack{CoreURL: coreURL, RootEmail: rootEmail, RootSecretKey: rootSecretKey}
}

func startContainer(ctx context.Context, t *testing.T, name string, req testcontainers.ContainerRequest) testcontainers.Container {
	t.Helper()

	ctr, err := testcontainers.GenericContainer(ctx, testcontainers.GenericContainerRequest{
		ContainerRequest: req,
		Started:          true,
	})

	// A container that started but failed its wait strategy is returned
	// together with the error: its logs are what explains the failure.
	if ctr != nil {
		t.Cleanup(func() {
			if t.Failed() {
				dumpLogs(t, name, ctr)
			}

			if err := testcontainers.TerminateContainer(ctr); err != nil {
				t.Logf("removing the %s container: %v", name, err)
			}
		})
	}

	if err != nil {
		t.Fatalf("starting the %s container (%s): %v", name, req.Image, err)
	}

	return ctr
}

func dumpLogs(t *testing.T, name string, ctr testcontainers.Container) {
	t.Helper()

	ctx, cancel := context.WithTimeout(context.Background(), 30*time.Second)
	defer cancel()

	logs, err := ctr.Logs(ctx)
	if err != nil {
		t.Logf("reading the %s container's logs: %v", name, err)
		return
	}
	defer func() { _ = logs.Close() }()

	data, err := io.ReadAll(logs)
	if err != nil {
		t.Logf("reading the %s container's logs: %v", name, err)
	}

	lines := strings.Split(strings.TrimRight(string(data), "\n"), "\n")
	if len(lines) > logTailLines {
		lines = lines[len(lines)-logTailLines:]
	}

	t.Logf("===== %s container logs (last %d lines) =====\n%s\n===== end of %s container logs =====",
		name, len(lines), Redact(strings.Join(lines, "\n")), name)
}

func fixtureSiteFiles(t *testing.T) []testcontainers.ContainerFile {
	t.Helper()

	root := fixturePath(t, "site")

	var files []testcontainers.ContainerFile

	err := filepath.WalkDir(root, func(path string, d fs.DirEntry, err error) error {
		if err != nil || d.IsDir() {
			return err
		}

		rel, err := filepath.Rel(root, path)
		if err != nil {
			return err
		}

		files = append(files, testcontainers.ContainerFile{
			HostFilePath:      path,
			ContainerFilePath: "/usr/share/nginx/html/" + filepath.ToSlash(rel),
			FileMode:          0o644,
		})

		return nil
	})
	if err != nil {
		t.Fatalf("listing the fixture site: %v", err)
	}

	return files
}

func imageFromEnv(name, fallback string) string {
	if image := os.Getenv(name); image != "" {
		return image
	}

	return fallback
}

// Long enough for the core's jwtSecret and rootSecretKey minimums.
func randomHex(t *testing.T) string {
	t.Helper()

	b := make([]byte, 32)
	if _, err := rand.Read(b); err != nil {
		t.Fatalf("generating a secret: %v", err)
	}

	return hex.EncodeToString(b)
}
