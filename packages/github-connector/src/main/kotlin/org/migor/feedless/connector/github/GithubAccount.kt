package org.migor.feedless.connector.github

import com.google.gson.Gson
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse


class GithubAccount(private val accountConfig: GithubAccountConfig) {

  fun repositories(): List<GithubRepository> {

    val url = "https://api.github.com/user/repos?per_page=100"

    val client: HttpClient = HttpClient.newHttpClient()

    val request: HttpRequest = HttpRequest.newBuilder()
      .uri(URI.create(url))
      .header("Authorization", "Bearer ${accountConfig.capability.token}")
      .header("Accept", "application/vnd.github+json")
      .header("X-GitHub-Api-Version", "2022-11-28")
      .GET()
      .build()

    val response: HttpResponse<String> = client.send(request, HttpResponse.BodyHandlers.ofString())

    if (response.statusCode() == 200) {
      return Gson().fromJson<List<GithubRepository>>(response.body(), List::class.java)
    }
    throw IllegalStateException("Could not fetch repositories. ${response.body()}")
  }

//  fun capability(): Capability<GitConnectionCapability> {
//    return Capability("", capability)
//  }
}
