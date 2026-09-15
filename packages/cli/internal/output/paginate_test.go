package output

import (
	"errors"
	"reflect"
	"testing"
)

func TestPaginate_StopsWhenHasMoreFalse(t *testing.T) {
	pages := [][]int{{1, 2, 3}, {4, 5}}
	calls := 0

	got, err := Paginate(100, func(page, pageSize int) (Page[int], error) {
		calls++
		if page >= len(pages) {
			t.Fatalf("fetch called for page %d, want at most %d calls", page, len(pages))
		}

		return Page[int]{Items: pages[page], HasMore: page < len(pages)-1}, nil
	})
	if err != nil {
		t.Fatalf("Paginate() error = %v", err)
	}

	want := []int{1, 2, 3, 4, 5}
	if !reflect.DeepEqual(got, want) {
		t.Errorf("got %v, want %v", got, want)
	}
	if calls != 2 {
		t.Errorf("fetch called %d times, want 2", calls)
	}
}

func TestPaginate_StopsAtLimit_TrimmingTheLastPage(t *testing.T) {
	got, err := Paginate(5, func(page, pageSize int) (Page[int], error) {
		items := make([]int, pageSize)
		for i := range items {
			items[i] = page*pageSize + i
		}

		return Page[int]{Items: items, HasMore: true}, nil
	})
	if err != nil {
		t.Fatalf("Paginate() error = %v", err)
	}

	if len(got) != 5 {
		t.Errorf("len(got) = %d, want 5", len(got))
	}
}

func TestPaginate_PageSizeIsMinOfLimitAnd100(t *testing.T) {
	var gotPageSize int

	_, err := Paginate(500, func(page, pageSize int) (Page[int], error) {
		gotPageSize = pageSize
		return Page[int]{Items: make([]int, pageSize), HasMore: false}, nil
	})
	if err != nil {
		t.Fatalf("Paginate() error = %v", err)
	}

	if gotPageSize != 100 {
		t.Errorf("pageSize = %d, want 100 (capped)", gotPageSize)
	}
}

func TestPaginate_PageSizeIsLimitWhenBelow100(t *testing.T) {
	var gotPageSize int

	_, err := Paginate(7, func(page, pageSize int) (Page[int], error) {
		gotPageSize = pageSize
		return Page[int]{Items: make([]int, pageSize), HasMore: false}, nil
	})
	if err != nil {
		t.Fatalf("Paginate() error = %v", err)
	}

	if gotPageSize != 7 {
		t.Errorf("pageSize = %d, want 7", gotPageSize)
	}
}

func TestPaginate_ZeroLimit_FetchesNothing(t *testing.T) {
	called := false

	got, err := Paginate(0, func(page, pageSize int) (Page[int], error) {
		called = true
		return Page[int]{}, nil
	})
	if err != nil {
		t.Fatalf("Paginate() error = %v", err)
	}

	if called {
		t.Error("fetch was called, want it never called for limit=0")
	}
	if got != nil {
		t.Errorf("got %v, want nil", got)
	}
}

func TestPaginate_PropagatesFetchError(t *testing.T) {
	wantErr := errors.New("boom")

	_, err := Paginate(10, func(page, pageSize int) (Page[int], error) {
		return Page[int]{}, wantErr
	})
	if !errors.Is(err, wantErr) {
		t.Errorf("Paginate() error = %v, want it to wrap %v", err, wantErr)
	}
}
