//package org.migor.feedless.repository
//
//import com.linecorp.kotlinjdsl.dsl.jpql.Jpql
//import com.linecorp.kotlinjdsl.querymodel.jpql.JpqlQueryable
//import com.linecorp.kotlinjdsl.querymodel.jpql.select.SelectQuery
//import com.linecorp.kotlinjdsl.support.spring.data.jpa.repository.KotlinJdslJpqlExecutor
//import com.netflix.graphql.dgs.DgsQueryExecutor
//import org.assertj.core.api.Assertions.assertThat
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.junit.jupiter.api.extension.ExtendWith
//import org.junit.jupiter.params.ParameterizedTest
//import org.junit.jupiter.params.provider.CsvSource
//import org.migor.feedless.AppProfiles
//import org.migor.feedless.agent.AgentService
//import org.migor.feedless.agent.AgentSyncExecutor
//import org.migor.feedless.document.DocumentService
//import org.migor.feedless.generated.DgsClient
//import org.migor.feedless.generated.types.Cursor
//import org.migor.feedless.generated.types.RepositoriesInput
//import org.migor.feedless.license.LicenseService
//import org.migor.feedless.plan.ProductDataLoader
//import org.migor.feedless.plan.ProductService
//import org.migor.feedless.session.SessionService
//import org.migor.feedless.user.UserService
//import org.mockito.Mockito
//import org.mockito.Mockito.`when`
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.context.SpringBootTest
//import org.springframework.boot.test.mock.mockito.MockBean
//import org.springframework.boot.test.mock.mockito.MockBeans
//import org.springframework.data.domain.PageImpl
//import org.springframework.data.domain.Pageable
//import org.springframework.test.context.ActiveProfiles
//import org.springframework.test.context.junit.jupiter.SpringExtension
//import org.springframework.web.socket.WebSocketHandler
//
//
//@ExtendWith(SpringExtension::class)
//@SpringBootTest
//@MockBeans(
//  value = [
////    MockBean(AgentService::class),
////    MockBean(AgentSyncExecutor::class),
////    MockBean(UserService::class),
////    MockBean(DocumentService::class),
////    MockBean(SessionService::class),
////    MockBean(LicenseService::class),
////    MockBean(ProductService::class),
////    MockBean(KotlinJdslJpqlExecutor::class),
////    MockBean(ProductDataLoader::class),
////    MockBean(WebSocketHandler::class),
//  ]
//)
//@ActiveProfiles(profiles = ["test", AppLayer.api, AppProfiles.database])
//class RepositoryResolverTest {
//
//  @MockBean
//  lateinit var repositoryDAO: RepositoryDAO
//
//  @Autowired
//  lateinit var dgsQueryExecutor: DgsQueryExecutor
//
//
//  @BeforeEach
//  fun setUp() {
//    `when`(repositoryDAO.findPage(
//      any(Pageable::class.java),
//      Mockito.argThat { lambda: (Jpql) -> JpqlQueryable<SelectQuery<RepositoryEntity>> ->
//        true
//      }
//    ))
//      .thenReturn(PageImpl(emptyList<RepositoryEntity>()))
////    runBlocking {
////
////      mockDocument = DocumentEntity()
////      mockDocument.url = documentUrl
////      mockDocument.contentText = "foo"
////      mockDocument.repositoryId = UUID.randomUUID()
////      mockDocument.status = ReleaseStatus.released
////
////      Mockito.`when`(
////        documentService.findById(
////          any(UUID::class.java),
////        )
////      ).thenReturn(mockDocument)
////    }
//  }
//
//  @Test
//  fun something() {
//    val graphQLQuery = DgsClient.buildQuery {
//      repositories(
//        data = RepositoriesInput(
//          cursor = Cursor(0),
//        )
//      ) {
//        id
//        description
//        title
//        shareKey
//        ownerId
//        product
//        visibility
//        refreshCron
//        tags
//        harvests {
//          logs
//          errornous
//          finishedAt
//          startedAt
//          itemsIgnored
//        }
//        sunset {
//          afterSnapshots
//          afterTimestamp
//        }
//        createdAt
//        lastUpdatedAt
//        nextUpdateAt
//        documentCount
//        disabledFrom
//        archived
//        pullsPerMonth
//        currentUserIsOwner
//        hasDisabledSources
////        frequency {
////          group
////          count
////        }
////        plugins {
////          pluginId
////          params {
////
////          }
////        }
////        segmented
////        retention
////        sources
//      }
//    }
//
//    val response = dgsQueryExecutor.executeAndExtractJsonPathAsObject(
//      graphQLQuery,
//      "data.scrape",
//      Map::class.java,
//    )
//    response.toString()
//  }
//
//
//  @ParameterizedTest
//  @CsvSource(
//    value = [
//      "0, 10, 0, 10",
//      "1, 10, 1, 10",
//      "-1, 10, 0, 10",
//      "-1, 100, 0, 10",
//      "-1, -1, 0, 1",
//    ]
//  )
//  fun testHandleCursor0(pageIn: Int, pageSizeIn: Int, expectedPage: Int, expectedPageSize: Int) {
//    val (pageNumber, pageSize) = handleCursor(
//      Cursor(
//        page = pageIn,
//        pageSize = pageSizeIn,
//      )
//    )
//    assertThat(pageNumber).isEqualTo(expectedPage)
//    assertThat(pageSize).isEqualTo(expectedPageSize)
//  }
//}
