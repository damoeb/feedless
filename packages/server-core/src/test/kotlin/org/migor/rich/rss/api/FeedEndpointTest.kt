package org.migor.rich.rss.api

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.rich.rss.RichRssApplication
import org.migor.rich.rss.api.http.FeedEndpoint
import org.migor.rich.rss.service.HttpService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.TestPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.util.ResourceUtils
import java.nio.file.Files

@ExtendWith(SpringExtension::class)
@ContextConfiguration(classes = [RichRssApplication::class])
@ActiveProfiles("dev", "stateless")
@TestPropertySource(locations = ["classpath:application.properties", "classpath:application-dev.properties"])
//@TestPropertySource(
//  locations = {"classpath:test.properties"},
//  properties = { "key=value" })
internal class FeedEndpointTest {

  @Autowired
  lateinit var feedEndpoint: FeedEndpoint

  @MockBean
  lateinit var httpService: HttpService

  @BeforeEach
  fun setUp() {
  }

//  @Test
  fun transformFeed() {
//    feedEndpoint.transformFeed()
    val sites = listOf(
      "after-on-rss",
      "apple-rss",
      "medium-rss",
      "yt-atom",
    )
    sites.forEach { site ->
      run {
        val feed = readFile("${site}.in.xml")
        val transformedFeed = readFile("${site}.in.xml")
        Assertions.assertEquals(feed, transformedFeed)
      }
    }
  }

  private fun readFile(filename: String): String {
    return Files.readString(ResourceUtils.getFile("classpath:raw-websites/$filename").toPath())
  }

}
