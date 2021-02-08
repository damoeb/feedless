function patchPostCSS(webpackConfig, tailwindConfig, components = false) {
  if (!tailwindConfig) {
    console.error('Missing tailwind config :', tailwindConfig);
    return;
  }
  const pluginName = "autoprefixer";
  for (const rule of webpackConfig.module.rules) {
    if (!(rule.use && rule.use.length > 0) || (!components && rule.exclude)) {
      continue;
    }
    for (const useLoader of rule.use) {
      if (!(useLoader.options && useLoader.options.postcssOptions)) {
        continue;
      }
      const originPostcssOptions = useLoader.options.postcssOptions;
      useLoader.options.postcssOptions = (loader) => {
        const _postcssOptions = originPostcssOptions(loader);
        const insertIndex = _postcssOptions.plugins.findIndex(
          ({postcssPlugin}) => postcssPlugin && postcssPlugin.toLowerCase() === pluginName
        );
        if (insertIndex !== -1) {
          _postcssOptions.plugins.splice(insertIndex, 0, ["tailwindcss", tailwindConfig]);
        } else {
          console.error(`${pluginName} not found in postcss plugins`);
        }
        return _postcssOptions;
      };
    }
  }
}

module.exports = (config) => {
  const tailwindConfig = require("./tailwind.config.js");
  patchPostCSS(config, tailwindConfig, true);
  return config;
};
