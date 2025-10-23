package org.migor.feedless.attachment

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.graphql.dgs.client.MonoGraphQLClient
import com.netflix.graphql.dgs.client.WebClientGraphQLClient
import kotlinx.coroutines.future.await
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.migor.feedless.AppLayer
import org.migor.feedless.AppProfiles
import org.migor.feedless.DisableDatabaseConfiguration
import org.migor.feedless.DisableMailConfiguration
import org.migor.feedless.api.graphql.ServerConfigResolver
import org.migor.feedless.generated.DgsClient
import org.migor.feedless.generated.types.CreateAttachmentFieldsInput
import org.migor.feedless.generated.types.CreateAttachmentInput
import org.migor.feedless.generated.types.RecordUniqueWhereInput
import org.migor.feedless.mail.MailService
import org.migor.feedless.report.ReportService
import org.migor.feedless.secrets.OneTimePasswordDAO
import org.migor.feedless.session.PermissionService
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.boot.test.mock.mockito.MockBeans
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.context.annotation.Import
import org.springframework.mock.web.MockMultipartFile
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.reactive.function.client.WebClient

@SpringBootTest(
  webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
  properties = [
    "app.apiGatewayUrl=https://localhost",
    "app.actuatorPassword=s3cr3t",
  ],
)
@ActiveProfiles(
  "test",
  AppLayer.api,
  AppLayer.service,
  AppProfiles.properties,
  AppLayer.security,
  AppProfiles.mail,
  AppProfiles.session,
)
@MockBeans(
  MockBean(ServerConfigResolver::class),
  MockBean(PermissionService::class),
  MockBean(OneTimePasswordDAO::class),
  MockBean(ReportService::class),
  MockBean(MailService::class),
)
@Import(
  DisableDatabaseConfiguration::class,
  DisableMailConfiguration::class,
)
class AttachmentResolverIntTest {

  private lateinit var monoGraphQLClient: WebClientGraphQLClient

  @LocalServerPort
  private var port: Int = 0

  @MockBean
  lateinit var attachmentService: AttachmentService

  @BeforeEach
  fun setUp() {
    val webClient = WebClient.create("http://localhost:$port/graphql")
    this.monoGraphQLClient = MonoGraphQLClient.createWithWebClient(webClient)
  }

  @Test
  fun createAttachment() = runTest {
//    Mockito.`when`(mailAuthenticationService.authenticateUsingMail(any2())).thenReturn(confirmCode)
    val fileContent = "Hello, this is a test file.".toByteArray()
    val multipartFile = MockMultipartFile(
      "file",
      "test.txt",
      "text/plain",
      fileContent
    )

    val graphQLMutation = DgsClient.buildMutation {
      createAttachment(
        data = CreateAttachmentInput(
          where = RecordUniqueWhereInput(
            id = ""
          ),
          attachment = CreateAttachmentFieldsInput(
            data = multipartFile,
            name = ""
          )
        )
      ) {
        type
        url
      }
    }

    val response = monoGraphQLClient.reactiveExecuteQuery(graphQLMutation)
      .toFuture()
      .await()
//      .extractValueAsObject("data.createAttachment", AttachmentDto::class.java)
      .extractValue<LinkedHashMap<String, Any>>("data.createAttachment")

    val attachment = ObjectMapper().convertValue(response, Map::class.java)

//    assertThat(auth[DgsConstants.CONFIRMCODE.Length] as Int).isEqualTo(confirmCode.length)
//    assertThat(auth[DgsConstants.CONFIRMCODE.OtpId] as String).isEqualTo(confirmCode.otpId)
  }

}

