package output

import "fmt"

// The API's PageSize maximum.
const maxPageSize = 100

type Page[T any] struct {
	Items   []T
	HasMore bool
}

// Paginate requests pages of min(limit, 100) until HasMore is false or limit items are collected.
func Paginate[T any](limit int, fetch func(page, pageSize int) (Page[T], error)) ([]T, error) {
	if limit <= 0 {
		return nil, nil
	}

	pageSize := limit
	if pageSize > maxPageSize {
		pageSize = maxPageSize
	}

	var items []T

	for page := 0; ; page++ {
		p, err := fetch(page, pageSize)
		if err != nil {
			return nil, fmt.Errorf("fetching page %d: %w", page, err)
		}

		items = append(items, p.Items...)

		if len(items) >= limit {
			return items[:limit], nil
		}

		if !p.HasMore {
			return items, nil
		}
	}
}
