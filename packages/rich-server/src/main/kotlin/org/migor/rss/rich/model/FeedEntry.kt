package org.migor.rss.rich.model

import org.hibernate.annotations.GenericGenerator
import javax.persistence.*


@Entity
@Table(name = "t_feed_entry")
class FeedEntry() {

  constructor(entry: SourceEntry, feed: Feed) : this() {
    this.entry = entry
    this.feed = feed
  }

  @Id
  @GeneratedValue(generator = "uuid")
  @GenericGenerator(name = "uuid", strategy = "uuid2")
  var id: String? = null

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "feed_id")
  var feed: Feed? = null

  @Column(name = "feed_id",
    updatable = false, insertable = false)
  var feedId: String? = null

  @OneToOne(cascade = [CascadeType.DETACH], fetch = FetchType.EAGER, orphanRemoval = false)
  @JoinColumn(name = "entry_id")
  var entry: SourceEntry? = null

  @Column(name = "entry_id",
    updatable = false, insertable = false)
  var entryId: String? = null


}
