package org.migor.rss.rich.services

import org.migor.rss.rich.dtos.UserDto
import org.migor.rss.rich.models.User
import org.migor.rss.rich.repositories.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class UserService {
  @Autowired
  lateinit var repository: UserRepository

  fun list(): Page<UserDto> {
    return repository.findAll(PageRequest.of(0, 10))
      .map { s: User? -> s?.toDto()}
  }

}
