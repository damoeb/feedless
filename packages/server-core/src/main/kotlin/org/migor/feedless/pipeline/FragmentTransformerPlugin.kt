package org.migor.feedless.pipeline

import org.migor.feedless.actions.ExecuteAction
import org.migor.feedless.common.HttpResponse
import org.migor.feedless.scrape.LogCollector

interface FragmentTransformerPlugin : Plugin {

  suspend fun transformFragment(
    action: ExecuteAction,
    data: HttpResponse,
    logger: LogCollector,
  ): FragmentOutput

}
