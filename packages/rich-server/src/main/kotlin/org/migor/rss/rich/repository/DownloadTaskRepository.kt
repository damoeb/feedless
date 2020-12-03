package org.migor.rss.rich.repository

import org.migor.rss.rich.model.DownloadTask
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface DownloadTaskRepository: CrudRepository<DownloadTask, String> {
}
