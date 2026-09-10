import { test } from 'node:test'
import assert from 'node:assert/strict'
import { runFl } from './helpers/run.ts'

test('--version prints the version and exits 0', async () => {
  const r = await runFl(['--version'])
  assert.equal(r.code, 0)
  assert.match(r.stdout, /^fl \d+\.\d+\.\d+/)
})

test('-v is an alias for --version', async () => {
  const r = await runFl(['-v'])
  assert.equal(r.code, 0)
  assert.match(r.stdout, /^fl \d+\.\d+\.\d+/)
})

test('no arguments prints usage and exits 0', async () => {
  const r = await runFl([])
  assert.equal(r.code, 0)
  assert.match(r.stdout, /Usage: fl/)
})

test('help prints usage', async () => {
  const r = await runFl(['help'])
  assert.equal(r.code, 0)
  assert.match(r.stdout, /Usage: fl/)
})

test('an unknown command exits 1 with usage on stderr', async () => {
  const r = await runFl(['bogus'])
  assert.equal(r.code, 1)
  assert.match(r.stderr, /unknown command: bogus/)
  assert.match(r.stderr, /Usage: fl/)
})
