package org.migor.rich.rss.pipeline

import org.migor.rich.rss.database.enums.ArticleHookType
import org.migor.rich.rss.database.model.ArticleHookSpec
import org.migor.rich.rss.database.model.Bucket
import org.migor.rich.rss.database.model.NamespacedTag
import org.migor.rich.rss.harvest.ArticleSnapshot
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.io.File

//data class ShellCommandOptions(val cmd: String, val workingDir: String, val mount: String)

@Profile("stateful")
@Service
class ShellCommandHook : PipelineHook {

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
