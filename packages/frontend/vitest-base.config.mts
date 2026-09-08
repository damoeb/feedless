import { defineConfig } from 'vitest/config';

export default defineConfig({
  test: {
    server: {
      deps: {
        // @ionic/angular's fesm2022 bundle imports the directory '@ionic/core/components',
        // which Node's ESM loader rejects. Letting Vite resolve the package instead of
        // externalizing it to Node fixes the import.
        inline: [/@ionic[\\/]angular/],
      },
    },
  },
});
