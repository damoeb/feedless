package org.migor.feedless.source

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.PageableRequest
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.actions.FetchAction
import org.migor.feedless.group.Group
import org.migor.feedless.group.GroupRepository
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryId
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.session.StatelessAuthService
import org.migor.feedless.user.User
import org.migor.feedless.user.UserRepository
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignment
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.testcontainers.junit.jupiter.Testcontainers
import java.time.LocalDateTime

/**
 * Covers T6: [SourcesFilter.minErrorsInSuccession] on the per-repository list, and the
 * cross-repository query behind `GET /user/sources` — the access predicate (owner, or member of
 * the owning group, any role; a stranger's public repository excluded) and its fixed ordering.
 */
@SpringBootTest
@ExtendWith(PostgreSQLExtension::class)
@DirtiesContext
@ActiveProfiles(
  "test",
  "database",
  AppProfiles.repository,
  AppProfiles.source,
  AppProfiles.user,
  AppLayer.repository,
)
@MockitoBean(
  types = [
    StatelessAuthService::class,
  ]
)
@Testcontainers
class SourceRepositoryIntTest {

  @Autowired
  private lateinit var sourceRepository: SourceRepository

  @Autowired
  private lateinit var repositoryRepository: RepositoryRepository

  @Autowired
  private lateinit var userRepository: UserRepository

  @Autowired
  private lateinit var groupRepository: GroupRepository

  @Autowired
  private lateinit var userGroupAssignmentRepository: UserGroupAssignmentRepository

  private lateinit var owner: User
  private lateinit var member: User
  private lateinit var stranger: User
  private lateinit var ownRepo: Repository

  @BeforeEach
  fun setUp() {
    userRepository.deleteAll()

    val suffix = System.currentTimeMillis()
    owner = userRepository.save(User(email = "owner-$suffix@test.com", lastLogin = LocalDateTime.now()))
    member = userRepository.save(User(email = "member-$suffix@test.com", lastLogin = LocalDateTime.now()))
    stranger = userRepository.save(User(email = "stranger-$suffix@test.com", lastLogin = LocalDateTime.now()))

    val group = groupRepository.save(Group(name = "group-$suffix", ownerId = owner.id))
    userGroupAssignmentRepository.save(
      UserGroupAssignment(role = RoleInGroup.viewer, userId = member.id, groupId = group.id),
    )

    ownRepo = repositoryRepository.save(
      Repository(
        title = "own repo",
        ownerId = owner.id,
        groupId = group.id,
        visibility = EntityVisibility.isPrivate,
      ),
    )
  }

  private fun createSource(
    repositoryId: RepositoryId,
    title: String = "source-${SourceId().uuid}",
    errorsInSuccession: Int = 0,
    lastRefreshedAt: LocalDateTime? = null,
    disabled: Boolean = false,
  ): Source {
    val id = SourceId()
    return sourceRepository.save(
      Source(
        id = id,
        title = title,
        repositoryId = repositoryId,
        errorsInSuccession = errorsInSuccession,
        lastRefreshedAt = lastRefreshedAt,
        disabled = disabled,
        actions = listOf(FetchAction(sourceId = id, pos = 0, url = "https://example.com/$title")),
      ),
    )
  }

  @Test
  fun `findAllByRepositoryIdFiltered filters by minErrorsInSuccession`() {
    val none = createSource(ownRepo.id, errorsInSuccession = 0)
    val one = createSource(ownRepo.id, errorsInSuccession = 1)
    val three = createSource(ownRepo.id, errorsInSuccession = 3)

    val unfiltered = sourceRepository.findAllByRepositoryIdFiltered(ownRepo.id, PageableRequest(0, 10))
    assertThat(unfiltered.map { it.id }).containsExactlyInAnyOrder(none.id, one.id, three.id)

    val atLeastOne = sourceRepository.findAllByRepositoryIdFiltered(
      ownRepo.id,
      PageableRequest(0, 10),
      SourcesFilter(minErrorsInSuccession = 1),
    )
    assertThat(atLeastOne.map { it.id }).containsExactlyInAnyOrder(one.id, three.id)

    val atLeastThree = sourceRepository.findAllByRepositoryIdFiltered(
      ownRepo.id,
      PageableRequest(0, 10),
      SourcesFilter(minErrorsInSuccession = 3),
    )
    assertThat(atLeastThree.map { it.id }).containsExactly(three.id)
  }

  @Test
  fun `findAllForUser returns sources of owned and group repositories, ordered by errorsInSuccession then lastRefreshedAt desc`() {
    val now = LocalDateTime.now().withNano(0)
    // Tied errorsInSuccession: lastRefreshedAt desc breaks the tie.
    val s1 = createSource(ownRepo.id, errorsInSuccession = 5, lastRefreshedAt = now.minusHours(1))
    val s2 = createSource(ownRepo.id, errorsInSuccession = 5, lastRefreshedAt = now.minusHours(2))
    val s3 = createSource(ownRepo.id, errorsInSuccession = 1, lastRefreshedAt = now.minusHours(3))

    // A stranger's private repository must never leak in.
    val strangerGroup = groupRepository.save(Group(name = "stranger-group-${now}", ownerId = stranger.id))
    val strangerPrivateRepo = repositoryRepository.save(
      Repository(
        title = "stranger-private",
        ownerId = stranger.id,
        groupId = strangerGroup.id,
        visibility = EntityVisibility.isPrivate,
      ),
    )
    createSource(strangerPrivateRepo.id, errorsInSuccession = 100)

    // Nor a stranger's PUBLIC repository — this is what distinguishes /user/sources from a plain
    // per-repository access check, which would allow a public repository to any caller.
    val strangerPublicRepo = repositoryRepository.save(
      Repository(
        title = "stranger-public",
        ownerId = stranger.id,
        groupId = strangerGroup.id,
        visibility = EntityVisibility.isPublic,
      ),
    )
    createSource(strangerPublicRepo.id, errorsInSuccession = 100)

    val asOwner = sourceRepository.findAllForUser(owner.id, emptyList(), PageableRequest(0, 10))
    assertThat(asOwner.map { it.id }).containsExactly(s1.id, s2.id, s3.id)

    val asMember = sourceRepository.findAllForUser(member.id, listOf(ownRepo.groupId), PageableRequest(0, 10))
    assertThat(asMember.map { it.id }).containsExactly(s1.id, s2.id, s3.id)
  }

  @Test
  fun `findAllForUser excludes a stranger with no owned or joined repositories`() {
    createSource(ownRepo.id, errorsInSuccession = 5)

    val result = sourceRepository.findAllForUser(stranger.id, emptyList(), PageableRequest(0, 10))

    assertThat(result).isEmpty()
  }

  @Test
  fun `findAllForUser filters by minErrorsInSuccession`() {
    val low = createSource(ownRepo.id, errorsInSuccession = 0)
    val high = createSource(ownRepo.id, errorsInSuccession = 2)

    val result = sourceRepository.findAllForUser(
      owner.id,
      emptyList(),
      PageableRequest(0, 10),
      SourcesFilter(minErrorsInSuccession = 1),
    )

    assertThat(result.map { it.id }).containsExactly(high.id)
    assertThat(result.map { it.id }).doesNotContain(low.id)
  }

  @Test
  fun `findAllForUser paginates correctly`() {
    val now = LocalDateTime.now().withNano(0)
    val s1 = createSource(ownRepo.id, errorsInSuccession = 3, lastRefreshedAt = now.minusMinutes(1))
    val s2 = createSource(ownRepo.id, errorsInSuccession = 2, lastRefreshedAt = now.minusMinutes(2))
    val s3 = createSource(ownRepo.id, errorsInSuccession = 1, lastRefreshedAt = now.minusMinutes(3))

    val page0 = sourceRepository.findAllForUser(owner.id, emptyList(), PageableRequest(0, 2))
    val page1 = sourceRepository.findAllForUser(owner.id, emptyList(), PageableRequest(1, 2))

    assertThat(page0.map { it.id }).containsExactly(s1.id, s2.id)
    assertThat(page1.map { it.id }).containsExactly(s3.id)
  }

  @Test
  fun `findAllForUser pages without skipping or repeating rows, mirroring listUserSources' ask-for-one-extra pattern`() {
    // Distinct errorsInSuccession fully determines order on its own, independent of the
    // createdAt/id tiebreakers — isolates this test to the offset/limit bug (T6 review round 1).
    val now = LocalDateTime.now().withNano(0)
    val sources = (5 downTo 1).map { n -> createSource(ownRepo.id, errorsInSuccession = n, lastRefreshedAt = now) }

    // Mirrors SourceHttpController.listUserSources: ask for one more than the page holds.
    val page0 = sourceRepository.findAllForUser(owner.id, emptyList(), PageableRequest.withExtraForHasMore(0, 2))
    val page1 = sourceRepository.findAllForUser(owner.id, emptyList(), PageableRequest.withExtraForHasMore(1, 2))
    val page2 = sourceRepository.findAllForUser(owner.id, emptyList(), PageableRequest.withExtraForHasMore(2, 2))

    // What the controller would actually return to the caller from each page (fetched.take(pageSize)).
    val returned = page0.take(2) + page1.take(2) + page2.take(2)
    assertThat(returned.map { it.id }).containsExactlyElementsOf(sources.map { it.id })

    // hasMore = fetched.size > pageSize on each page.
    assertThat(page0).hasSize(3)
    assertThat(page1).hasSize(3)
    assertThat(page2).hasSize(1)
  }

  @Test
  fun `findAllByRepositoryIdFiltered pages without skipping or repeating rows, mirroring listSources' ask-for-one-extra pattern`() {
    val sources = (1..5).map { n -> createSource(ownRepo.id, title = "walk-$n") }

    // Mirrors SourceHttpController.listSources: ask for one more than the page holds.
    val page0 = sourceRepository.findAllByRepositoryIdFiltered(ownRepo.id, PageableRequest.withExtraForHasMore(0, 2))
    val page1 = sourceRepository.findAllByRepositoryIdFiltered(ownRepo.id, PageableRequest.withExtraForHasMore(1, 2))
    val page2 = sourceRepository.findAllByRepositoryIdFiltered(ownRepo.id, PageableRequest.withExtraForHasMore(2, 2))

    val returned = page0.take(2) + page1.take(2) + page2.take(2)
    assertThat(returned.map { it.id }).containsExactlyElementsOf(sources.reversed().map { it.id })

    assertThat(page0).hasSize(3)
    assertThat(page1).hasSize(3)
    assertThat(page2).hasSize(1)
  }
}
