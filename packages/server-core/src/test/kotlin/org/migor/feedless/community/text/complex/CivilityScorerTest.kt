package org.migor.feedless.community.text.complex

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import org.migor.feedless.community.CommentEntity
import org.migor.feedless.community.LanguageService
import org.mockito.Mockito


@Tag("nlp")
class CivilityScorerTest {

  private lateinit var civilityScorer: CivilityScorer

  @BeforeEach
  fun setUp() {
    val languageService = LanguageService()
    languageService.postConstruct()

    civilityScorer = CivilityScorer()
    civilityScorer.languageService = languageService
    civilityScorer.postConstruct()

//        val inputStreamFactory: InputStreamFactory =
//      MarkableFileInputStreamFactory(File("C:\\Users\\emehm\\Desktop\\data\\training_data.txt"))
//    val lineStream: ObjectStream<String> = PlainTextByLineStream(inputStreamFactory, "UTF-8")
//    val sampleStream: ObjectStream<DocumentSample> = DocumentSampleStream(lineStream)
//    val model = DocumentCategorizerME.train("en", sampleStream, TrainingParameters.defaultParams(), DoccatFactory())
//    civilityScorer.categorizers.put("eng", model)

  }

  @Test
  fun scoreSentiment() {
    val comment = Mockito.mock(CommentEntity::class.java)
//      Mockito.`when`(comment.value).thenReturn("In general I would agree with this assessment. I really like your idea.")
    Mockito.`when`(comment.contentText).thenReturn("Du erzählst nur scheisse")
    civilityScorer.scoreSentiment(comment)
  }
}
