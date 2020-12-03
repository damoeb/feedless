package org.migor.rss.rich.endpoints

import org.migor.rss.rich.dtos.UserDto
import org.migor.rss.rich.services.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController


@RestController
class UserController {

  @Autowired
  lateinit var userService: UserService

  @GetMapping("/users")
  fun list(): Page<UserDto> {
    return userService.list()
  }

}
