package org.migor.rich.rss.exporter.opml

import jakarta.xml.bind.annotation.XmlAccessType
import jakarta.xml.bind.annotation.XmlAccessorType
import jakarta.xml.bind.annotation.XmlAttribute
import jakarta.xml.bind.annotation.XmlRootElement


@XmlRootElement(name = "outline")
@XmlAccessorType(XmlAccessType.FIELD)
class OpmlOutline {

  @XmlAttribute(name = "text")
  var text: String? = null

  @XmlAttribute(name = "title", required = true)
  lateinit var title: String

//  @XmlAttribute(name = "type")
//  var type: String? = null

  @XmlAttribute(name = "xmlUrl", required = true)
  lateinit var xmlUrl: String

  @XmlAttribute(name = "htmlUrl", required = true)
  lateinit var htmlUrl: String
}
