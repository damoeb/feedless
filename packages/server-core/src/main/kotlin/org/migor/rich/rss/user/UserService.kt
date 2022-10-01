package org.migor.rich.rss.user

import org.migor.rich.rss.api.ApiErrorCode
import org.migor.rich.rss.api.ApiException
import org.migor.rich.rss.database.enums.BucketType
import org.migor.rich.rss.database.models.UserEntity
import org.migor.rich.rss.database.repositories.UserDAO
import org.migor.rich.rss.service.BucketService
import org.migor.rich.rss.service.NoteService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Profile("database2")
class UserService {

  private val log = LoggerFactory.getLogger(UserService::class.simpleName)

  @Autowired
  lateinit var noteService: NoteService

  @Autowired
  lateinit var userDAO: UserDAO

//  @Autowired
//  lateinit var bucketRepository: BucketRepository
//
  @Autowired
  lateinit var bucketService: BucketService

  @Transactional
  fun signup(corrId: String, signupUser: SignupUserDto): UserEntity {
    if (userDAO.existsByEmail(signupUser.email)) {
      throw ApiException(ApiErrorCode.INTERNAL_ERROR, "")
    }
    this.log.info("[${corrId}] Creating user ${signupUser.email}")
    val user = UserEntity()
    user.email = signupUser.email
    user.name = signupUser.name
    val saved = userDAO.save(user)

    val userId = user.id
    bucketService.createBucket(corrId, "inbox", user, BucketType.INBOX, isPublic = false)
    val privateBucket = bucketService.createBucket(corrId, "private", user, BucketType.ARCHIVE, isPublic = false)
    bucketService.createBucket(corrId, "public", user, BucketType.ARCHIVE, isPublic = true)

//    noteService.createRootNote(corrId, userId, privateBucket)

    return saved
  }
}
