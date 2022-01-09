package org.migor.rss.rich.api

import org.migor.rss.rich.service.BucketService
import org.migor.rss.rich.util.CryptUtil.extractDigest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.core.io.InputStreamResource
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.util.ResourceUtils
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import java.util.*


@Profile(value = arrayOf("dev", "test"))
@Controller
class DebugController {

  @Autowired
  lateinit var bucketService: BucketService

  @RequestMapping("/debug/sample.pdf", method = arrayOf(RequestMethod.GET, RequestMethod.HEAD))
  fun samplePdf(): ResponseEntity<InputStreamResource> {
    val file = "sample.pdf"
    val inputStream = ResourceUtils.getFile("classpath:${file}").inputStream()
    val inputStreamResource = InputStreamResource(inputStream)
    val responseHeaders = HttpHeaders()
    responseHeaders.contentLength = 7846
    responseHeaders.contentType = MediaType.valueOf("application/pdf")
    responseHeaders["Content-Disposition"] = Collections.singletonList("attachment; filename=${file}")
    return ResponseEntity<InputStreamResource>(
      inputStreamResource,
      responseHeaders,
      HttpStatus.OK
    )
  }

  @GetMapping("/debug/atom-feed-with-digest-auth", produces = ["application/rss+xml;charset=UTF-8"])
  fun atomFeedWithDigestAuth(
    @RequestHeader("authorization", required = false, defaultValue = "") authorization: String,
    @RequestParam("page", required = false, defaultValue = "0") page: Int
  ): ResponseEntity<String> {
    val digest = extractDigest(authorization)
    Objects.requireNonNull(digest)
    return ResponseEntity.ok(
      """
      <?xml version="1.0" encoding="utf-8"?>
      <feed xmlns="http://www.w3.org/2005/Atom">

        <title>Example Feed</title>
        <link href="http://example.org/"/>
        <updated>2003-12-13T18:30:02Z</updated>
        <author>
          <name>John Doe</name>
        </author>
        <id>urn:uuid:60a76c80-d399-11d9-b93C-0003939e0af6</id>

        <entry>
          <title>entry 1</title>
          <link href="http://locahost:8080/debug/sample.pdf"/>
          <id>urn:uuid:1</id>
          <updated>2003-12-13T18:30:02Z</updated>
          <summary>Some text.</summary>
        </entry>

        <entry>
          <title>entry 2</title>
          <link href="http://locahost:8080/debug/sample.pdf"/>
          <id>urn:uuid:2</id>
          <updated>2003-12-13T18:30:02Z</updated>
          <summary>Some text.</summary>
        </entry>

      </feed>

    """.trimIndent()
    )
//    return FeedExporter.toRss(bucketService.findByBucketId(bucketId, page))
  }
}
