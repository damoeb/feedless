package org.migor.feedless.feed

import org.junit.jupiter.api.BeforeEach

val corrId = "test"

class LegacyFeedServiceTest {

  lateinit var service: LegacyFeedService

  @BeforeEach
  fun beforeEach() {
    service = LegacyFeedService()
  }

//  @Test
//  fun `getFeed will append legacy notifications`() {
//    service.getFeed("foo")
//  }
//
//  @Test
//  fun `webToFeed will append legacy notifications`() {
//    service.webToFeed(corrId, "foo")
//  }
//
//  @Test
//  fun `transformFeed will append legacy notifications`() {
//    service.transformFeed("foo")
//  }
//
//  @Test
//  fun `getRepository will append legacy notifications`() {
//    service.getRepository("foo")
//  }
}
