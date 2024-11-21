package org.migor.feedless.repository

import org.migor.feedless.AppProfiles
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate

//@Service
@Profile(AppProfiles.saas)
class GithubService {
  private val githubApiUrl = "https://api.github.com"

  fun checkAndStarFeedless(token: String): String {
    return this.checkAndStarRepo("damoeb", "feedless", token)
  }

  private fun checkAndStarRepo(owner: String, repo: String, token: String): String {
    val restTemplate = RestTemplate()

    val entity = prepareBody(token)

    // Check if the repo is already starred
    val urlCheckStar = "$githubApiUrl/user/starred/$owner/$repo"
    return try {
      val response: ResponseEntity<String> = restTemplate.exchange(
        urlCheckStar, HttpMethod.GET, entity, String::class.java
      )
      if (response.statusCode.is2xxSuccessful) {
        "Repository is already starred."
      } else {
        starRepo(restTemplate, owner, repo, token)
      }
    } catch (ex: HttpClientErrorException.NotFound) {
      // Repo is not starred, so star it
      starRepo(restTemplate, owner, repo, token)
    }
  }

  private fun starRepo(restTemplate: RestTemplate, owner: String, repo: String, token: String): String {
    val entity = prepareBody(token)

    val urlStarRepo = "$githubApiUrl/user/starred/$owner/$repo"
    return try {
      restTemplate.exchange(urlStarRepo, HttpMethod.PUT, entity, String::class.java)
      "Repository has been starred."
    } catch (ex: Exception) {
      "Failed to star the repository: ${ex.message}"
    }
  }

  private fun prepareBody(token: String): HttpEntity<String> {
    val headers = HttpHeaders().apply {
      set("Authorization", "Bearer $token")
      set("Accept", "application/vnd.github.v3+json")
    }
    val entity = HttpEntity<String>(headers)
    return entity
  }
}
