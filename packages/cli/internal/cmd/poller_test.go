package cmd

import (
	"bytes"
	"context"
	"errors"
	"os"
	"strings"
	"testing"
	"time"

	"github.com/damoeb/feedless/packages/cli/internal/api"
)

// fakeClock lets a test control p.now() deterministically without a real
// clock.
type fakeClock struct{ t time.Time }

func (c *fakeClock) now() time.Time { return c.t }

// noSleep is a pollSleepFunc that returns instantly instead of really
// sleeping — every poller test uses this (or a variant), never realSleep.
func noSleep(ctx context.Context, _ time.Duration) error {
	return ctx.Err()
}

func alwaysSleepsOK(_ context.Context, _ time.Duration) error {
	return nil
}

func TestPollDelay_Sequence(t *testing.T) {
	tests := []struct {
		n    int
		want time.Duration
	}{
		{0, time.Second},
		{1, 2 * time.Second},
		{2, 4 * time.Second},
		{3, 8 * time.Second},
		{4, 10 * time.Second},
		{5, 10 * time.Second},
		{100, 10 * time.Second},
	}

	for _, tt := range tests {
		if got := pollDelay(tt.n); got != tt.want {
			t.Errorf("pollDelay(%d) = %v, want %v", tt.n, got, tt.want)
		}
	}
}

func TestHarvestPoller_Wait_PollsUntilCompleted(t *testing.T) {
	statuses := []api.HarvestStatus{api.Queued, api.Running, api.Running, api.Completed}
	call := 0

	var sleeps []time.Duration

	p := &harvestPoller{
		fetch: func(_ context.Context) (api.Harvest, error) {
			h := api.Harvest{Status: statuses[call], StartedAt: time.Now()}
			call++

			return h, nil
		},
		sleep: func(_ context.Context, d time.Duration) error {
			sleeps = append(sleeps, d)

			return nil
		},
		now:    time.Now,
		stderr: &bytes.Buffer{},
		isTTY:  false,
	}

	final, err := p.Wait(context.Background())
	if err != nil {
		t.Fatalf("Wait() error = %v", err)
	}

	if final.Status != api.Completed {
		t.Errorf("final.Status = %v, want completed", final.Status)
	}

	if call != 4 {
		t.Errorf("fetch called %d times, want 4", call)
	}

	wantSleeps := []time.Duration{time.Second, 2 * time.Second, 4 * time.Second, 8 * time.Second}
	if len(sleeps) != len(wantSleeps) {
		t.Fatalf("sleeps = %v, want %v", sleeps, wantSleeps)
	}

	for i, d := range wantSleeps {
		if sleeps[i] != d {
			t.Errorf("sleeps[%d] = %v, want %v", i, sleeps[i], d)
		}
	}
}

func TestHarvestPoller_Wait_CompletedOnFirstPoll_OnlyOneFetch(t *testing.T) {
	call := 0
	p := &harvestPoller{
		fetch: func(_ context.Context) (api.Harvest, error) {
			call++

			return api.Harvest{Status: api.Completed}, nil
		},
		sleep:  alwaysSleepsOK,
		now:    time.Now,
		stderr: &bytes.Buffer{},
	}

	if _, err := p.Wait(context.Background()); err != nil {
		t.Fatalf("Wait() error = %v", err)
	}

	if call != 1 {
		t.Errorf("fetch called %d times, want 1", call)
	}
}

func TestHarvestPoller_Wait_CancelledContext_StopsAndReturnsCancelled(t *testing.T) {
	ctx, cancel := context.WithCancel(context.Background())
	cancel()

	called := false
	p := &harvestPoller{
		fetch: func(_ context.Context) (api.Harvest, error) {
			called = true

			return api.Harvest{}, nil
		},
		sleep:  noSleep, // returns ctx.Err() immediately since ctx is already cancelled
		now:    time.Now,
		stderr: &bytes.Buffer{},
	}

	_, err := p.Wait(ctx)
	if !errors.Is(err, context.Canceled) {
		t.Fatalf("Wait() error = %v, want context.Canceled", err)
	}

	if called {
		t.Error("fetch was called, want polling to stop before ever fetching")
	}
}

func TestHarvestPoller_Wait_FetchError_Propagates(t *testing.T) {
	wantErr := errors.New("boom")
	p := &harvestPoller{
		fetch: func(_ context.Context) (api.Harvest, error) {
			return api.Harvest{}, wantErr
		},
		sleep:  alwaysSleepsOK,
		now:    time.Now,
		stderr: &bytes.Buffer{},
	}

	_, err := p.Wait(context.Background())
	if !errors.Is(err, wantErr) {
		t.Fatalf("Wait() error = %v, want %v", err, wantErr)
	}
}

func TestHarvestPoller_Wait_TTY_ReportsQueuedAndRunningStatusLines(t *testing.T) {
	clock := &fakeClock{t: time.Date(2026, 1, 1, 0, 0, 42, 0, time.UTC)}
	started := clock.t.Add(-42 * time.Second)

	statuses := []api.HarvestStatus{api.Queued, api.Running, api.Completed}
	call := 0

	var stderr bytes.Buffer

	p := &harvestPoller{
		fetch: func(_ context.Context) (api.Harvest, error) {
			h := api.Harvest{Status: statuses[call], StartedAt: started}
			call++

			return h, nil
		},
		sleep:  alwaysSleepsOK,
		now:    clock.now,
		stderr: &stderr,
		isTTY:  true,
	}

	if _, err := p.Wait(context.Background()); err != nil {
		t.Fatalf("Wait() error = %v", err)
	}

	out := stderr.String()
	if !bytes.Contains([]byte(out), []byte("queued…")) {
		t.Errorf("stderr = %q, want it to contain %q", out, "queued…")
	}
	if !bytes.Contains([]byte(out), []byte("running… 00:42")) {
		t.Errorf("stderr = %q, want it to contain %q", out, "running… 00:42")
	}
	// Wait must move past the status line once polling stops, so whatever
	// prints next starts on its own line.
	if out[len(out)-1] != '\n' {
		t.Errorf("stderr = %q, want it to end with a newline once polling stops", out)
	}
}

func TestHarvestPoller_Wait_NonTTY_NoStatusOutput(t *testing.T) {
	var stderr bytes.Buffer

	p := &harvestPoller{
		fetch: func(_ context.Context) (api.Harvest, error) {
			return api.Harvest{Status: api.Completed}, nil
		},
		sleep:  alwaysSleepsOK,
		now:    time.Now,
		stderr: &stderr,
		isTTY:  false,
	}

	if _, err := p.Wait(context.Background()); err != nil {
		t.Fatalf("Wait() error = %v", err)
	}

	if stderr.Len() != 0 {
		t.Errorf("stderr = %q, want empty (non-TTY)", stderr.String())
	}
}

func TestFormatElapsed(t *testing.T) {
	tests := []struct {
		d    time.Duration
		want string
	}{
		{0, "00:00"},
		{42 * time.Second, "00:42"},
		{90 * time.Second, "01:30"},
		{-5 * time.Second, "00:00"},
	}

	for _, tt := range tests {
		if got := formatElapsed(tt.d); got != tt.want {
			t.Errorf("formatElapsed(%v) = %q, want %q", tt.d, got, tt.want)
		}
	}
}

// --- progressIsTTY / productionPollDeps: the isTTY decision must come from
// the actual writer the status line is written to (stderr), never from
// os.Stdout — a prior version hardcoded output.IsTerminal(os.Stdout), which
// mis-detects the moment stdout and stderr disagree (2>err.log with stdout
// still a terminal writes raw \r/\x1b[K into the log; stdout redirected
// with stderr a terminal wrongly suppresses the progress line). These
// tests exercise the decision function itself and productionPollDeps'
// wiring to it, not just the harvestPoller.isTTY field a caller could set
// to anything (already covered by TestHarvestPoller_Wait_TTY_… above).

func TestProgressIsTTY_NonFileWriter_False(t *testing.T) {
	// A *bytes.Buffer — what every test's cmd.ErrOrStderr() actually is —
	// can never be a terminal, regardless of what the real process's stdout
	// or stderr happens to be.
	if progressIsTTY(&bytes.Buffer{}) {
		t.Error("progressIsTTY(&bytes.Buffer{}) = true, want false")
	}
}

func TestProgressIsTTY_RegularFile_False(t *testing.T) {
	// An *os.File satisfies the type assertion but a plain regular file
	// (as opposed to a real terminal device) still isn't a terminal —
	// proves the function actually calls output.IsTerminal rather than
	// treating every *os.File as one.
	f, err := os.CreateTemp(t.TempDir(), "not-a-tty")
	if err != nil {
		t.Fatalf("os.CreateTemp() error = %v", err)
	}
	t.Cleanup(func() { _ = f.Close() })

	if progressIsTTY(f) {
		t.Error("progressIsTTY(regular file) = true, want false")
	}
}

func TestProductionPollDeps_IsTTY_ComesFromGivenWriter_NotStdout(t *testing.T) {
	// The regression this guards against: isTTY used to be
	// output.IsTerminal(os.Stdout), decided independently of which writer
	// the status line actually goes to. productionPollDeps now takes that
	// writer as a parameter — cmd.ErrOrStderr() in production — so there is
	// no way for it to consult os.Stdout at all; passing a *bytes.Buffer
	// (never a terminal) must always yield isTTY == false, whatever the
	// test process's real stdout is.
	deps := productionPollDeps(&bytes.Buffer{})
	if deps.isTTY {
		t.Error("productionPollDeps(&bytes.Buffer{}).isTTY = true, want false")
	}

	if deps.sleep == nil || deps.now == nil {
		t.Error("productionPollDeps() left sleep or now nil")
	}
}

// TestHarvestPoller_ProgressIsTTYFalse_NeverWritesControlCharsToStderr is
// the end-to-end proof, at the harvestPoller level, that a non-terminal
// stderr writer (the case productionPollDeps(cmd.ErrOrStderr()) now
// guarantees whenever stderr isn't a real *os.File terminal) never receives
// the \r / \x1b[K progress control sequences — even across a multi-poll
// queued -> running -> completed sequence that would otherwise report
// status on every step.
func TestHarvestPoller_ProgressIsTTYFalse_NeverWritesControlCharsToStderr(t *testing.T) {
	statuses := []api.HarvestStatus{api.Queued, api.Running, api.Running, api.Completed}
	call := 0

	var stderr bytes.Buffer

	p := &harvestPoller{
		fetch: func(_ context.Context) (api.Harvest, error) {
			h := api.Harvest{Status: statuses[call], StartedAt: time.Now()}
			call++

			return h, nil
		},
		sleep:  alwaysSleepsOK,
		now:    time.Now,
		stderr: &stderr,
		isTTY:  progressIsTTY(&stderr), // &stderr is a *bytes.Buffer, never a terminal
	}

	if _, err := p.Wait(context.Background()); err != nil {
		t.Fatalf("Wait() error = %v", err)
	}

	out := stderr.String()
	if strings.Contains(out, "\r") || strings.Contains(out, "\x1b[") {
		t.Errorf("stderr = %q, want no \\r or \\x1b[ control sequences (stderr here isn't a terminal)", out)
	}
}
