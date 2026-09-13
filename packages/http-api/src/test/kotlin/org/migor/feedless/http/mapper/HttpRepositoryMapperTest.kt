package org.migor.feedless.http.mapper

import org.junit.jupiter.api.Test
import org.migor.feedless.EntityVisibility
import org.migor.feedless.Vertical
import org.migor.feedless.group.GroupId
import org.migor.feedless.repository.Repository
import org.migor.feedless.user.UserId

class HttpRepositoryMapperTest {

  private val mapper = HttpRepositoryMapper(HttpScrapeFlowMapper())
  private val repository = Repository(
    title = "feed",
    visibility = EntityVisibility.isPrivate,
    shareKey = "key-1",
    ownerId = UserId(),
    groupId = GroupId(),
    product = Vertical.rssProxy,
  )

  @Test
  fun `the owner of a private repository gets its share key`() {
    assert(mapper.toHttp(repository, currentUserIsOwner = true).shareKey == "key-1")
  }

  @Test
  fun `the owner of a public repository gets no share key`() {
    val public = repository.copy(visibility = EntityVisibility.isPublic)
    assert(mapper.toHttp(public, currentUserIsOwner = true).shareKey == null)
  }

  @Test
  fun `a non-owner gets no share key`() {
    assert(mapper.toHttp(repository, currentUserIsOwner = false).shareKey == null)
  }
}
