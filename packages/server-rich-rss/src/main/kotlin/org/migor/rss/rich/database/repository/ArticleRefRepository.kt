package org.migor.rss.rich.database.repository

import org.migor.rss.rich.database.model.ArticleRef
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
interface ArticleRefRepository : CrudRepository<ArticleRef, String>
