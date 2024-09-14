package org.migor.feedless.pipeline.plugins


//fun getLastDocumentByRepositoryId(documentService: DocumentService, repositoryId: UUID): DocumentEntity? {
//  val pageable = PageRequest.of(0, 1, Sort.Direction.DESC, "createdAt")
//  return documentService.findAllByRepositoryId(
//    repositoryId,
//    status = ReleaseStatus.released,
//    pageable = pageable
//  ).firstOrNull()
//}

//@Service
//@Profile(AppProfiles.database)
//class DiffDataForwarderPlugin : FilterEntityPlugin, MailProviderPlugin {
//
//  private val log = LoggerFactory.getLogger(DiffDataForwarderPlugin::class.simpleName)
//
//  @Autowired
//  private lateinit var documentService: DocumentService
//
//  @Autowired
//  private lateinit var productService: ProductService
//
//  @Autowired
//  private lateinit var sourceDAO: SourceDAO
//
//  @Autowired
//  private lateinit var templateService: TemplateService
//
//  override fun id() = FeedlessPlugins.org_feedless_diff_email_forward.name
//  override fun name() = ""
//  override fun listed() = false
//
//  override fun filterEntity(
//    corrId: String,
//    document: DocumentEntity,
//    params: PluginExecutionParamsInput,
//    index: Int
//  ): Boolean {
//    log.debug("[$corrId] filter ${document.url}")
//
//    val increment = params.org_feedless_diff_email_forward.nextItemMinIncrement.coerceAtLeast(0.01)
//    log.info("[$corrId] filter nextItemMinIncrement=$increment")
//
//    val previous = getLastDocumentByRepositoryId(documentService, document.repositoryId)
//
//    return previous?.let {
//      val compareBy = params.org_feedless_diff_email_forward.compareBy!!
//      compareBy.fragmentNameRef?.let {
//        log.warn("[$corrId] resolving named fragment '${compareBy.fragmentNameRef}' not yet supported. Falling back to document")
//      }
//      when (compareBy.field!!) {
//        WebDocumentField.text -> compareByText(
//          corrId,
//          document.text,
//          previous.text,
//          increment
//        )
//
//        WebDocumentField.markup -> compareByText(
//          corrId,
//          document.html!!,
//          previous.html!!,
//          increment
//        )
//
//        WebDocumentField.pixel -> compareByPixel(
//          corrId,
//          document,
//          previous,
//          increment
//        )
//      }
//    } ?: true
//  }
//
////  override fun provideWelcomeMail(
////      corrId: String,
////      repository: RepositoryEntity,
////      mailForward: MailForwardEntity
////  ): MailData {
////    log.info("[$corrId] prepare welcome mail")
////    val mailData = MailData()
////    mailData.subject = "VisualDiff Tracker: ${repository.title}"
////    val website =
////      sourceDAO.findAllByRepositoryIdOrderByCreatedAtDesc(repository.id).joinToString(", ") { it.url }
////
////    val params = VisualDiffWelcomeParams(
////      trackerTitle = repository.title,
////      website = website,
////      trackerInfo = repository.sourcesSyncExpression,
////      activateTrackerMailsUrl = "${productService.getGatewayUrl(repository.product)}${ApiUrls.mailForwardingAllow}/${mailForward.id}",
////      info = ""
////    )
////    mailData.body = templateService.renderTemplate(corrId, VisualDiffWelcomeMailTemplate(params))
////
////    return mailData
////  }
//
//  override fun provideDocumentMail(
//    corrId: String,
//    document: DocumentEntity,
//    repository: RepositoryEntity,
//    params: PluginExecutionParamsInput
//  ): MailData {
//    log.info("[$corrId] prepare diff email")
//
//    val config = params.org_feedless_diff_email_forward
//
//    val mailData = MailData()
//
//    mailData.subject = repository.title
//
//    val sdf = SimpleDateFormat("yyyy/MM/dd-HH:mm")
//
//    config.inlineLatestImage = false
//    config.inlinePreviousImage = false
//    config.inlineDiffImage = true
//
//    val latestDateString = sdf.format(document.createdAt)
//    val images = mutableListOf(
//      Triple(
//        config.inlineLatestImage,
//        "latest-$latestDateString",
//        document.raw!!
//      )
//    )
//
//    val lastWebDocument = getLastDocumentByRepositoryId(this.documentService, repository.id)
//    lastWebDocument?.let {
//      if (document.id.toString() == lastWebDocument.id.toString()) {
//        throw IllegalArgumentException("comparing same document")
//      }
//      images.add(
//        Triple(
//          config.inlinePreviousImage,
//          "previous-${sdf.format(lastWebDocument.createdAt)}",
//          lastWebDocument.raw!!
//        )
//      )
//      images.add(
//        Triple(
//          config.inlineDiffImage,
//          "diff-${latestDateString}",
//          createDiffImage(lastWebDocument, document)
//        )
//      )
//    }
//
//    val inlined = mutableListOf<String>()
//    images.forEach { (shouldInline, contentId, data) ->
//      run {
//        if (BooleanUtils.isTrue(shouldInline)) {
//          inlined.add("<p><img src='cid:$contentId'></p>")
//          mailData.attachments.add(MailAttachment(contentId, ByteArrayDataSource(data, "image/webp"), true))
//        }
//        mailData.attachments.add(MailAttachment("$contentId.webp", ByteArrayDataSource(data, "image/webp")))
//      }
//    }
//
//    val website = sourceDAO.findAllByRepositoryIdOrderByCreatedAtDesc(
//      repository.id
//    ).joinToString(", ") { s -> s.url }
//
//    val templateParams = VisualDiffChangeDetectedParams(
//      trackerTitle = repository.title,
//      website = website,
//      inlineImages = inlined.joinToString("\n")
//    )
//    mailData.body = templateService.renderTemplate(corrId, VisualDiffChangeDetectedMailTemplate(templateParams))
//
//    return mailData
//  }
//
//  private fun toImage(record: DocumentEntity): BufferedImage {
//    return ImageIO.read(ByteArrayInputStream(record.raw))
//  }
//
//  private fun compareByPixel(
//    corrId: String,
//    left: DocumentEntity,
//    right: DocumentEntity,
//    minIncrement: Double
//  ): Boolean {
//    val img1 = toImage(left)
//    val img2 = toImage(right)
//
//    val width = img1.width.coerceAtMost(img2.width)
//    val height = img1.height.coerceAtMost(img2.height)
//    var changes = 0
//    for (x in 0 until width) {
//      for (y in 0 until height) {
//        if (img1.getRGB(x, y) != img2.getRGB(x, y)) {
//          changes++
//        }
//      }
//    }
//
//    changes += abs(img1.width - img2.width) * img1.height.coerceAtLeast(img2.height) + abs(img1.height - img2.height) * img1.width.coerceAtLeast(
//      img2.width
//    )
//    val total = (width * height)
//    val ratio = changes / total.toDouble()
//    log.info("[$corrId] pixelDistance=$changes total=$total ratio=$ratio minIncrement=$minIncrement")
//    return ratio > minIncrement
//  }
//
//  private fun compareByText(corrId: String, left: String, right: String, minIncrement: Double): Boolean {
//    val changes = LevenshteinDistance.getDefaultInstance().apply(left, right)
//    val len = left.length.coerceAtLeast(right.length)
//    val ratio = changes / len.toDouble()
//    log.info("[$corrId] editDistance=$changes total=$len ratio=$ratio minIncrement=$minIncrement")
//    return ratio > minIncrement
//  }
//
//  private fun createDiffImage(oldDocument: DocumentEntity, newDocument: DocumentEntity): ByteArray {
//    val oldImage = toImage(oldDocument)
//    val newImage = toImage(newDocument)
//
//    assert(newImage.width == oldImage.width) { "image width not equal" }
//    assert(newImage.height == oldImage.height) { "image height not equal" }
//
//    val pixelFilter = object : RGBImageFilter() {
//      override fun filterRGB(x: Int, y: Int, rgb: Int): Int {
//        return if (oldImage.getRGB(x, y) == rgb) {
//          0x64ffffff and rgb
//        } else {
//          0x7FFF0000
//        }
//      }
//    }
//
//    val diffImage = Toolkit.getDefaultToolkit().createImage(FilteredImageSource(newImage.source, pixelFilter))
//
//    val bimage = BufferedImage(diffImage.getWidth(null), diffImage.getHeight(null), BufferedImage.TYPE_INT_ARGB)
//    val g = bimage.createGraphics()
//    g.drawImage(diffImage, 0, 0, null)
//    g.dispose()
//
//    val baos = ByteArrayOutputStream()
//    ImageIO.write(bimage, "webp", baos)
//    return baos.toByteArray()
//  }
//
//}
