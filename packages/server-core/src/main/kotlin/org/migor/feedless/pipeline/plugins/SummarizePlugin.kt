package org.migor.feedless.pipeline.plugins

import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.document.Document
import org.migor.feedless.generated.types.FeedlessPlugins
import org.migor.feedless.llm.LlmService
import org.migor.feedless.pipeline.MapEntityPlugin
import org.migor.feedless.repository.Repository
import org.migor.feedless.scrape.LogCollector
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("${AppProfiles.scrape} & ${AppLayer.service}")
class SummarizePlugin(private val llmService: LlmService) : MapEntityPlugin<Void> {

  private val log = LoggerFactory.getLogger(FulltextPlugin::class.simpleName)

  override fun id(): String = FeedlessPlugins.org_feedless_fulltext.name
  override fun name(): String = "Fulltext & Readability"
  override fun listed() = true

  override suspend fun mapEntity(
    document: Document,
    repository: Repository,
    params: Void,
    logCollector: LogCollector
  ): Document {
    logCollector.log("mapEntity ${document.url}")

    llmService.prompt(
      """
Extract the event title, start date, end date, and summary from the text.
The output must always be valid JSON with the following keys:
"title" (string, without any dates)
"startDatetime" (string, ISO 8601 format, optional)
"endDatetime" (string, ISO 8601 format, optional)
"summary" (string)
Use the language of the raw text for the "title" and "summary".
Return ONLY the JSON object, no extra text.

Raw text:

    """.trimIndent()
    )

  }

  override suspend fun mapEntity(
    document: Document,
    repository: Repository,
    paramsJson: String?,
    logCollector: LogCollector
  ): Document {
    return mapEntity(document, repository, fromJson(paramsJson), logCollector)
  }

  override suspend fun fromJson(jsonParams: String?): Void {

  }

}
