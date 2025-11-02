//package org.migor.feedless.connector.github
//
//import org.assertj.core.api.Java6Assertions.assertThat
//import org.junit.jupiter.api.Test
//import org.migor.feedless.storage.AnonymousGitConnectionConfig
//import org.migor.feedless.storage.AuthenticatedGitConnectionConfig
//import org.migor.feedless.storage.GitAccountCredentials
//import java.net.URI
//
//class GithubConnectorTest {
//  @Test
//  fun connectAnonymous() {
//    val connectionConfig = AnonymousGitConnectionConfig(urls = listOf(URI("https://github.com/damoeb/feedless")))
//    val connector = GithubConnector()
//    val connection = connector.connect(connectionConfig)
//
//    val connectionCapability = connection.capability()
//    assertThat(connectionCapability).isNotNull()
//    assertThat(connectionCapability.capabilityId).isNotNull()
//    assertThat(connectionCapability.capabilityPayload).isNotNull()
//
//    val repositories = connection.repositories()
//    assertThat(repositories).hasSize(1)
//
//    val localRepository = repositories.first()
//      .clone()
//      .checkout(branch = "master")
////    val localRepositoryCapability = localRepository.capability()
////    assertThat(localRepositoryCapability).isNotNull()
////    assertThat(localRepositoryCapability.capabilityId).isNotNull()
////    assertThat(localRepositoryCapability.capabilityPayload).isNotNull()
////
////    val files = localRepository.files()
////    assertThat(files).hasSize(1)
//  }
//
//  @Test
//  fun connectAuthenticated() {
//    val connectionConfig = AuthenticatedGitConnectionConfig(GitAccountCredentials("username", "password"))
//    val connector = GithubConnector()
//    val connection = connector.connect(connectionConfig)
//
////    val accountCapability = connectionConfig.capability()
////    assertThat(accountCapability).isNotNull()
////    assertThat(accountCapability.capabilityId).isNotNull()
////    assertThat(accountCapability.capabilityPayload).isNotNull()
////
////    val repositories = connectionConfig.repositories()
////
////    val localRepository = repositories.first()
////      .clone()
////      .checkout(branch = "master")
////    val localRepositoryCapability = localRepository.capability()
////    assertThat(localRepositoryCapability).isNotNull()
////    assertThat(localRepositoryCapability.capabilityId).isNotNull()
////    assertThat(localRepositoryCapability.capabilityPayload).isNotNull()
////
////    val files = localRepository.files()
////    assertThat(files).hasSize(1)
//  }
//}
