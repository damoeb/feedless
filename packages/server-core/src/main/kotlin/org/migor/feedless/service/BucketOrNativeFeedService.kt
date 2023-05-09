package org.migor.feedless.service

import org.migor.feedless.AppProfiles
import org.migor.feedless.api.auth.CurrentUser
import org.migor.feedless.data.jpa.EntityWithUUID
import org.migor.feedless.data.jpa.StandardJpaFields
import org.migor.feedless.data.jpa.enums.EntityVisibility
import org.migor.feedless.data.jpa.repositories.BucketDAO
import org.migor.feedless.data.jpa.repositories.NativeFeedDAO
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import java.util.*

@Service
@Profile(AppProfiles.database)
class BucketOrNativeFeedService {

  private val log = LoggerFactory.getLogger(BucketOrNativeFeedService::class.simpleName)

  @Autowired
  lateinit var currentUser: CurrentUser

  @Autowired
  lateinit var bucketDAO: BucketDAO

  @Autowired
  lateinit var nativeFeedDAO: NativeFeedDAO

  fun findAll(offset: Int, pageSize: Int): List<EntityWithUUID> {
    return (currentUser.userId()
      ?.let { bucketDAO.findAllMixed(it, offset, pageSize)
        .map { Pair(it[0] as UUID, it[1] as Boolean) }
        .map { pair ->
          if (pair.second) {
            log.info("bucket ${pair.first}")
            bucketDAO.findById(pair.first).orElseThrow {IllegalArgumentException("bucket not found")}
          } else {
            log.info("feed ${pair.first}")
            nativeFeedDAO.findById(pair.first).orElseThrow {IllegalArgumentException("nativeFeed not found")}
          }
        }}
      ?: bucketDAO.findAllPublic(EntityVisibility.isPublic,
        PageRequest.of(offset/pageSize, pageSize, Sort.by(Sort.Direction.DESC, StandardJpaFields.createdAt))))

  }

}
