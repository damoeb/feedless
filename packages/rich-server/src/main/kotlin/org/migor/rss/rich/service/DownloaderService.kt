package org.migor.rss.rich.service

import org.migor.rss.rich.model.DownloadTask
import org.migor.rss.rich.repository.DownloadTaskRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class DownloaderService {

  @Autowired
  lateinit var downloadTaskRepository: DownloadTaskRepository;

  fun queue(task: DownloadTask): DownloadTask {
    return this.downloadTaskRepository.save(task);
  }


}
