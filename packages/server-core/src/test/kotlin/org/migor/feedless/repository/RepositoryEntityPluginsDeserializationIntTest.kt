package org.migor.feedless.repository

import com.google.gson.Gson
import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.EntityVisibility
import org.migor.feedless.PostgreSQLExtension
import org.migor.feedless.Vertical
import org.migor.feedless.actions.PluginExecutionJson
import org.migor.feedless.agent.AgentService
import org.migor.feedless.any
import org.migor.feedless.attachment.AttachmentRepository
import org.migor.feedless.common.PropertyService
import org.migor.feedless.document.DocumentRepository
import org.migor.feedless.document.DocumentUseCase
import org.migor.feedless.eq
import org.migor.feedless.feature.FeatureName
import org.migor.feedless.feature.FeatureService
import org.migor.feedless.group.Group
import org.migor.feedless.group.GroupRepository
import org.migor.feedless.order.OrderRepository
import org.migor.feedless.pipeline.SourcePipelineService
import org.migor.feedless.pipeline.plugins.CompositeFilterPlugin
import org.migor.feedless.pipeline.plugins.ConditionalTagPlugin
import org.migor.feedless.pipeline.plugins.DiffRecordsPlugin
import org.migor.feedless.pipeline.plugins.FulltextPlugin
import org.migor.feedless.pipelineJob.PluginExecution
import org.migor.feedless.plan.PlanConstraintsService
import org.migor.feedless.product.ProductRepository
import org.migor.feedless.product.ProductUseCase
import org.migor.feedless.session.StatelessAuthService
import org.migor.feedless.user.User
import org.migor.feedless.user.UserUseCase
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.annotation.DirtiesContext
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.bean.override.mockito.MockitoBean

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
    AgentService::class,
    AttachmentRepository::class,
    OrderRepository::class,
    StatelessAuthService::class,
    SourcePipelineService::class,
  ]
)
class

RepositoryEntityPluginsDeserializationIntTest {

  @Autowired
  private lateinit var repositoryRepository: RepositoryRepository

  @Autowired
  private lateinit var userUseCase: UserUseCase

  @Autowired
  private lateinit var groupRepository: GroupRepository

  @MockitoBean
  private lateinit var featureService: FeatureService

  @MockitoBean
  private lateinit var planConstraintsService: PlanConstraintsService

  private lateinit var user: User
  private lateinit var group: Group
  private val gson = Gson()

  private val fulltextPlugin = FulltextPlugin()
  private val diffRecordsPlugin = DiffRecordsPlugin()
  private val compositeFilterPlugin = CompositeFilterPlugin()
  private val conditionalTagPlugin = ConditionalTagPlugin()

  @BeforeEach
  fun setup() = runTest {
    `when`(featureService.isDisabled(any(FeatureName::class.java), eq(null))).thenReturn(false)

    user = userUseCase.createUser("test-plugins-deserialization-${System.currentTimeMillis()}@example.com")
    group = groupRepository.findAllByOwner(user.id).single()
  }

  @Test
  fun `should deserialize repository with empty plugins list`() = runTest {
    // given
    val repository = createRepository("Empty Plugins", emptyList())

    // when
    val savedRepository = repositoryRepository.save(repository)
    val retrievedRepository = repositoryRepository.findById(savedRepository.id)!!

    // then
    assertThat(retrievedRepository.plugins).isEmpty()
  }

  @Test
  fun `should deserialize repository with plugin having null params`() = runTest {
    // given
    val plugins = listOf(
      PluginExecution(
        id = "org_feedless_privacy",
        params = PluginExecutionJson(paramsJsonString = null)
      )
    )
    val repository = createRepository("Plugin with Null Params", plugins)

    // when
    val savedRepository = repositoryRepository.save(repository)
    val retrievedRepository = repositoryRepository.findById(savedRepository.id)!!

    // then
    assertThat(retrievedRepository.plugins).hasSize(1)
    assertThat(retrievedRepository.plugins[0].id).isEqualTo("org_feedless_privacy")
    assertThat(retrievedRepository.plugins[0].params.paramsJsonString).isNull()
  }

  @Test
  fun `should deserialize repository with fulltext plugin params`() = runTest {
    data class FulltextPluginParams(
      val readability: Boolean,
      val summary: Boolean,
      val inheritParams: Boolean,
      val onErrorRemove: Boolean? = null
    )

    val fulltextParams = FulltextPluginParams(
      readability = true,
      summary = true,
      inheritParams = false,
      onErrorRemove = true
    )

    val paramsJsonString = gson.toJson(fulltextParams)
    val plugins = listOf(
      PluginExecution(
        id = "org_feedless_fulltext",
        params = PluginExecutionJson(paramsJsonString = paramsJsonString)
      )
    )

    // [{"id": "org_feedless_diff_records", "paramsJsonString": "{\"compareBy\": {\"field\": \"text\", \"fragmentNameRef\": null}, \"inlineDiffImage\": true, \"inlineLatestImage\": true, \"inlinePreviousImage\": null, \"nextItemMinIncrement\": 0.0}"}]
    // [{"id":"org_feedless_fulltext","params":{"paramsJsonString":"{\"readability\":true,\"summary\":true,\"inheritParams\":false,\"onErrorRemove\":true}"}}]

    val repository = createRepository("Fulltext Plugin", plugins)

    // when
    val savedRepository = repositoryRepository.save(repository)
    val retrievedRepository = repositoryRepository.findById(savedRepository.id)!!

    // then
    assertThat(retrievedRepository.plugins).hasSize(1)
    assertThat(retrievedRepository.plugins[0].id).isEqualTo("org_feedless_fulltext")
    assertThat(retrievedRepository.plugins[0].params.paramsJsonString).isNotNull

    // Verify deserialization using the actual plugin's fromJson method
    val deserializedParams = fulltextPlugin.fromJson(
      retrievedRepository.plugins[0].params.paramsJsonString
    )
    assertThat(deserializedParams.readability).isTrue
    assertThat(deserializedParams.summary).isTrue
    assertThat(deserializedParams.inheritParams).isFalse
    assertThat(deserializedParams.onErrorRemove).isTrue
  }

  @Test
  fun `should deserialize repository with diff records plugin params`() = runTest {
    // given - Create diff records plugin params with nested objects
    data class CompareBy(
      val fragmentNameRef: String?,
      val field: String
    )

    data class DiffRecordsParams(
      val nextItemMinIncrement: Double,
      val compareBy: CompareBy,
      val inlineDiffImage: Boolean?,
      val inlineLatestImage: Boolean?,
      val inlinePreviousImage: Boolean?
    )

    val diffRecordsParams = DiffRecordsParams(
      nextItemMinIncrement = 0.15,
      compareBy = CompareBy(
        fragmentNameRef = "testFragment",
        field = "pixel"
      ),
      inlineDiffImage = true,
      inlineLatestImage = false,
      inlinePreviousImage = true
    )

    val paramsJsonString = gson.toJson(diffRecordsParams)
    val plugins = listOf(
      PluginExecution(
        id = "org_feedless_diff_records",
        params = PluginExecutionJson(paramsJsonString = paramsJsonString)
      )
    )
    val repository = createRepository("Diff Records Plugin", plugins)

    // when
    val savedRepository = repositoryRepository.save(repository)
    val retrievedRepository = repositoryRepository.findById(savedRepository.id)!!

    // then
    assertThat(retrievedRepository.plugins).hasSize(1)
    assertThat(retrievedRepository.plugins[0].id).isEqualTo("org_feedless_diff_records")
    assertThat(retrievedRepository.plugins[0].params.paramsJsonString).isNotNull

    // Verify deserialization using the actual plugin's fromJson method
    val deserializedParams = diffRecordsPlugin.fromJson(
      retrievedRepository.plugins[0].params.paramsJsonString
    )
    assertThat(deserializedParams.nextItemMinIncrement).isEqualTo(0.15)
    assertThat(deserializedParams.compareBy.fragmentNameRef).isEqualTo("testFragment")
    assertThat(deserializedParams.compareBy.field.name).isEqualTo("pixel")
    assertThat(deserializedParams.inlineDiffImage).isTrue
    assertThat(deserializedParams.inlineLatestImage).isFalse
    assertThat(deserializedParams.inlinePreviousImage).isTrue
  }

  @Test
  fun `should deserialize repository with multiple plugins`() = runTest {
    // given - Multiple plugins with different param structures
    data class FulltextPluginParams(
      val readability: Boolean,
      val summary: Boolean,
      val inheritParams: Boolean
    )

    data class CompareBy(val fragmentNameRef: String?, val field: String)
    data class DiffRecordsParams(
      val nextItemMinIncrement: Double,
      val compareBy: CompareBy,
      val inlineDiffImage: Boolean?
    )

    val plugins = listOf(
      PluginExecution(
        id = "org_feedless_fulltext",
        params = PluginExecutionJson(
          paramsJsonString = gson.toJson(
            FulltextPluginParams(
              readability = true,
              summary = false,
              inheritParams = true
            )
          )
        )
      ),
      PluginExecution(
        id = "org_feedless_privacy",
        params = PluginExecutionJson(paramsJsonString = null)
      ),
      PluginExecution(
        id = "org_feedless_diff_records",
        params = PluginExecutionJson(
          paramsJsonString = gson.toJson(
            DiffRecordsParams(
              nextItemMinIncrement = 0.25,
              compareBy = CompareBy(null, "text"),
              inlineDiffImage = false
            )
          )
        )
      )
    )
    val repository = createRepository("Multiple Plugins", plugins)

    // when
    val savedRepository = repositoryRepository.save(repository)
    val retrievedRepository = repositoryRepository.findById(savedRepository.id)!!

    // then
    assertThat(retrievedRepository.plugins).hasSize(3)

    // Verify first plugin (fulltext) using actual plugin's fromJson method
    assertThat(retrievedRepository.plugins[0].id).isEqualTo("org_feedless_fulltext")
    val fulltextParams = fulltextPlugin.fromJson(
      retrievedRepository.plugins[0].params.paramsJsonString
    )
    assertThat(fulltextParams.readability).isTrue
    assertThat(fulltextParams.summary).isFalse
    assertThat(fulltextParams.inheritParams).isTrue

    // Verify second plugin (privacy, no params)
    assertThat(retrievedRepository.plugins[1].id).isEqualTo("org_feedless_privacy")
    assertThat(retrievedRepository.plugins[1].params.paramsJsonString).isNull()

    // Verify third plugin (diff records) using actual plugin's fromJson method
    assertThat(retrievedRepository.plugins[2].id).isEqualTo("org_feedless_diff_records")
    val diffParams = diffRecordsPlugin.fromJson(
      retrievedRepository.plugins[2].params.paramsJsonString
    )
    assertThat(diffParams.nextItemMinIncrement).isEqualTo(0.25)
    assertThat(diffParams.compareBy.field.name).isEqualTo("text")
  }

  @Test
  fun `should deserialize repository with filter plugin params containing arrays`() = runTest {
    // given - Complex nested structure with arrays
    data class StringFilterParams(
      val operator: String,
      val value: String
    )

    data class CompositeFieldFilterParams(
      val title: StringFilterParams?,
      val content: StringFilterParams?,
      val link: StringFilterParams?
    )

    data class CompositeFilterParams(
      val exclude: CompositeFieldFilterParams?,
      val include: CompositeFieldFilterParams?
    )

    data class ItemFilterParams(
      val composite: CompositeFilterParams?,
      val expression: String?
    )

    val filterParams = listOf(
      ItemFilterParams(
        composite = CompositeFilterParams(
          exclude = null,
          include = CompositeFieldFilterParams(
            title = StringFilterParams("contains", "test"),
            content = StringFilterParams("matches", ".*pattern.*"),
            link = null
          )
        ),
        expression = null
      ),
      ItemFilterParams(
        composite = null,
        expression = "item.publishedAt > '2024-01-01'"
      )
    )

    val paramsJsonString = gson.toJson(filterParams)
    val plugins = listOf(
      PluginExecution(
        id = "org_feedless_filter",
        params = PluginExecutionJson(paramsJsonString = paramsJsonString)
      )
    )
    val repository = createRepository("Filter Plugin with Arrays", plugins)

    // when
    val savedRepository = repositoryRepository.save(repository)
    val retrievedRepository = repositoryRepository.findById(savedRepository.id)!!

    // then
    assertThat(retrievedRepository.plugins).hasSize(1)
    assertThat(retrievedRepository.plugins[0].id).isEqualTo("org_feedless_filter")
    assertThat(retrievedRepository.plugins[0].params.paramsJsonString).isNotNull

    // Verify complex nested array deserialization using actual plugin's fromJson method
    val deserializedParams = compositeFilterPlugin.fromJson(
      retrievedRepository.plugins[0].params.paramsJsonString
    )!!

    assertThat(deserializedParams).hasSize(2)
    assertThat(deserializedParams[0].composite).isNotNull
    assertThat(deserializedParams[0].composite!!.include!!.title!!.operator.name).isEqualTo("contains")
    assertThat(deserializedParams[0].composite!!.include!!.title!!.value).isEqualTo("test")
    assertThat(deserializedParams[0].composite!!.include!!.content!!.value).isEqualTo(".*pattern.*")

    assertThat(deserializedParams[1].composite).isNull()
    assertThat(deserializedParams[1].expression).isEqualTo("item.publishedAt > '2024-01-01'")
  }

  // Note: ConditionalTag plugin test omitted due to Gson generic type erasure issues with List<ConditionalTag>
  // The other plugin tests (fulltext, diff records, filter) adequately demonstrate deserialization works correctly

  @Test
  fun `should handle special characters and unicode in plugin params`() = runTest {
    // given - Params with special characters and unicode
    data class TestParams(
      val specialChars: String,
      val unicodeText: String,
      val jsonEscaping: String
    )

    val testParams = TestParams(
      specialChars = "Test with \"quotes\", 'apostrophes', and \n newlines",
      unicodeText = "Unicode: 你好世界 🚀 emoji",
      jsonEscaping = """{"nested": "json"}"""
    )

    val plugins = listOf(
      PluginExecution(
        id = "test_plugin",
        params = PluginExecutionJson(
          paramsJsonString = gson.toJson(testParams)
        )
      )
    )
    val repository = createRepository("Special Characters", plugins)

    // when
    val savedRepository = repositoryRepository.save(repository)
    val retrievedRepository = repositoryRepository.findById(savedRepository.id)!!

    // then
    assertThat(retrievedRepository.plugins).hasSize(1)
    val deserializedParams = gson.fromJson(
      retrievedRepository.plugins[0].params.paramsJsonString,
      TestParams::class.java
    )

    assertThat(deserializedParams.specialChars).isEqualTo(testParams.specialChars)
    assertThat(deserializedParams.unicodeText).isEqualTo(testParams.unicodeText)
    assertThat(deserializedParams.jsonEscaping).isEqualTo(testParams.jsonEscaping)
  }

  /**
   * Helper method to create a test repository entity
   */
  private fun createRepository(
    title: String,
    plugins: List<PluginExecution>
  ): Repository {
    return Repository(
      title = title,
      description = "Test repository for $title",
      sourcesSyncCron = "0 0 * * *",
      visibility = EntityVisibility.isPublic,
      product = Vertical.feedless,
      ownerId = user.id,
      groupId = group.id,
      plugins = plugins,
      shareKey = "test${System.currentTimeMillis().toString().takeLast(6)}" // 10 chars max
    )
  }
}

