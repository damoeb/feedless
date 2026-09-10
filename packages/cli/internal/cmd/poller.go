package cmd

import (
	"context"
	"fmt"
	"io"
	"os"
	"time"

	"github.com/damoeb/feedless/packages/cli/internal/api"
	"github.com/damoeb/feedless/packages/cli/internal/output"
)

// pollSleepFunc sleeps for d, honoring ctx cancellation: it returns
// ctx.Err() (non-nil) as soon as ctx is done, instead of sleeping the full
// duration. realSleep is the production implementation; tests inject a fake
// that advances an injected clock instantly instead of really sleeping (per
// the brief: "must not really sleep").
type pollSleepFunc func(ctx context.Context, d time.Duration) error

// realSleep is pollSleepFunc's production implementation.
func realSleep(ctx context.Context, d time.Duration) error {
	timer := time.NewTimer(d)
	defer timer.Stop()

	select {
	case <-ctx.Done():
		return ctx.Err()
	case <-timer.C:
		return nil
	}
}

// pollDeps bundles harvestPoller's injectable seams — sleep/now for tests,
// isTTY for the status line — so `source run`'s cobra wiring passes
// productionPollDeps() while a test passes its own fakes directly to
// runSourceRun, without going through cobra flag parsing or a real clock.
type pollDeps struct {
	sleep pollSleepFunc
	now   func() time.Time
	isTTY bool
}

// productionPollDeps is what newSourceRunCmd's RunE actually uses: a real
// sleeper, the real clock, and the real stdout TTY detection.
func productionPollDeps() pollDeps {
	return pollDeps{sleep: realSleep, now: time.Now, isTTY: output.IsTerminal(os.Stdout)}
}

// pollDelay returns the wait before poll number n (0-based): 1s before the
// first poll, then 2s, doubling on each subsequent poll, capped at 10s —
// "first after 1s, then every 2s, backing off to at most 10s" (brief,
// requirement 1).
func pollDelay(n int) time.Duration {
	if n == 0 {
		return time.Second
	}

	d := 2 * time.Second
	for i := 1; i < n && d < 10*time.Second; i++ {
		d *= 2
	}

	if d > 10*time.Second {
		d = 10 * time.Second
	}

	return d
}

// harvestPoller polls a harvest (fetch) until its status is completed,
// sleeping between polls via sleep and — on a TTY — writing a single
// updating status line to stderr ("queued…", "running… 00:42"). It's the
// small, independently testable polling unit the brief asks for: Wait takes
// only a context, so a test builds one with a fake fetch/sleep/now and
// drives it directly, no httptest server or cobra command required (see
// poller_test.go).
type harvestPoller struct {
	fetch  func(ctx context.Context) (api.Harvest, error)
	sleep  pollSleepFunc
	now    func() time.Time
	stderr io.Writer
	isTTY  bool

	reported bool // whether reportStatus ever wrote to stderr; finish uses this to know whether to move past the status line with a newline
}

// Wait polls until the harvest is completed or ctx is cancelled (or fetch
// fails), returning the completed Harvest or the error that stopped
// polling.
func (p *harvestPoller) Wait(ctx context.Context) (api.Harvest, error) {
	defer p.finish()

	for n := 0; ; n++ {
		if err := p.sleep(ctx, pollDelay(n)); err != nil {
			return api.Harvest{}, err
		}

		h, err := p.fetch(ctx)
		if err != nil {
			return api.Harvest{}, err
		}

		p.reportStatus(h)

		if h.Status == api.Completed {
			return h, nil
		}
	}
}

func (p *harvestPoller) reportStatus(h api.Harvest) {
	if !p.isTTY {
		return
	}

	var status string

	switch h.Status {
	case api.Queued:
		status = "queued…"
	case api.Running:
		status = "running… " + formatElapsed(p.now().Sub(h.StartedAt))
	default:
		return // completed — the final summary follows right after, no status line needed
	}

	_, _ = fmt.Fprintf(p.stderr, "\r%s\x1b[K", status)

	p.reported = true
}

// finish moves the cursor past the status line once polling stops, so
// whatever prints next (the summary, or the interrupt hint) starts on its
// own line. A no-op if reportStatus never wrote anything (non-TTY, or the
// harvest was already completed on the very first poll).
func (p *harvestPoller) finish() {
	if p.reported {
		_, _ = fmt.Fprintln(p.stderr)
	}
}

// formatElapsed renders d as mm:ss for the "running… 00:42" status line.
func formatElapsed(d time.Duration) string {
	if d < 0 {
		d = 0
	}

	total := int(d.Seconds())

	return fmt.Sprintf("%02d:%02d", total/60, total%60)
}
