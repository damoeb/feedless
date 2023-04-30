package org.migor.rich.rss.api

object ApiParams {
  const val corrId = "x-corr-id"
  const val nonce = "nonce"
}

object WebToFeedParams {
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
  const val articleRecovery = "ar"
  const val prerenderScript = "ps"
  const val strictMode = "sm"
  const val eventFeed = "ef"
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
