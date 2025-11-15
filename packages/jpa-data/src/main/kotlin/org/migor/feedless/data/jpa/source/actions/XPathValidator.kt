package org.migor.feedless.data.jpa.source.actions

import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import org.apache.commons.lang3.StringUtils
import org.jsoup.select.Selector
import us.codecraft.xsoup.Xsoup

class XPathValidator : ConstraintValidator<XPathConstraint, String?> {

  override fun isValid(xpath: String?, context: ConstraintValidatorContext): Boolean {

    return if (xpath == null || StringUtils.isBlank(xpath)) {
      false
    } else {
      try {
        Xsoup.compile(xpath)
        true
      } catch (e: Selector.SelectorParseException) {
        false
      }
    }
  }
}
