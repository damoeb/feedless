package org.migor.feedless.group

import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatExceptionOfType
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.ConflictException
import org.migor.feedless.EntityVisibility
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.agent.AgentService
import org.migor.feedless.any
import org.migor.feedless.any2
import org.migor.feedless.capability.RequestContext
import org.migor.feedless.common.PropertyService
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.document.DocumentUseCase
import org.migor.feedless.eq
import org.migor.feedless.feature.FeatureName
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.order.OrderRepository
import org.migor.feedless.pipeline.SourcePipelineService
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.product.ProductRepository
import org.migor.feedless.product.ProductUseCase
import org.migor.feedless.repository.InboxService
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryRepository
import org.migor.feedless.session.StatelessAuthService
import org.migor.feedless.user.User
import org.migor.feedless.user.UserRepository
import org.migor.feedless.user.UserUseCase
import org.migor.feedless.userGroup.RoleInGroup
import org.migor.feedless.userGroup.UserGroupAssignmentRepository
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean
import java.time.LocalDateTime
import java.util.UUID

/** No call may take a user's last owned group away (no login or token then). Runs against Postgres for real transactions. */
@SpringBootTest
@ExtendWith(PostgreSQLExtension::class)
@DirtiesContext
@ActiveProfiles(
  "test",
  "database",
  AppProfiles.repository,
  AppProfiles.source,
  AppProfiles.user,
  AppProfiles.scrape,
  AppLayer.repository,
  AppLayer.service,
)
@MockitoBean(
  types = [
    ProductRepository::class,
    DocumentRepository::class,
    DocumentUseCase::class,
    ProductUseCase::class,
    PropertyService::class,
    InboxService::class,
    StatelessAuthService::class,
    OrderRepository::class,
    AgentService::class,
    SourcePipelineService::class,
    PlanConstraintsService::class,
  ]
)
class GroupUseCaseIntTest {

  @Autowired
  private lateinit var groupUseCase: GroupUseCase

  @Autowired
  private lateinit var userUseCase: UserUseCase

  @Autowired
  private lateinit var userRepository: UserRepository

  @Autowired
  private lateinit var userGroupAssignmentRepository: UserGroupAssignmentRepository

  @Autowired
  private lateinit var repositoryRepository: RepositoryRepository

  @MockitoSpyBean
  private lateinit var groupRepository: GroupRepository

  @MockitoBean
  private lateinit var featureService: FeatureService

  /** Owns its default group (with the inbox repository) and [group]. */
  private lateinit var owner: User

  /** A second group of [owner]'s without repositories: the one each test deletes or edits. */
  private lateinit var group: Group

  @BeforeEach
  fun setUp() = runBlocking {
    `when`(featureService.isDisabled(any(FeatureName::class.java), eq(null))).thenReturn(false)
    // Data is not reset between tests (shared Testcontainers Postgres), so every test seeds its own users.
    owner = userUseCase.createUser("group-owner+${UUID.randomUUID()}@feedless.test")
    group = asOwner { groupUseCase.create("team ${UUID.randomUUID()}") }
  }

  @Test
  fun `delete removes the group and its assignments`() {
    val member = userUseCase.createUserBlocking()
    asOwner { groupUseCase.addUserToGroup(member.id, group.id, RoleInGroup.viewer) }

    asOwner { groupUseCase.delete(group.id) }

    assertThat(groupRepository.findById(group.id)).isNull()
    assertThat(userGroupAssignmentRepository.findAllByGroupId(group.id)).isEmpty()
    assertThat(userGroupAssignmentRepository.findAllByUserId(owner.id).map { it.role }).containsExactly(RoleInGroup.owner)
  }

  @Test
  fun `delete is refused while the group owns repositories, and changes nothing`() {
    repositoryRepository.save(
      Repository(
        title = "repository of ${group.id.uuid}",
        visibility = EntityVisibility.isPrivate,
        ownerId = owner.id,
        groupId = group.id,
      ),
    )

    assertConflict("still owns repositories") { groupUseCase.delete(group.id) }

    assertThat(groupRepository.findById(group.id)).isNotNull()
    assertThat(userGroupAssignmentRepository.findAllByGroupId(group.id)).hasSize(1)
  }

  @Test
  fun `delete is refused when the group is a member's only owned group, and changes nothing`() {
    val memberWithoutGroup = userWithoutGroup()
    asOwner { groupUseCase.addUserToGroup(memberWithoutGroup.id, group.id, RoleInGroup.owner) }

    assertConflict("lock that member out") { groupUseCase.delete(group.id) }

    assertThat(groupRepository.findById(group.id)).isNotNull()
    assertThat(userGroupAssignmentRepository.findAllByGroupId(group.id).map { it.userId })
      .containsExactlyInAnyOrder(owner.id, memberWithoutGroup.id)
  }

  @Test
  fun `delete is one transaction - a failure after the assignments were deleted leaves them intact`() {
    val member = userUseCase.createUserBlocking()
    asOwner { groupUseCase.addUserToGroup(member.id, group.id, RoleInGroup.viewer) }
    doThrow(IllegalStateException("simulated failure")).`when`(groupRepository).delete(any2())

    assertThatExceptionOfType(IllegalStateException::class.java)
      .isThrownBy { asOwner { groupUseCase.delete(group.id) } }

    assertThat(groupRepository.findById(group.id)).isNotNull()
    assertThat(userGroupAssignmentRepository.findAllByGroupId(group.id).map { it.userId })
      .containsExactlyInAnyOrder(owner.id, member.id)
  }

  @Test
  fun `removing the group's last owner is refused`() {
    assertConflict("last owner") { groupUseCase.removeUserFromGroup(group.id, owner.id) }

    assertThat(userGroupAssignmentRepository.findByUserIdAndGroupId(owner.id, group.id)).isNotNull()
  }

  @Test
  fun `removing an owner from the only group they own is refused`() {
    val memberWithoutGroup = userWithoutGroup()
    asOwner { groupUseCase.addUserToGroup(memberWithoutGroup.id, group.id, RoleInGroup.owner) }

    assertConflict("only group the user owns") { groupUseCase.removeUserFromGroup(group.id, memberWithoutGroup.id) }

    assertThat(userGroupAssignmentRepository.findByUserIdAndGroupId(memberWithoutGroup.id, group.id)).isNotNull()
  }

  @Test
  fun `removing an owner who owns another group succeeds`() {
    val coOwner = userUseCase.createUserBlocking()
    asOwner { groupUseCase.addUserToGroup(coOwner.id, group.id, RoleInGroup.owner) }

    asOwner { groupUseCase.removeUserFromGroup(group.id, coOwner.id) }

    assertThat(userGroupAssignmentRepository.findByUserIdAndGroupId(coOwner.id, group.id)).isNull()
    assertThat(userGroupAssignmentRepository.findByUserIdAndGroupId(owner.id, group.id)).isNotNull()
  }

  @Test
  fun `removing a viewer succeeds`() {
    val member = userWithoutGroup()
    asOwner { groupUseCase.addUserToGroup(member.id, group.id, RoleInGroup.viewer) }

    asOwner { groupUseCase.removeUserFromGroup(group.id, member.id) }

    assertThat(userGroupAssignmentRepository.findByUserIdAndGroupId(member.id, group.id)).isNull()
  }

  private fun <T> asOwner(block: suspend () -> T): T =
    runBlocking(RequestContext(userId = owner.id)) { block() }

  private fun assertConflict(messagePart: String, block: suspend () -> Unit) {
    assertThatExceptionOfType(ConflictException::class.java)
      .isThrownBy { asOwner(block) }
      .withMessageContaining(messagePart)
  }

  /** A user who owns no group at all — such a user cannot log in, which is what the rules prevent. */
  private fun userWithoutGroup(): User = userRepository.save(
    User(email = "no-group+${UUID.randomUUID()}@feedless.test", lastLogin = LocalDateTime.now()),
  )

  private fun UserUseCase.createUserBlocking(): User =
    runBlocking { createUser("group-member+${UUID.randomUUID()}@feedless.test") }
}
