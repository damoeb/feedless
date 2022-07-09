package org.migor.rich.rss.user

import org.migor.rich.rss.api.ApiErrorCode
import org.migor.rich.rss.api.ApiException
import org.migor.rich.rss.database.model.BucketType
import org.migor.rich.rss.database.model.User
import org.migor.rich.rss.database.repository.BucketRepository
import org.migor.rich.rss.database.repository.UserRepository
import org.migor.rich.rss.service.BucketService
import org.migor.rich.rss.service.NoteService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Profile("database")
class UserService {

  private val log = LoggerFactory.getLogger(UserService::class.simpleName)

  @Autowired
  lateinit var noteService: NoteService

  @Autowired
  lateinit var userRepository: UserRepository

  @Autowired
  lateinit var bucketRepository: BucketRepository

  @Autowired
  lateinit var bucketService: BucketService

  @Transactional
  fun signup(corrId: String, signupUser: SignupUserDto): User {
    if (userRepository.existsByEmail(signupUser.email)) {
      throw ApiException(ApiErrorCode.INTERNAL_ERROR, "")
    }
    this.log.info("[${corrId}] Creating user ${signupUser.email}")
    val user = User()
    user.email = signupUser.email
    user.name = signupUser.name
    val saved = userRepository.save(user)

    val userId = user.id!!
    bucketService.createBucket(corrId, "inbox", userId, BucketType.INBOX, isPublic = false)
    val privateBucket = bucketService.createBucket(corrId, "private", userId, BucketType.ARCHIVE, isPublic = false)
    bucketService.createBucket(corrId, "public", userId, BucketType.ARCHIVE, isPublic = true)

//    noteService.createRootNote(corrId, userId, privateBucket)

    return saved
  }
}
