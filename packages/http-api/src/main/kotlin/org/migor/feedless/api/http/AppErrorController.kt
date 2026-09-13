package org.migor.feedless.api.http

import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.boot.webmvc.error.ErrorController
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.RequestMapping


@Controller
class AppErrorController : ErrorController {

  @RequestMapping("/error")
  fun errorHtml(request: HttpServletRequest, response: HttpServletResponse) {
    response.sendRedirect("/")
  }
}
