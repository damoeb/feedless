package org.migor.feedless.community.text.hyphenation

import java.io.Serializable
import java.util.*
import kotlin.math.max


/**
 * All hypenator sources from https://github.com/mfietz/JHyphenator
 *
 * Hyphenator.java is an highly optimized adaptation of parts from Mathew
 * Kurian's TextJustify-Android Library:
 * https://github.com/bluejamesbond/TextJustify-Android/
 */
class Hyphenator private constructor(pattern: HyphenationPattern) : Serializable {
  private val trie: TrieNode = createTrie(pattern.patterns)
  private val leftMin: Int = pattern.leftMin
  private val rightMin: Int = pattern.rightMin

  /**
   * Returns a list of syllables that indicates at which points the word can
   * be broken with a hyphen
   *
   * @param word Word to hyphenate
   * @return list of syllables
   */
  fun hyphenate(word: String): List<String> {
    var word = word
    word = "_" + word + "_"

    val lowercase = word.lowercase(Locale.getDefault())

    val wordLength = lowercase.length
    val points = IntArray(wordLength)
    val characterPoints = IntArray(wordLength)
    for (i in 0 until wordLength) {
      points[i] = 0
      characterPoints[i] = lowercase.codePointAt(i)
    }

    var node: TrieNode?
    val trie: TrieNode = this.trie
    var nodePoints: IntArray?
    for (i in 0 until wordLength) {
      node = trie
      for (j in i until wordLength) {
        node = node!!.codePoint.get(characterPoints[j])
        if (node != null) {
          nodePoints = node.points
          if (nodePoints != null) {
            var k = 0
            val nodePointsLength = nodePoints.size
            while (k < nodePointsLength) {
              points[i + k] =
                max(points[i + k].toDouble(), nodePoints[k].toDouble()).toInt()
              k++
            }
          }
        } else {
          break
        }
      }
    }

    val result: MutableList<String> = ArrayList()
    var start = 1
    for (i in 1 until wordLength - 1) {
      if (i > this.leftMin && i < wordLength - this.rightMin && points[i] % 2 > 0) {
        result.add(word.substring(start, i))
        start = i
      }
    }
    if (start < word.length - 1) {
      result.add(word.substring(start, word.length - 1))
    }
    return result
  }

  companion object {

    private val cached = HashMap<HyphenationPattern, Hyphenator>()

    /**
     * Returns a hyphenator instance for a given hypenation pattern
     *
     * @param hyphenationPattern hyphenation language pattern
     * @return newly created or cached hyphenator instance
     */
    fun getInstance(hyphenationPattern: HyphenationPattern): Hyphenator? {
      synchronized(cached) {
        if (!cached.containsKey(hyphenationPattern)) {
          cached[hyphenationPattern] = Hyphenator(hyphenationPattern)
          return cached[hyphenationPattern]
        }
        return cached[hyphenationPattern]
      }
    }

    private fun createTrie(patternObject: Map<Int, String>): TrieNode {
      var t: TrieNode
      val tree = TrieNode()

      for ((key, value) in patternObject) {
        val patterns = arrayOfNulls<String>(value.length / key)
        run {
          var i = 0
          while (i + key <= value.length) {
            patterns[i / key] = value.substring(i, i + key)
            i = i + key
          }
        }
        for (i in patterns.indices) {
          val pattern = patterns[i]
          t = tree

          for (c in 0 until pattern!!.length) {
            val chr = pattern[c]
            if (Character.isDigit(chr)) {
              continue
            }
            val codePoint = pattern.codePointAt(c)
            if (t.codePoint.get(codePoint) == null) {
              t.codePoint.put(codePoint, TrieNode())
            }
            t = t.codePoint.get(codePoint)!!
          }

          val list = IntArrayList()
          var digitStart = -1
          for (p in 0 until pattern.length) {
            if (Character.isDigit(pattern[p])) {
              if (digitStart < 0) {
                digitStart = p
              }
              if (p == pattern.length - 1) {
                // last number in the pattern
                val number = pattern.substring(digitStart, pattern.length)
                list.add(number.toInt())
              }
            } else if (digitStart >= 0) {
              val number = pattern.substring(digitStart, p)
              list.add(number.toInt())
              digitStart = -1
            } else {
              list.add(0)
            }
          }
          t.points = list.toArray()
        }
      }
      return tree
    }
  }
}

class TrieNode : Serializable {
  var codePoint: IntTrieNodeArrayMap = IntTrieNodeArrayMap()
  var points: IntArray? = null
}

class IntTrieNodeArrayMap @JvmOverloads constructor(capacity: Int = DEFAULT_INITIAL_CAPACITY) :
  Serializable {
  private var keys: IntArray
  private var values: Array<TrieNode?>

  private var size = 0

  init {
    keys = IntArray(capacity)
    values = arrayOfNulls(capacity)
  }

  private fun findIndex(key: Int): Int {
    for (i in 0 until size) {
      if (keys[i] == key) {
        return i
      }
    }
    return -1
  }

  fun put(key: Int, node: TrieNode?): TrieNode? {
    val oldIndex = findIndex(key)
    if (oldIndex >= 0) {
      val oldValue = values[oldIndex]
      values[oldIndex] = node
      return oldValue
    }
    if (size == keys.size) {
      val newKeys = IntArray(if (size == 0) DEFAULT_INITIAL_CAPACITY else size * 2)
      System.arraycopy(keys, 0, newKeys, 0, size)
      val newValues = arrayOfNulls<TrieNode>(if (size == 0) 2 else size * 2)
      System.arraycopy(values, 0, newValues, 0, size)
      keys = newKeys
      values = newValues
    }
    keys[size] = key
    values[size] = node
    size++
    return null
  }

  fun get(key: Int): TrieNode? {
    for (i in 0 until size) {
      if (keys[i] == key) return values[i]
    }
    return null
  }
}

class IntArrayList @JvmOverloads constructor(capacity: Int = DEFAULT_INITIAL_CAPACITY) {
  private var values: IntArray
  private var size = 0

  init {
    values = IntArray(capacity)
  }

  fun add(i: Int) {
    if (size == values.size) {
      val newValues = IntArray(if (size == 0) DEFAULT_INITIAL_CAPACITY else size * 2)
      System.arraycopy(values, 0, newValues, 0, size)
      values = newValues
    }
    values[size] = i
    size++
  }

  fun toArray(): IntArray {
    val result = IntArray(size)
    System.arraycopy(values, 0, result, 0, size)
    return result
  }
}

const val DEFAULT_INITIAL_CAPACITY: Int = 16
