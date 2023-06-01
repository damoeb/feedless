const CompressionPlugin = require("compression-webpack-plugin");
const BrotliPlugin = require("brotli-webpack-plugin");

// https://dev.to/oyemade/improve-performance-in-an-angular-app-using-brotli-and-gzip-text-compression-2p1e
module.exports = {
  plugins: [
    new CompressionPlugin({
      algorithm: "gzip",
    }),
    new BrotliPlugin(),
  ],
};
