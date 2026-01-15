package org.migor.feedless.classifier

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import org.apache.commons.lang3.StringUtils
import org.json.JSONObject
import org.migor.feedless.document.Document
import org.migor.feedless.document.DocumentClass
import org.migor.feedless.document.DocumentClassifier
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.math.exp

class OnnxDocumentClassifier(
  private val modelPath: String? = null
) : DocumentClassifier {

  private val log = LoggerFactory.getLogger(OnnxDocumentClassifier::class.java)

  private lateinit var session: OrtSession
  private lateinit var vocab: Map<String, Int>
  private lateinit var id2label: Map<Int, String>
  private val maxLength = 512
  private val padTokenId = 0
  private val clsTokenId = 101
  private val sepTokenId = 102
  private val unkTokenId = 100

  private val resolvedModelPath: String by lazy {
    modelPath
      ?: System.getenv("DOCUMENT_CLASSIFIER_MODEL_PATH")
      ?: resolveDefaultModelPath()
  }

  init {
    loadModel()
    loadVocab()
    loadConfig()
  }

  private fun resolveDefaultModelPath(): String {
    // Try multiple possible paths
    val possiblePaths = listOf(
      "./packages/document-classifier/results/best_model_onnx",
      "./results/best_model_onnx",
      "../document-classifier/results/best_model_onnx",
      System.getProperty("user.dir") + "/packages/document-classifier/results/best_model_onnx"
    )

    for (path in possiblePaths) {
      val modelFile = File(path, "model.onnx")
      if (modelFile.exists()) {
        log.info("Found model at: ${modelFile.absolutePath}")
        return path
      }
    }

    throw IllegalStateException(
      "Model path not found. Tried: ${possiblePaths.joinToString()}. " +
        "Please specify modelPath parameter or set DOCUMENT_CLASSIFIER_MODEL_PATH environment variable."
    )
  }

  private fun loadModel() {
    log.info("Loading ONNX model from: $resolvedModelPath")
    val env = OrtEnvironment.getEnvironment()
    val modelFile = File(resolvedModelPath, "model.onnx")
    if (!modelFile.exists()) {
      throw IllegalStateException("Model file not found: ${modelFile.absolutePath}")
    }
    session = env.createSession(modelFile.absolutePath)
    log.info("ONNX model loaded successfully")
  }

  private fun loadVocab() {
    log.info("Loading vocabulary from: $resolvedModelPath")
    val vocabFile = File(resolvedModelPath, "vocab.txt")
    if (!vocabFile.exists()) {
      throw IllegalStateException("Vocabulary file not found: ${vocabFile.absolutePath}")
    }

    vocab = vocabFile.readLines()
      .mapIndexed { index, token -> token to index }
      .toMap()

    log.info("Vocabulary loaded: ${vocab.size} tokens")
  }

  private fun loadConfig() {
    val configFile = File(resolvedModelPath, "config.json")
    if (!configFile.exists()) {
      throw IllegalStateException("Config file not found: ${configFile.absolutePath}")
    }

    val config = JSONObject(configFile.readText())
    val id2labelObj = config.getJSONObject("id2label")
    id2label = id2labelObj.keys().asSequence().associate {
      it.toInt() to id2labelObj.getString(it)
    }

    log.info("Config loaded. Labels: $id2label")
  }

  override suspend fun classify(document: Document): DocumentClass {
    val text = StringUtils.trimToNull(document.text) ?: document.title ?: ""
    if (text.isBlank()) {
      return DocumentClass("UNKNOWN", 0.0)
    }

    try {
      // Tokenize the text
      val tokenIds = tokenize(text)

      // Convert IntArray to LongArray
      val inputIdsLong = LongArray(tokenIds.size) { tokenIds[it].toLong() }
      val attentionMaskLong = LongArray(tokenIds.size) { 1L }

      // Create input tensors - ONNX expects shape [batch_size, sequence_length]
      val inputIds = arrayOf(inputIdsLong)
      val attentionMask = arrayOf(attentionMaskLong)

      // Create ONNX tensors
      val inputIdsTensor = OnnxTensor.createTensor(
        OrtEnvironment.getEnvironment(),
        inputIds
      )
      val attentionMaskTensor = OnnxTensor.createTensor(
        OrtEnvironment.getEnvironment(),
        attentionMask
      )

      // Run inference
      val inputs = mapOf(
        "input_ids" to inputIdsTensor,
        "attention_mask" to attentionMaskTensor
      )

      val outputs = session.run(inputs)

      @Suppress("UNCHECKED_CAST")
      val logits = outputs[0].value as Array<FloatArray>

      // Apply softmax to get probabilities
      // Output shape is [batch_size, num_labels], so we take the first batch item
      val probabilities = softmax(logits[0])

      // Find the class with highest probability
      val maxIndex = probabilities.indices.maxByOrNull { probabilities[it] } ?: 0
      val maxProbability = probabilities[maxIndex]
      val label = id2label[maxIndex] ?: "UNKNOWN"

      // Clean up
      inputIdsTensor.close()
      attentionMaskTensor.close()
      outputs.close()

      log.info("Classified text as: $label with probability: $maxProbability")

      return DocumentClass(label, maxProbability.toDouble())

    } catch (e: Exception) {
      log.error("Error classifying document: ${e.message}", e)
      return DocumentClass("ERROR", 0.0)
    }
  }

  private fun tokenize(text: String): IntArray {
    // Basic WordPiece tokenization
    // This is a simplified version - for production, consider using a proper tokenizer library
    val normalizedText = text.lowercase().trim()

    // Split into words and subwords
    val words = normalizedText.split(Regex("\\s+"))
    val tokens = mutableListOf<Int>()

    // Add [CLS] token
    tokens.add(clsTokenId)

    // Tokenize each word
    for (word in words) {
      if (word.isEmpty()) continue

      // Try to find the word in vocabulary
      if (word in vocab) {
        tokens.add(vocab[word]!!)
      } else {
        // WordPiece tokenization: split into subwords
        val subwords = wordPieceTokenize(word)
        tokens.addAll(subwords)
      }

      // Limit to max_length - 1 (reserve space for [SEP])
      if (tokens.size >= maxLength - 1) {
        break
      }
    }

    // Add [SEP] token
    tokens.add(sepTokenId)

    // Pad to max_length
    while (tokens.size < maxLength) {
      tokens.add(padTokenId)
    }

    return tokens.take(maxLength).toIntArray()
  }

  private fun wordPieceTokenize(word: String): List<Int> {
    val tokens = mutableListOf<Int>()
    var remaining = word

    // Try to find longest matching subword
    while (remaining.isNotEmpty()) {
      var found = false
      var longestMatch = ""
      var longestMatchId = -1

      // Try to find the longest subword starting from the beginning
      for (i in remaining.length downTo 1) {
        val candidate = remaining.substring(0, i)
        val candidateWithPrefix = "##$candidate"

        if (candidate in vocab) {
          longestMatch = candidate
          longestMatchId = vocab[candidate]!!
          found = true
          break
        } else if (candidateWithPrefix in vocab) {
          longestMatch = candidateWithPrefix
          longestMatchId = vocab[candidateWithPrefix]!!
          found = true
          break
        }
      }

      if (found && longestMatchId != -1) {
        tokens.add(longestMatchId)
        remaining = remaining.substring(longestMatch.removePrefix("##").length)
      } else {
        // If no match found, use [UNK]
        tokens.add(unkTokenId)
        break
      }
    }

    return tokens
  }

  private fun softmax(logits: FloatArray): FloatArray {
    val maxLogit = logits.maxOrNull() ?: 0f
    val expValues = logits.map { exp(it - maxLogit) }
    val sum = expValues.sum()
    return expValues.map { (it / sum).toFloat() }.toFloatArray()
  }

  fun close() {
    session.close()
  }
}
