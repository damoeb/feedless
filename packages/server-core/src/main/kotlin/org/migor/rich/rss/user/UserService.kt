package org.migor.rich.rss.user

import org.migor.rich.rss.api.ApiErrorCode
import org.migor.rich.rss.api.ApiException
import org.migor.rich.rss.database.model.BucketType
import org.migor.rich.rss.database.model.User
import org.migor.rich.rss.database.repository.BucketRepository
import org.migor.rich.rss.database.repository.StreamRepository
import org.migor.rich.rss.database.repository.UserRepository
import org.migor.rich.rss.service.BucketService
import org.migor.rich.rss.service.NoteService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
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

  @Autowired
  lateinit var streamRepository: StreamRepository

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
    bucketService.createBucket(corrId, "inbox", userId, BucketType.INBOX)
    bucketService.createBucket(corrId, "archive", userId, BucketType.ARCHIVE)

//    noteService.createRootNote(corrId, userId)

    return saved
  }
}
