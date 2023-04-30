package org.migor.rich.rss.service

import org.migor.rich.rss.AppProfiles
import org.migor.rich.rss.trigger.plugins.FulltextPlugin
import org.migor.rich.rss.trigger.plugins.InlineImagesPlugin
import org.migor.rich.rss.trigger.plugins.ScorePlugin
import org.migor.rich.rss.trigger.plugins.WebDocumentPlugin
import org.migor.rich.rss.trigger.plugins.graph.WebGraphPlugin
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.env.Environment
import org.springframework.core.env.Profiles
import org.springframework.stereotype.Service

@Service
@Profile(AppProfiles.database)
class PluginsService {

  @Autowired
  lateinit var scorePlugin: ScorePlugin

  @Autowired
  lateinit var environment: Environment

  @Autowired
  lateinit var fulltextPlugin: FulltextPlugin

  @Autowired
  lateinit var webGraphPlugin: WebGraphPlugin

  @Autowired
  lateinit var inlineImagesPlugin: InlineImagesPlugin

  fun resolvePlugins(harvestItems: Boolean = true, inlineImages: Boolean = false): List<WebDocumentPlugin> {
    val plugins: MutableList<WebDocumentPlugin> = mutableListOf(
      scorePlugin
    )

    if (harvestItems) {
      plugins.add(fulltextPlugin)
    }

    if (inlineImages) {
      plugins.add(inlineImagesPlugin)
    }

    if (environment.acceptsProfiles(Profiles.of(AppProfiles.webGraph))) {
      plugins.add(webGraphPlugin)
    }
    return plugins
  }

}
