package config

import (
	"errors"
	"net"
	"os/exec"

	"github.com/godbus/dbus/v5"
	"github.com/zalando/go-keyring"
)

// isKeyringUnavailable reports whether err means "there is no OS keyring
// backend to talk to on this machine" — the only situation StoreToken may
// fall back to a plain-text hosts.yml token for. Every other error (a
// locked keychain, the user denying access, ErrSetDataTooBig, ...) is a
// real failure and must not be swallowed into a silent downgrade.
//
// go-keyring (v0.2.8) has exactly one typed sentinel for this,
// ErrUnsupportedPlatform, returned by its build-time fallback provider on
// any OS without a real backend (js/wasm, cgo-less freebsd/dragonfly, ...)
// — see keyring_fallback.go in the module. On Linux (and the BSDs that do
// have a real provider) there is no such sentinel: the secret-service
// provider (keyring_unix.go) talks to D-Bus via github.com/godbus/dbus/v5
// and github.com/zalando/go-keyring/secret_service, and propagates
// whatever error that produces unchanged. Two shapes of that error mean
// "no keyring", both identifiable by type rather than by string-matching
// a message:
//
//   - *dbus.Error with a small set of well-known Name values: the D-Bus
//     session exists but nothing owns org.freedesktop.secrets, i.e. no
//     Secret Service is running (the common headless/minimal-desktop
//     case) — ServiceUnknown/NameHasNoOwner/NoReply/Spawn.ExecFailed.
//   - *exec.Error or *net.OpError: connecting to a session bus at all
//     failed (no DBUS_SESSION_BUS_ADDRESS, no dbus-launch binary to
//     autostart one, or nothing listening on the socket) — the common CI
//     case. godbus returns these as plain, unwrapped stdlib error types
//     (see (godbus/dbus/v5).SessionBus / getSessionBusPlatformAddress),
//     not a dbus.Error, since no D-Bus exchange ever took place.
//
// A *dbus.Error with any other Name (e.g. IsLocked, AccessDenied, a
// cancelled unlock prompt) is treated as a real failure, not "unavailable".
func isKeyringUnavailable(err error) bool {
	if errors.Is(err, keyring.ErrUnsupportedPlatform) {
		return true
	}

	var dbusErr *dbus.Error
	if errors.As(err, &dbusErr) {
		switch dbusErr.Name {
		case "org.freedesktop.DBus.Error.ServiceUnknown",
			"org.freedesktop.DBus.Error.NameHasNoOwner",
			"org.freedesktop.DBus.Error.NoReply",
			"org.freedesktop.DBus.Error.Spawn.ExecFailed",
			"org.freedesktop.DBus.Error.Timeout":
			return true
		default:
			return false
		}
	}

	var execErr *exec.Error
	if errors.As(err, &execErr) {
		return true
	}

	var opErr *net.OpError

	return errors.As(err, &opErr)
}
