import express, { NextFunction, Request, Response } from 'express';
import { createProxyMiddleware } from 'http-proxy-middleware';
import { AddressInfo } from 'net';

const PORT: number = parseInt(process.env.PORT || '80', 10);
const TARGET: string = process.env.TARGET || 'nominatim.default.svc.cluster.local:8080';

const app = express();

// Middleware
app.use((req: Request, res: Response, next: NextFunction) => {
  res.header('Access-Control-Allow-Origin', 'https://lokale.events');
  res.header('Access-Control-Allow-Methods', 'GET,OPTIONS');
  res.header('Access-Control-Allow-Headers', 'Origin, X-Requested-With, Content-Type, Accept');

  if (req.method === 'OPTIONS') {
    return res.sendStatus(204);
  }

  next();
});

app.use(
  '/',
  createProxyMiddleware({
    target: TARGET,
    changeOrigin: true
  })
);

// Start Server
const server = app.listen(PORT, () => {
  const address = server.address() as AddressInfo;
  console.log(`CORS proxy running at http://localhost:${address.port} → ${TARGET}`);
});
