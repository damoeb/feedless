package org.migor.rss.rich.harvest

class XmlContent: ContentStrategy {
  override fun canProcess(harvestResponse: HarvestResponse): Boolean {
    val contentType = harvestResponse.contentType!!.split(";")[0]
    return contentType.contains("xml")
  }

  override fun process(harvestResponse: HarvestResponse): HarvestContent {
    // todo fix xml errors
    // parse
    // convert to json structure
    return HarvestContent()
  }

}
