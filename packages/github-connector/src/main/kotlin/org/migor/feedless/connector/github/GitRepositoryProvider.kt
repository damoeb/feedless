package org.migor.feedless.connector.github

import com.google.gson.Gson
import org.migor.feedless.PageableRequest
import org.migor.feedless.capability.CapabilityId
import org.migor.feedless.capability.UnresolvedCapability
import org.migor.feedless.repository.RepositoriesFilter
import org.migor.feedless.repository.Repository
import org.migor.feedless.repository.RepositoryProvider
import org.migor.feedless.user.UserId
import org.springframework.stereotype.Component

@Component
class GitRepositoryProvider : RepositoryProvider {
  override suspend fun expectsCapabilities(capabilityId: CapabilityId): Boolean {
    return capabilityId == GithubCapability.ID
  }

  override suspend fun provideAll(
    capability: UnresolvedCapability,
    pageable: PageableRequest,
    where: RepositoriesFilter?
  ): List<Repository> {
    val githubCapability = Gson().fromJson(capability.capabilityPayload, GithubCapability::class.java)

    val userId = UserId() // todo get from capability

    val accountConfig = GithubAccountConfig(githubCapability)
    val githubAccount = GithubAccount(accountConfig)
    val githubRepositories = githubAccount.repositories()

    val repositories = githubRepositories.map { githubRepo ->
      Repository(
        title = githubRepo.name,
        description = githubRepo.description ?: "",
        ownerId = userId
      )
    }

    // Apply text filtering if present
    val filtered = where?.text?.let { textFilter ->
      repositories.filter { repo ->
        repo.title.contains(textFilter.query, ignoreCase = true) ||
          repo.description.contains(textFilter.query, ignoreCase = true)
      }
    } ?: repositories

    // Apply pagination
    val startIndex = pageable.pageNumber * pageable.pageSize
    val endIndex = minOf(startIndex + pageable.pageSize, filtered.size)

    return if (startIndex < filtered.size) {
      filtered.subList(startIndex, endIndex)
    } else {
      emptyList()
    }
  }
}
