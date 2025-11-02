package org.migor.feedless.connector.github

import org.migor.feedless.capability.Capability
import org.migor.feedless.storage.LocalGitRepository
import org.migor.feedless.storage.LocalGitRepositoryCapability
import org.migor.feedless.storage.LocalGitRepositoryFile
import java.io.File

class LocalGitRepositoryImpl(repoUrl: String) : LocalGitRepository {

  private val localDir: File
//  private val git: Git

  init {

    this.localDir = File.createTempFile("private-repo", "")
//    val credentials = UsernamePasswordCredentialsProvider("oauth2", token)
//
//    this.git = Git.cloneRepository()
//      .setURI(repoUrl)
//      .setCredentialsProvider(credentials)
//      .setDirectory(localDir)
//      .call()

//    git.checkout().setName()
  }

  override fun checkout(branch: String): LocalGitRepository {
//    git.checkout().setName(branch).call()
    return this
  }

  override fun files(): List<LocalGitRepositoryFile> {
//    git.push().call()
    TODO("Not yet implemented")
  }

  override fun add(files: List<LocalGitRepositoryFile>): LocalGitRepository {
    TODO("Not yet implemented")
  }

  override fun commit(): LocalGitRepository {
    TODO("Not yet implemented")
  }

  override fun push(): LocalGitRepository {
    TODO("Not yet implemented")
  }

  override fun capability(): Capability<LocalGitRepositoryCapability> {
    return Capability("repository", LocalGitRepositoryCapability(localDir))
  }

}
