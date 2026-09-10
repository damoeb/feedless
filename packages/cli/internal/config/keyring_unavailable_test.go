package config

import (
	"errors"
	"fmt"
	"net"
	"os/exec"
	"testing"

	"github.com/godbus/dbus/v5"
	"github.com/zalando/go-keyring"
)

func TestIsKeyringUnavailable_ErrUnsupportedPlatform(t *testing.T) {
	if !isKeyringUnavailable(keyring.ErrUnsupportedPlatform) {
		t.Error("isKeyringUnavailable(ErrUnsupportedPlatform) = false, want true")
	}
}

func TestIsKeyringUnavailable_WrappedErrUnsupportedPlatform(t *testing.T) {
	err := fmt.Errorf("opening the keyring: %w", keyring.ErrUnsupportedPlatform)
	if !isKeyringUnavailable(err) {
		t.Error("isKeyringUnavailable(wrapped ErrUnsupportedPlatform) = false, want true")
	}
}

func TestIsKeyringUnavailable_DBusServiceUnknown_NoSecretService(t *testing.T) {
	err := dbus.NewError("org.freedesktop.DBus.Error.ServiceUnknown", nil)
	if !isKeyringUnavailable(err) {
		t.Error("isKeyringUnavailable(ServiceUnknown) = false, want true (no Secret Service registered)")
	}
}

func TestIsKeyringUnavailable_DBusNameHasNoOwner(t *testing.T) {
	err := dbus.NewError("org.freedesktop.DBus.Error.NameHasNoOwner", nil)
	if !isKeyringUnavailable(err) {
		t.Error("isKeyringUnavailable(NameHasNoOwner) = false, want true")
	}
}

func TestIsKeyringUnavailable_DBusNoReply(t *testing.T) {
	err := dbus.NewError("org.freedesktop.DBus.Error.NoReply", nil)
	if !isKeyringUnavailable(err) {
		t.Error("isKeyringUnavailable(NoReply) = false, want true")
	}
}

func TestIsKeyringUnavailable_DBusOtherError_IsARealFailure(t *testing.T) {
	// e.g. the user cancelled an unlock prompt, or a locked keychain.
	err := dbus.NewError("org.freedesktop.Secret.Error.IsLocked", nil)
	if isKeyringUnavailable(err) {
		t.Error("isKeyringUnavailable(IsLocked) = true, want false: a locked keyring is a real failure, not \"no keyring\"")
	}
}

func TestIsKeyringUnavailable_DBusAccessDenied_IsARealFailure(t *testing.T) {
	err := dbus.NewError("org.freedesktop.DBus.Error.AccessDenied", nil)
	if isKeyringUnavailable(err) {
		t.Error("isKeyringUnavailable(AccessDenied) = true, want false: denied access is a real failure, not \"no keyring\"")
	}
}

func TestIsKeyringUnavailable_ExecError_NoDbusLaunchBinary(t *testing.T) {
	err := &exec.Error{Name: "dbus-launch", Err: exec.ErrNotFound}
	if !isKeyringUnavailable(err) {
		t.Error("isKeyringUnavailable(*exec.Error) = false, want true: no way to even reach a session bus")
	}
}

func TestIsKeyringUnavailable_NetOpError_SocketUnreachable(t *testing.T) {
	err := &net.OpError{Op: "dial", Net: "unix", Err: errors.New("no such file or directory")}
	if !isKeyringUnavailable(err) {
		t.Error("isKeyringUnavailable(*net.OpError) = false, want true: nothing listening on the session bus socket")
	}
}

func TestIsKeyringUnavailable_ErrSetDataTooBig_IsARealFailure(t *testing.T) {
	if isKeyringUnavailable(keyring.ErrSetDataTooBig) {
		t.Error("isKeyringUnavailable(ErrSetDataTooBig) = true, want false: too-big data is a real failure, not \"no keyring\"")
	}
}

func TestIsKeyringUnavailable_UnrecognizedError_IsARealFailure(t *testing.T) {
	if isKeyringUnavailable(errors.New("some other keyring failure")) {
		t.Error("isKeyringUnavailable(unrecognized error) = true, want false")
	}
}
