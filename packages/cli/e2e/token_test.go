//go:build e2e

package e2e

import (
	"bytes"
	"context"
	"encoding/json"
	"errors"
	"fmt"
	"io"
	"net/http"
	"strings"
)

// MintAPIToken obtains an API token for the user identified by email and
// secretKey the way a user gets one today: log in over GraphQL (authUser,
// which answers a session JWT), then create a user secret with that session
// (createUserSecret, which answers an API JWT). The second is what the web
// UI hands a user to paste into `feedctl auth login --with-token`.
func MintAPIToken(ctx context.Context, coreURL, email, secretKey string) (string, error) {
	var login struct {
		AuthUser struct {
			Token string `json:"token"`
		} `json:"authUser"`
	}

	err := graphqlRequest(ctx, coreURL, "",
		`mutation Login($data: AuthUserInput!) { authUser(data: $data) { token } }`,
		map[string]any{"data": map[string]string{"email": email, "secretKey": secretKey}},
		&login)
	if err != nil {
		return "", fmt.Errorf("authUser: %w", err)
	}

	if login.AuthUser.Token == "" {
		return "", errors.New("authUser answered no token")
	}

	var secret struct {
		CreateUserSecret struct {
			Value string `json:"value"`
		} `json:"createUserSecret"`
	}

	err = graphqlRequest(ctx, coreURL, login.AuthUser.Token, `mutation { createUserSecret { value } }`, nil, &secret)
	if err != nil {
		return "", fmt.Errorf("createUserSecret: %w", err)
	}

	if secret.CreateUserSecret.Value == "" {
		return "", errors.New("createUserSecret answered no value")
	}

	return secret.CreateUserSecret.Value, nil
}

// graphqlRequest POSTs one GraphQL operation to coreURL/graphql and decodes
// its data into out. A GraphQL error is an error, even on HTTP 200.
func graphqlRequest(ctx context.Context, coreURL, bearer, query string, variables map[string]any, out any) error {
	payload, err := json.Marshal(map[string]any{"query": query, "variables": variables})
	if err != nil {
		return fmt.Errorf("encoding request: %w", err)
	}

	req, err := http.NewRequestWithContext(ctx, http.MethodPost, coreURL+"/graphql", bytes.NewReader(payload))
	if err != nil {
		return fmt.Errorf("building request: %w", err)
	}

	req.Header.Set("Content-Type", "application/json")
	if bearer != "" {
		req.Header.Set("Authorization", "Bearer "+bearer)
	}

	resp, err := http.DefaultClient.Do(req)
	if err != nil {
		return err
	}
	defer func() { _ = resp.Body.Close() }()

	body, err := io.ReadAll(resp.Body)
	if err != nil {
		return fmt.Errorf("reading response: %w", err)
	}

	if resp.StatusCode != http.StatusOK {
		return fmt.Errorf("HTTP %s: %s", resp.Status, body)
	}

	var envelope struct {
		Data   json.RawMessage `json:"data"`
		Errors []struct {
			Message string `json:"message"`
		} `json:"errors"`
	}

	if err := json.Unmarshal(body, &envelope); err != nil {
		return fmt.Errorf("decoding response %q: %w", body, err)
	}

	if len(envelope.Errors) > 0 {
		messages := make([]string, len(envelope.Errors))
		for i, e := range envelope.Errors {
			messages[i] = e.Message
		}

		return fmt.Errorf("GraphQL errors: %s", strings.Join(messages, "; "))
	}

	if err := json.Unmarshal(envelope.Data, out); err != nil {
		return fmt.Errorf("decoding data %q: %w", envelope.Data, err)
	}

	return nil
}
