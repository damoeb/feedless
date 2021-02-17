package org.migor.rss.rich.service

import org.migor.rss.rich.dto.UserDto
import org.migor.rss.rich.model.User
import org.migor.rss.rich.repository.UserRepository
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.stereotype.Service

@Service
class UserService {

  @Autowired
  lateinit var userRepository: UserRepository

  fun list(): Page<UserDto> {
    return userRepository.findAll(PageRequest.of(0, 10))
      .map { s: User? -> s?.toDto() }
  }

  fun findByEmailHash(emailHash: String): UserDto {
    return userRepository.findByEmailHash(emailHash).toDto()
  }

  fun findById(ownerId: String): UserDto {
    return userRepository.findById(ownerId).orElseThrow().toDto()
  }

}
