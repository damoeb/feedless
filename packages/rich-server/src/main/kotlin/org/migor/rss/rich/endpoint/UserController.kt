package org.migor.rss.rich.endpoint

import org.migor.rss.rich.service.SubscriptionService
import org.migor.rss.rich.service.UserService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.ModelAndView
import java.util.stream.Collectors


@Controller
class UserController {

  @Autowired
  lateinit var userService: UserService

  @Autowired
  lateinit var subscriptionService: SubscriptionService

  @GetMapping("/u:{emailHash}")
  fun user(@PathVariable("emailHash") emailHash: String): ModelAndView {
    val mav = ModelAndView("user")
    val user = userService.findByEmailHash(emailHash)
    mav.addObject("name", user.name)
    mav.addObject("description", user.description)
    mav.addObject("feeds", arrayOf("private", "public"))
    mav.addObject("subscriptions", subscriptionService.publicSubscriptions(user.id!!))
    return mav
  }

  @GetMapping("/u")
  fun users(): ModelAndView {
    val mav = ModelAndView("users")
    val users = userService.list()
    mav.addObject("users", users.get().collect(Collectors.toList()))
    mav.addObject("page", users.number)
    mav.addObject("isFirst", users.isFirst)
    mav.addObject("isLast", users.isLast)
    return mav
  }

}
