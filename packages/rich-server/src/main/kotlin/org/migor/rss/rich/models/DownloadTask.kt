package org.migor.rss.rich.models

import javax.persistence.*

@Entity
@Table(name = "download_task")
class DownloadTask {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(columnDefinition = "serial")
  val id: Long? = null

}
