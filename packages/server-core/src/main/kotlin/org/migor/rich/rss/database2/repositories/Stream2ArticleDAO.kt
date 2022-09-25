package org.migor.rich.rss.database2.repositories

import org.migor.rich.rss.database2.models.Stream2ArticleEntity
import org.migor.rich.rss.database2.models.StreamEntity
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface Stream2ArticleDAO : CrudRepository<Stream2ArticleEntity, String>
