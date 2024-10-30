import { writeFileSync } from 'fs';
import { translations } from './src/translations';

translations.forEach(translation => {
  const file = `src/assets/i18n/${translation.lang}.json`;
  console.log(`* ${file}`);
  writeFileSync(file, JSON.stringify(translation.translations, null, 2));
});
