package org.migor.feedless.api

object ApiParams {
  const val corrId = "x-corr-id"
}

object WebToFeedParamsV2 {
  const val url = "u"
  const val linkPath = "l"
  const val extendContext = "ec"
  const val contextPath = "cp"
  const val paginationXPath = "pp"
  const val datePath = "dp"
  const val prerender = "p"
  const val prerenderWaitUntil = "aw"
  const val filter = "q"
  const val version = "v"
  const val format = "f"
  const val prerenderScript = "ps"
  const val strictMode = "sm"
  const val eventFeed = "ef"
}

object WebToFeedParamsV1 {
  const val url = "url"
  const val link = "l"
  const val extendContext = "x"
  const val contextPath = "context"
  const val datePath = "date"
  const val prerender = "p"
  const val prerenderWaitUntil = "aw"
  const val filter = "q"
  const val version = "v"
  const val format = "out"
  const val prerenderScript = "ps"
  const val debug = "debug"
  const val homepageUrl = "homepageUrl"
}


object WebToPageChangeParams {
  const val url = "u"
  const val prerender = "p"
  const val prerenderWaitUntil = "aw"
  const val version = "v"
  const val type = "t"
  const val format = "f"
  const val xpath = "x"
  const val prerenderScript = "ps"
}
