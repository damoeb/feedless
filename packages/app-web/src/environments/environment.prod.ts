import { AppEnvironment } from '../app/app.module';

export const environment: AppEnvironment = {
  production: true,
  product: () => {
    switch (location.host) {
      // todo
      default:
        return 'feedless';
    }
  },
};
