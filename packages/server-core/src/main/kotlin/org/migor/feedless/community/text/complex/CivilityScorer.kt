package org.migor.feedless.community.text.complex

import jakarta.annotation.PostConstruct
import org.migor.feedless.AppProfiles
import org.migor.feedless.community.CommentEntity
import org.migor.feedless.community.LanguageService
import org.migor.feedless.community.text.simple.VocabularyScorer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service


data class CivilityWeights(val sentiment: Double, val attacks: Double, val politeness: Double)

@Service
@Profile(AppProfiles.community)
class CivilityScorer {

  private val log = LoggerFactory.getLogger(VocabularyScorer::class.simpleName)
//  private var categorizers = mutableMapOf<String, DocumentCategorizerME>()

  @Autowired
  lateinit var languageService: LanguageService

  @PostConstruct
  fun postConstruct() {

//    val inputStreamFactory: InputStreamFactory =
//      MarkableFileInputStreamFactory(File("C:\\Users\\emehm\\Desktop\\data\\training_data.txt"))
//    val lineStream: ObjectStream<String> = PlainTextByLineStream(inputStreamFactory, "UTF-8")
//    val sampleStream: ObjectStream<DocumentSample> = DocumentSampleStream(lineStream)
//    val model = DocumentCategorizerME.train("en", sampleStream, TrainingParameters.defaultParams(), DoccatFactory())

//    categorizers.putAll(arrayOf("en", "de").map { lang -> LocaleUtils.toLocale(lang).isO3Language to DocumentCategorizerME(DoccatModel(ResourceUtils.getFile("classpath:models/opennlp-${lang}-ud-pos-1.0-1.9.3.bin"))) })

//    val outcomes = myCategorizer.categorize(arrayOf<String>(this.getFileContent()))
//    val category = myCategorizer.getBestCategory(outcomes)
//    val map = myCategorizer.scoreMap(arrayOf<String>(this.getFileContent()))
//    println(category)
  }

  fun civility(comment: CommentEntity, w: CivilityWeights): Double {
    /*
    Language Analysis: Let S be a sentiment score indicating the positivity or negativity of the language used.
Detection of Personal Attacks: Let A be a binary indicator (0 or 1) representing the presence or absence of personal attacks.
Politeness Indicators: Let P be a score indicating the presence of politeness indicators.
C = wS*S + wA*A + wP*P
     */
    return arrayOf(
      w.sentiment * scoreSentiment(comment),
      w.attacks * hasAttacks(comment).toInt(),
      w.politeness * scorePoliteness(comment)
    ).average()
  }

  fun scorePoliteness(comment: CommentEntity): Double {
    // todo
    return 0.0
  }

  fun hasAttacks(comment: CommentEntity): Boolean {
    // todo
    return false
  }


  fun scoreSentiment(comment: CommentEntity): Double {
//    sentiment
//    SentimentAnnotator.STANFORD_SENTIMENT
//    val text = comment.value
//    val language = languageService.bestLanguage(text)
//    if (language.confidence > 0.5) {
//      categorizers.get(language)
//    }
//
//    val annotation: Annotation = pipeline.process(text)
//    val sentences: List<CoreMap> = annotation.get(CoreAnnotations.SentencesAnnotation::class.java)
//    sentences.get(0).get(SentimentCoreAnnotations.SentimentClass::class.java)
    // todo
    return 0.0
  }

}

private fun Boolean.toInt(): Int = if (this) 1 else 0
