import { execFile } from 'node:child_process'
import { mkdtempSync } from 'node:fs'
import { tmpdir } from 'node:os'
import path, { dirname } from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = dirname(fileURLToPath(import.meta.url))
const FL_PATH = path.resolve(__dirname, '../../bin/fl')

export interface FlResult {
  stdout: string
  stderr: string
  code: number
}

export interface RunOptions {
  /** Written to the subprocess's stdin (e.g. a token for `auth login`). */
  input?: string
  /** Prepend this dir to PATH — used to shim `security` in auth tests. */
  binDir?: string
}

/**
 * Run `bin/fl` as a real subprocess with a throwaway HOME (so keychain/config
 * state never leaks between tests or touches the developer's machine). The
 * environment is built from scratch — nothing from the caller's shell leaks in,
 * so a test that omits FL_TOKEN really runs without a token.
 */
export async function runFl(
  args: string[],
  env: Record<string, string> = {},
  opts: RunOptions = {},
): Promise<FlResult> {
  const home = mkdtempSync(path.join(tmpdir(), 'fl-home-'))
  const basePath = process.env.PATH ?? ''
  const fullEnv: Record<string, string> = {
    PATH: opts.binDir ? `${opts.binDir}:${basePath}` : basePath,
    HOME: home,
    ...env,
  }

  return new Promise<FlResult>((resolve) => {
    const child = execFile(
      'bash',
      [FL_PATH, ...args],
      { env: fullEnv, timeout: 5_000, maxBuffer: 1024 * 1024 },
      (error, stdout, stderr) => {
        resolve({
          stdout: stdout ?? '',
          stderr: stderr ?? '',
          code: typeof error?.code === 'number' ? error.code : error ? 1 : 0,
        })
      },
    )
    child.stdin?.on('error', () => {})
    child.stdin?.end(opts.input ?? '')
  })
}
