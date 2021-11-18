package org.migor.rss.rich.service

import org.apache.commons.lang3.StringUtils
import org.migor.rss.rich.util.JsonUtil
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.concurrent.TimeUnit

data class YtDlFormat(val url: String, val format: String, val ext: String)
data class YtDlResult(val title: String, val thumbnail: String, val duration: Double, val formats: List<YtDlFormat>)

@Service
class SiteService {

  private val log = LoggerFactory.getLogger(SiteService::class.simpleName)

  fun detectEnclosures(url: String, timeoutSec: Long = 60): YtDlResult {
    val json = runCatching {
      // runuser -l  userNameHere -c '/path/to/command arg1 arg2'
      ProcessBuilder("\\s".toRegex().split("youtube-dl -j $url"))
        .redirectOutput(ProcessBuilder.Redirect.PIPE)
        .start().also { it.waitFor(timeoutSec, TimeUnit.SECONDS) }
        .inputStream.bufferedReader().readText()
    }.onFailure { it.printStackTrace() }.getOrNull()

    return if (StringUtils.isBlank(json)) {
      throw RuntimeException("")
    } else {
      JsonUtil.gson.fromJson(json, YtDlResult::class.java)
    }
  }
}
