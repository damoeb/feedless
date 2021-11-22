package org.migor.rss.rich.pipeline

import org.migor.rss.rich.database.enums.ArticleHookType
import org.migor.rss.rich.database.model.ArticleHookSpec
import org.migor.rss.rich.database.model.Bucket
import org.migor.rss.rich.database.model.NamespacedTag
import org.migor.rss.rich.harvest.ArticleSnapshot
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

//data class ShellCommandOptions(val cmd: String, val workingDir: String, val mount: String)

@Service
class ShellCommandHook : ArticleHook {

  override fun process(
    corrId: String,
    snapshot: ArticleSnapshot,
    bucket: Bucket,
    hookSpec: ArticleHookSpec,
    addTag: (NamespacedTag) -> Boolean,
    addData: (Pair<String, String>) -> String?
  ): Boolean {
//    runCommand()
    return true
  }

  override fun type(): ArticleHookType = ArticleHookType.shellCommand

  companion object {
    private val log = LoggerFactory.getLogger(ShellCommandHook::class.simpleName)

    fun runCommand(
      corrId: String, command: String, workingDirOpt: File? = null
    ): Result<Int> {
      log.info("[${corrId}] Executing ${command}")
      return runCatching {
        // runuser -l  userNameHere -c '/path/to/command arg1 arg2'
        ProcessBuilder("\\s".toRegex().split(command))
          .also { pb -> workingDirOpt?.let { workingDir -> pb.directory(workingDir) } }
          .inheritIO()
          .start()
//          .also { pb -> timeoutSecOpt?.let { timeoutSec -> pb.waitFor(timeoutSec, TimeUnit.SECONDS) } }
          .waitFor()
      }.onSuccess { log.info("[${corrId}] harvested") }
        .onFailure { e -> log.error("[${corrId}] Failed to execute: ${e.message}") }
    }
  }

}
