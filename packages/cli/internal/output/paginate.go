package output

import "fmt"

// maxPageSize is the API's page size ceiling (openapi.yaml's PageSize
// parameter: maximum: 100).
const maxPageSize = 100

// Page is one page of list results, as every list endpoint's response
// shape reduces to: the items on this page, and whether another page is
// available (SourceListResponse etc.'s hasMore; RepositoryListResponse's
// totalCount isn't needed here).
type Page[T any] struct {
	Items   []T
	HasMore bool
}

// Paginate collects up to limit items by calling fetch for page 0, 1, 2, …
// with pageSize = min(limit, 100) (the API's own ceiling), stopping as soon
// as a page reports HasMore=false or limit items have been collected. A
// limit <= 0 fetches nothing and returns nil.
//
// Callers wrap one list operation as fetch, e.g.:
//
//	items, err := output.Paginate(limit, func(page, pageSize int) (output.Page[api.Source], error) {
//	    resp, err := apiClient.API.ListSourcesWithResponse(ctx, repoID, &api.ListSourcesParams{
//	        Page: &page, PageSize: &pageSize,
//	    })
//	    if err != nil {
//	        return output.Page[api.Source]{}, err
//	    }
//	    if apiErr := cmd.NewAPIError(resp, ""); apiErr != nil {
//	        return output.Page[api.Source]{}, apiErr
//	    }
//	    return output.Page[api.Source]{Items: resp.JSON200.Items, HasMore: resp.JSON200.HasMore}, nil
//	})
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
