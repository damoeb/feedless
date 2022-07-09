package org.migor.rich.rss.database.repository

import org.migor.rich.rss.database.model.ArticleRef
import org.springframework.context.annotation.Profile
import org.springframework.data.repository.CrudRepository
import org.springframework.stereotype.Repository

@Repository
@Profile("database")
interface ArticleRefRepository : CrudRepository<ArticleRef, String>
