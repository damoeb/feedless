package org.migor.rss.rich.database.repository

import org.migor.rss.rich.database.model.ArticleRefToStream
import org.migor.rss.rich.database.model.ArticleRefToStreamId
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ArticleRefToStreamRepository : CrudRepository<ArticleRefToStream, ArticleRefToStreamId>
