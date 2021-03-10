package org.migor.rss.rich.controller

import org.migor.rss.rich.dto.FeedDto
import org.migor.rss.rich.model.AccessPolicy
import org.migor.rss.rich.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.ModelAndView


@Controller
class EditorController {

  @Autowired
  lateinit var userService: UserService

  @GetMapping("/user:{emailHash}/editor")
  fun editor(@PathVariable("emailHash") emailHash: String): ModelAndView {
    val mav = ModelAndView("editor")
    val user = userService.findByEmailHash(emailHash)
    mav.addObject("user", user)
    mav.addObject("feeds", user.feeds.filter { feed: FeedDto -> feed.accessPolicy != AccessPolicy.PUBLIC })

    return mav
  }
}
