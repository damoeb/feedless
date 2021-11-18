package org.migor.rss.rich.plugins

import org.migor.rss.rich.database.enums.PostProcessorType
import org.migor.rss.rich.database.model.Article
import org.migor.rss.rich.database.model.Bucket
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.io.File

data class ShellCommandOptions(val cmd: String, val workingDir: String, val mount: String)

@Service
class ShellCommandPlugin : PostProcessorPlugin<ShellCommandOptions> {

  override fun getType(): PostProcessorType = PostProcessorType.SHELL_COMMAND
  override fun getDefaultOptions(): ShellCommandOptions = ShellCommandOptions("", "", "")
  override fun process(article: Article, bucket: Bucket, options: Map<String, String>?) {
//    runCommand()
  }

  companion object {
    private val log = LoggerFactory.getLogger(ShellCommandPlugin::class.simpleName)

    fun runCommand(
      command: String, workingDirOpt: File? = null
    ): Result<Int> {
      log.info("Executing ${command}")
      return runCatching {
        // runuser -l  userNameHere -c '/path/to/command arg1 arg2'
        ProcessBuilder("\\s".toRegex().split(command))
          .also { pb -> workingDirOpt?.let { workingDir -> pb.directory(workingDir) } }
          .inheritIO()
          .start()
//          .also { pb -> timeoutSecOpt?.let { timeoutSec -> pb.waitFor(timeoutSec, TimeUnit.SECONDS) } }
          .waitFor()
      }.onSuccess { log.info("harvested") }
        .onFailure { e -> log.error("Failed to execute: ${e.message}") }
    }
  }

}
