export interface WebToFeedParams {
  url: string;
  linkPath: string;
  extendContent: string;
  contextPath: string;
  paginationPath: string;
  datePath: string;
  prerender: string;
  prerenderWaitUntil: string;
  filter: string;
  version: string;
  format: string;
  articleRecovery: string;
  prerenderScript: string;
  strictMode: string;
  eventFeed: string;
}

export const webToFeedParams: WebToFeedParams = {
  url: 'u',
  linkPath: 'l',
  extendContent: 'ec',
  contextPath: 'cp',
  paginationPath: 'pp',
  datePath: 'dp',
  prerender: 'p',
  prerenderWaitUntil: 'aw',
  filter: 'q',
  version: 'v',
  format: 'f',
  articleRecovery: 'ar',
  prerenderScript: 'ps',
  strictMode: 'sm',
  eventFeed: 'ef',
};

export interface WebToPageChangeParams {
  url: string;
  prerender: string;
  prerenderWaitUntil: string;
  version: string;
  type: string;
  format: string;
  xpath: string;
  prerenderScript: string;
}

export const webToPageChangeParams: WebToPageChangeParams = {
  url: 'u',
  prerender: 'p',
  prerenderWaitUntil: 'aw',
  version: 'v',
  type: 't',
  format: 'f',
  xpath: 'x',
  prerenderScript: 'ps',
};
