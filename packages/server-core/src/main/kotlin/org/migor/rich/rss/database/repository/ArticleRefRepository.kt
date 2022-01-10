package org.migor.rich.rss.database.repository

import org.migor.rich.rss.database.model.ArticleRef
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ArticleRefRepository : CrudRepository<ArticleRef, String>
