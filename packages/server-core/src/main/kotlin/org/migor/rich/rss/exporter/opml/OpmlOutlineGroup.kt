package org.migor.rich.rss.exporter.opml

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlAttribute
import jakarta.xml.bind.annotation.XmlElement
import jakarta.xml.bind.annotation.XmlRootElement


@XmlRootElement(name = "outline")
@XmlAccessorType(XmlAccessType.FIELD)
class OpmlOutlineGroup {

  @XmlAttribute(name = "title", required = true)
  lateinit var title: String

  @XmlAttribute(name = "text")
  var text: String? = null

  @XmlAttribute(name = "type")
  var type: String? = null

  @XmlElement(name = "outline")
  var outlines: List<OpmlOutline>? = null
}
