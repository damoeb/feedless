package config

import (
	"errors"
	"net"
	"os/exec"

	"github.com/godbus/dbus/v5"
	"github.com/zalando/go-keyring"
)

// isKeyringUnavailable is true only when no OS keyring exists, the one case the plain-text fallback is allowed for;
// a locked keychain or denied access is a real failure. On Linux "none" means no Secret Service or no session bus.
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
