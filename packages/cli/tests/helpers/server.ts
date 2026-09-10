import express, { type Request, type Response } from 'express'
import { type Server } from 'node:http'

/** Everything the CLI sent, so a test can assert on the outgoing request. */
export interface RecordedRequest {
  method: string
  /** Full path including the `/api/v1` prefix the CLI adds, e.g. `/api/v1/user`. */
  path: string
  url: string
  query: Record<string, string>
  headers: Record<string, string>
  body: unknown
}

interface Stub {
  method: string
  /** Logical path already prefixed with `/api/v1`. Matched as a prefix. */
  path: string
  status: number
  body: unknown
  contentType: string
}

export interface MockServerOptions {
  /** Default response body for any request without a matching stub. */
  body?: unknown
  /** Default response status. */
  status?: number
  contentType?: string
}

export interface MockServer {
  port: number
  /** Base URL to hand the CLI as `FL_API_URL` — the CLI appends `/api/v1`. */
  url: string
  requests: RecordedRequest[]
  /** Register a response for a logical path (no `/api/v1` prefix). Last wins. */
  stub: (method: string, path: string, body: unknown, status?: number) => void
  reset: () => void
  stop: () => Promise<void>
}

/**
 * A stand-in for the Feedless HTTP API. Records every request and replays a
 * stubbed (or default) response. The CLI targets `${FL_API_URL}/api/v1`, so
 * stub paths are given logically (`/user`) and matched against `/api/v1/user`.
 */
export async function mockServer(opts: MockServerOptions = {}): Promise<MockServer> {
  const app = express()
  app.use(express.json())

  const requests: RecordedRequest[] = []
  let stubs: Stub[] = []

  app.use((req: Request, res: Response) => {
    requests.push({
      method: req.method,
      path: req.path,
      url: req.originalUrl,
      query: req.query as Record<string, string>,
      headers: req.headers as Record<string, string>,
      body: req.body,
    })

    const stub = stubs
      .slice()
      .reverse()
      .find((s) => s.method === req.method && req.path.startsWith(s.path))

    const status = stub?.status ?? opts.status ?? 200
    const contentType = stub?.contentType ?? opts.contentType ?? 'application/json'
    const body = stub ? stub.body : opts.body ?? {}

    if (status === 204 || body === undefined || body === null) {
      res.status(status).end()
      return
    }
    res.status(status).set('Content-Type', contentType)
    if (contentType === 'application/json') {
      res.json(body)
    } else {
      res.send(body)
    }
  })

  const server: Server = await new Promise((resolve) => {
    const s = app.listen(0, () => resolve(s))
  })
  const address = server.address()
  if (!address || typeof address === 'string') throw new Error('failed to get server address')
  const port = address.port

  return {
    port,
    url: `http://localhost:${port}`,
    requests,
    stub(method, path, body, status = 200) {
      stubs.push({
        method: method.toUpperCase(),
        path: `/api/v1${path}`,
        status,
        body,
        contentType: typeof body === 'string' ? 'text/plain' : 'application/json',
      })
    },
    reset() {
      requests.length = 0
      stubs = []
    },
    async stop() {
      server.closeAllConnections()
      return new Promise<void>((resolve, reject) => {
        server.close((err) => (err ? reject(err) : resolve()))
      })
    },
  }
}
