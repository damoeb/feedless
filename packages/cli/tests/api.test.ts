import { test } from 'node:test'
import assert from 'node:assert/strict'
import { mockServer, type MockServer } from './helpers/server.ts'
import { runFl } from './helpers/run.ts'
import user from './fixtures/user.json' with { type: 'json' }

// `fl api` is the raw escape hatch: forward a path/method/body to the API with
// auth attached, and pretty-print the JSON response.
test('fl api sends Authorization Bearer and hits /api/v1', async () => {
  const s = await mockServer({ body: user })
  try {
    const r = await runFl(['api', '/user'], { FL_API_URL: s.url, FL_TOKEN: 'tok-abc' })
    assert.equal(r.code, 0, r.stderr)
    assert.equal(s.requests[0].path, '/api/v1/user')
    assert.equal(s.requests[0].method, 'GET')
    assert.equal(s.requests[0].headers.authorization, 'Bearer tok-abc')
    // The deprecated header must never be sent.
    assert.equal(s.requests[0].headers.authentication, undefined)
  } finally {
    await s.stop()
  }
})

test('fl api --method POST --data sends a JSON body', async () => {
  const s = await mockServer({ body: { id: 'r1' }, status: 201 })
  try {
    const r = await runFl(
      ['api', '/repositories', '--method', 'post', '--data', '{"title":"Raw"}'],
      { FL_API_URL: s.url, FL_TOKEN: 'tok-abc' },
    )
    assert.equal(r.code, 0, r.stderr)
    const call = s.requests[0]
    assert.equal(call.method, 'POST') // method upper-cased
    assert.equal(call.path, '/api/v1/repositories')
    assert.equal((call.body as Record<string, unknown>).title, 'Raw')
    assert.match(call.headers['content-type'] ?? '', /application\/json/)
  } finally {
    await s.stop()
  }
})

test('fl api pretty-prints the JSON response', async () => {
  const s = await mockServer({ body: user })
  try {
    const r = await runFl(['api', '/user'], { FL_API_URL: s.url, FL_TOKEN: 't' })
    assert.equal(r.code, 0, r.stderr)
    // jq pretty-print puts each field on its own indented line.
    assert.match(r.stdout, /"email": "test@example.com"/)
    assert.match(r.stdout, /\n {2}"id"/)
  } finally {
    await s.stop()
  }
})

test('surfaces ApiError code/message/corrId on 404', async () => {
  const s = await mockServer({
    status: 404,
    body: { code: 'NOT_FOUND', message: 'no such repo', corrId: 'c1' },
  })
  try {
    const r = await runFl(['api', '/repositories/x'], { FL_API_URL: s.url, FL_TOKEN: 't' })
    assert.equal(r.code, 2)
    assert.match(r.stderr, /HTTP 404 — NOT_FOUND: no such repo \(corrId=c1\)/)
  } finally {
    await s.stop()
  }
})

test('tolerates an error response with no body', async () => {
  const s = await mockServer({ status: 500, body: null })
  try {
    const r = await runFl(['api', '/user'], { FL_API_URL: s.url, FL_TOKEN: 't' })
    assert.equal(r.code, 2)
    assert.match(r.stderr, /HTTP 500/)
  } finally {
    await s.stop()
  }
})

test('refuses plain http to a non-loopback host', async () => {
  const r = await runFl(['api', '/user'], {
    FL_API_URL: 'http://feedless.example.com',
    FL_TOKEN: 't',
  })
  assert.equal(r.code, 1)
  assert.match(r.stderr, /refusing to send token over plain http/)
})

test('allows plain http to loopback', async () => {
  const s = await mockServer({ body: user })
  try {
    const r = await runFl(['api', '/user'], { FL_API_URL: s.url, FL_TOKEN: 't' })
    assert.equal(r.code, 0, r.stderr)
  } finally {
    await s.stop()
  }
})

test('exits 1 with a login hint when no token is configured', async () => {
  const s: MockServer = await mockServer({ body: user })
  try {
    const r = await runFl(['api', '/user'], { FL_API_URL: s.url })
    assert.equal(r.code, 1)
    assert.match(r.stderr, /fl auth login/)
    assert.equal(s.requests.length, 0) // never hit the network
  } finally {
    await s.stop()
  }
})

test('exits 1 when no API url is configured', async () => {
  const r = await runFl(['api', '/user'], { FL_TOKEN: 't' })
  assert.equal(r.code, 1)
  assert.match(r.stderr, /FL_API_URL|auth login/)
})
