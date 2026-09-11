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

// pollSleepFunc returns ctx.Err() as soon as ctx is done; tests inject one that never really sleeps.
type pollSleepFunc func(ctx context.Context, d time.Duration) error

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

// pollDeps are the seams tests replace: sleep, clock and TTY detection.
type pollDeps struct {
	sleep pollSleepFunc
	now   func() time.Time
	isTTY bool
}

// TTY detection uses stderr, where the status line goes, not stdout: the two can be redirected separately.
func productionPollDeps(stderr io.Writer) pollDeps {
	return pollDeps{sleep: realSleep, now: time.Now, isTTY: progressIsTTY(stderr)}
}

// Only an *os.File can be a terminal.
func progressIsTTY(w io.Writer) bool {
	f, ok := w.(*os.File)
	if !ok {
		return false
	}

	return output.IsTerminal(f)
}

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

// harvestPoller shows a single updating status line on a TTY while waiting.
type harvestPoller struct {
	fetch  func(ctx context.Context) (api.Harvest, error)
	sleep  pollSleepFunc
	now    func() time.Time
	stderr io.Writer
	isTTY  bool

	reported bool // whether reportStatus ever wrote to stderr; finish uses this to know whether to move past the status line with a newline
}

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

// finish ends the status line so the next output starts on its own line.
func (p *harvestPoller) finish() {
	if p.reported {
		_, _ = fmt.Fprintln(p.stderr)
	}
}

func formatElapsed(d time.Duration) string {
	if d < 0 {
		d = 0
	}

	total := int(d.Seconds())

	return fmt.Sprintf("%02d:%02d", total/60, total%60)
}
