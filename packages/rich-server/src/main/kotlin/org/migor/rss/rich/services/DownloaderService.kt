package org.migor.rss.rich.services

import org.migor.rss.rich.models.DownloadTask
import org.migor.rss.rich.repositories.DownloadTaskRepository
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
