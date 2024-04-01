import { AppConfig, feedlessConfig } from './src/app/feedless-config';

type ProductId2AppConfig = { [k: string]: AppConfig };
const apps: ProductId2AppConfig = feedlessConfig.apps.reduce((acc, app) => {
  acc[app.id] = app;
  return acc;
}, {} as ProductId2AppConfig);

const config = JSON.parse(JSON.stringify(feedlessConfig));
config.apps = apps;

console.log(JSON.stringify(config, null, 2));
