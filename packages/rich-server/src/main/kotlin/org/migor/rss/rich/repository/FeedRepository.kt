package org.migor.rss.rich.repository

import org.migor.rss.rich.model.Feed
import org.migor.rss.rich.model.Subscription
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.CrudRepository
import org.springframework.data.repository.PagingAndSortingRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.*
import javax.transaction.Transactional

@Repository
interface FeedRepository: PagingAndSortingRepository<Feed, String>
