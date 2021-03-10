package org.migor.rss.rich.controller

import org.migor.rss.rich.service.EntryService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.servlet.ModelAndView


@Controller
class ViewerController {

  @Autowired
  lateinit var entryService: EntryService

  @GetMapping("/viewer:{entryId}")
  fun editor(@PathVariable("entryId") entryId: String): ModelAndView {
    val mav = ModelAndView("entryId")
    val entry = entryService.findById(entryId)
    mav.addObject("entry", entry)

    return mav
  }
}
