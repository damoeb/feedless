package org.migor.rich.rss.exporter.opml

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlElementWrapper
import jakarta.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "opml")
@XmlAccessorType(XmlAccessType.FIELD)
class OpmlDocument {
  lateinit var version: String

  @XmlElementWrapper(name = "head")
  @XmlElement(name = "title")
  var title: List<String>? = null

  @XmlElementWrapper(name = "body")
  @XmlElement(name = "outline")
  lateinit var outlines: List<OpmlOutlineGroup>
}
