import http from "k6/http";

import { sleep } from "k6";

// k6 run --vus 400 --duration 60s performance-test.js
export default function () {

  // http.get('http://localhost:8080')

  const body = "{\"operationName\":\"repositoryById\",\"variables\":{\"repository\":{\"where\":{\"id\":\"a0af7fa0-070f-42ff-8956-836e848ad912\"}}},\"query\":\"query repositoryById($repository: RepositoryWhereInput!) {\\n  repository(data: $repository) {\\n    ...RepositoryFull\\n    annotations {\\n      votes {\\n        id\\n        flag {\\n          value\\n          __typename\\n        }\\n        upVote {\\n          value\\n          __typename\\n        }\\n        downVote {\\n          value\\n          __typename\\n        }\\n        __typename\\n      }\\n      __typename\\n    }\\n    __typename\\n  }\\n}\\n\\nfragment RepositoryFull on Repository {\\n  ...RepositoryFragment\\n  frequency(groupBy: createdAt) {\\n    count\\n    group\\n    __typename\\n  }\\n  sources {\\n    ...SourceFragment\\n    __typename\\n  }\\n  __typename\\n}\\n\\nfragment RepositoryFragment on Repository {\\n  id\\n  ownerId\\n  title\\n  description\\n  product\\n  visibility\\n  tags\\n  createdAt\\n  lastUpdatedAt\\n  nextUpdateAt\\n  plugins {\\n    pluginId\\n    params {\\n      ...PluginExecutionParamsFragment\\n      __typename\\n    }\\n    __typename\\n  }\\n  annotations {\\n    downVotes\\n    upVotes\\n    __typename\\n  }\\n  refreshCron\\n  hasDisabledSources\\n  shareKey\\n  disabledFrom\\n  archived\\n  documentCount\\n  retention {\\n    maxAgeDays\\n    maxCapacity\\n    __typename\\n  }\\n  __typename\\n}\\n\\nfragment PluginExecutionParamsFragment on PluginExecutionParams {\\n  org_feedless_feed {\\n    generic {\\n      ...SelectorsFragment\\n      __typename\\n    }\\n    __typename\\n  }\\n  org_feedless_diff_email_forward {\\n    ...DiffEmailForwardParamsFragment\\n    __typename\\n  }\\n  org_feedless_filter {\\n    composite {\\n      ...CompositeFilterParamsFragment\\n      __typename\\n    }\\n    expression\\n    __typename\\n  }\\n  org_feedless_fulltext {\\n    readability\\n    summary\\n    inheritParams\\n    __typename\\n  }\\n  jsonData\\n  __typename\\n}\\n\\nfragment SelectorsFragment on Selectors {\\n  contextXPath\\n  linkXPath\\n  extendContext\\n  dateXPath\\n  paginationXPath\\n  dateIsStartOfEvent\\n  __typename\\n}\\n\\nfragment DiffEmailForwardParamsFragment on DiffEmailForwardParams {\\n  inlineDiffImage\\n  inlineLatestImage\\n  inlinePreviousImage\\n  compareBy {\\n    field\\n    fragmentNameRef\\n    __typename\\n  }\\n  nextItemMinIncrement\\n  __typename\\n}\\n\\nfragment CompositeFilterParamsFragment on CompositeFilterParams {\\n  exclude {\\n    ...CompositeFieldFilterParamsFragment\\n    __typename\\n  }\\n  include {\\n    ...CompositeFieldFilterParamsFragment\\n    __typename\\n  }\\n  __typename\\n}\\n\\nfragment CompositeFieldFilterParamsFragment on CompositeFieldFilterParams {\\n  index {\\n    operator\\n    value\\n    __typename\\n  }\\n  title {\\n    ...StringFilterParamsFragment\\n    __typename\\n  }\\n  content {\\n    ...StringFilterParamsFragment\\n    __typename\\n  }\\n  link {\\n    ...StringFilterParamsFragment\\n    __typename\\n  }\\n  __typename\\n}\\n\\nfragment StringFilterParamsFragment on StringFilterParams {\\n  operator\\n  value\\n  __typename\\n}\\n\\nfragment SourceFragment on Source {\\n  id\\n  disabled\\n  tags\\n  latLng {\\n    lat\\n    lon\\n    __typename\\n  }\\n  lastErrorMessage\\n  recordCount\\n  title\\n  flow {\\n    ...ScrapeFlowFragment\\n    __typename\\n  }\\n  __typename\\n}\\n\\nfragment ScrapeFlowFragment on ScrapeFlow {\\n  sequence {\\n    ...ActionFragment\\n    __typename\\n  }\\n  __typename\\n}\\n\\nfragment ActionFragment on ScrapeAction {\\n  execute {\\n    pluginId\\n    params {\\n      ...PluginExecutionParamsFragment\\n      __typename\\n    }\\n    __typename\\n  }\\n  extract {\\n    fragmentName\\n    selectorBased {\\n      fragmentName\\n      emit\\n      xpath {\\n        value\\n        __typename\\n      }\\n      max\\n      __typename\\n    }\\n    imageBased {\\n      boundingBox {\\n        x\\n        y\\n        h\\n        w\\n        __typename\\n      }\\n      __typename\\n    }\\n    __typename\\n  }\\n  type {\\n    element {\\n      value\\n      __typename\\n    }\\n    __typename\\n  }\\n  fetch {\\n    get {\\n      ...HttpGetRequestFragment\\n      __typename\\n    }\\n    __typename\\n  }\\n  click {\\n    position {\\n      x\\n      y\\n      __typename\\n    }\\n    element {\\n      xpath {\\n        value\\n        __typename\\n      }\\n      name {\\n        value\\n        __typename\\n      }\\n      __typename\\n    }\\n    __typename\\n  }\\n  header {\\n    value\\n    name\\n    __typename\\n  }\\n  select {\\n    element {\\n      value\\n      __typename\\n    }\\n    selectValue\\n    __typename\\n  }\\n  __typename\\n}\\n\\nfragment HttpGetRequestFragment on HttpGetRequest {\\n  url {\\n    literal\\n    variable\\n    __typename\\n  }\\n  timeout\\n  viewport {\\n    ...ViewportFragment\\n    __typename\\n  }\\n  forcePrerender\\n  additionalWaitSec\\n  language\\n  waitUntil\\n  __typename\\n}\\n\\nfragment ViewportFragment on ViewPort {\\n  height\\n  width\\n  isLandscape\\n  isMobile\\n  __typename\\n}\"}";
  http.post("http://localhost:8080/graphql", body, {
    "headers": {
      "accept": "*/*",
      "accept-language": "en-US,en;q=0.9",
      "content-type": "application/json",
      "sec-ch-ua": "\"Brave\";v=\"131\", \"Chromium\";v=\"131\", \"Not_A Brand\";v=\"24\"",
      "sec-ch-ua-mobile": "?0",
      "sec-ch-ua-platform": "\"Linux\"",
      "sec-fetch-dest": "empty",
      "sec-fetch-mode": "cors",
      "sec-fetch-site": "same-site",
      "sec-gpc": "1",
      "x-corr-id": "5WWHS",
      "x-product": "feedless",
      "cookie": "TOKEN=eyJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiNzE4MTU5NDAtMTBiZi00MDNhLWFhOTQtYWUyYWRjZGQwYmFhIiwiaXNzIjoiaHR0cDovL2xvY2FsaG9zdDo4MDgwIiwiaWQiOiJyaWNoIiwiZXhwIjoxNzMyMzEzOTY5NDUwLCJ0b2tlbl90eXBlIjoiVVNFUiIsImlhdCI6MTczMjE0MTE2OTQ1MCwiYXV0aG9yaXRpZXMiOlsiQU5PTllNT1VTIiwiVVNFUiJdfQ.oEWicxDNQmLlScU78oO5-pkVoGUVUd7TnzCjz5BktaA",
      "Referer": "http://localhost:4200/",
      "Referrer-Policy": "strict-origin-when-cross-origin"
    },
  });

  sleep(1);

}
