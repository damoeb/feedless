import { isNull, isUndefined } from 'lodash-es';

export const isUrl = (value: string): boolean => {
  if (!value || value.length < 3) {
    return false;
  }
  const potentialUrl = value.toLowerCase();
  if (
    potentialUrl.startsWith('http://') ||
    potentialUrl.startsWith('https://')
  ) {
    try {
      new URL(value);

      const urlPattern =
        /[-a-zA-Z0-9@:%._+~#=]{1,256}\.[a-zA-Z0-9()]{2,6}\b([-a-zA-Z0-9()@:%_+.~#?&//=]*)?/gi;
      return !!potentialUrl.match(new RegExp(urlPattern));
    } catch (e) {
      return false;
    }
  } else {
    return isUrl(`https://${potentialUrl}`);
  }
};

export const isValidUrl = (value: string): boolean => {
  const potentialUrl = value.trim();
  return (
    potentialUrl.toLowerCase().startsWith('http://') ||
    potentialUrl.toLowerCase().startsWith('https://')
  );
};
export const fixUrl = (value: string): string => {
  const potentialUrl = value?.trim();
  if (isValidUrl(potentialUrl)) {
    return potentialUrl;
  } else {
    if (isNull(value) || isUndefined(value)) {
      throw new Error('invalid url');
    } else {
      try {
        const fixedUrl = `https://${potentialUrl}`;
        new URL(fixedUrl);
        return fixedUrl;
      } catch (e) {
        throw new Error('invalid url');
      }
    }
  }
};
