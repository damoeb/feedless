/*
 *  Copyright 2011 Peter Karich
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.migor.feedless.web

import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

/*
 *  Copyright 2011 Peter Karich
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
/**
 * Forked from https://github.com/mohaps/xtractor
 *
 * Changes: rewrite to kotlin, refactoring
 *
 * @author Peter Karich
 * @author damoeb
 */
object SHelper {
  const val UTF8 = "UTF-8"
  private val SPACE = Pattern.compile(" ")
  fun getLargestPossibleImageUrl(imgUrl: String): String {
    val parts = imgUrl.split("\\?").toTypedArray()
    val baseUrl = parts[0]
    val validTokens: MutableList<String> = ArrayList()
    if (parts.size > 1) {
      val queryStr = parts[1]
      val tokens = queryStr.split("\\&").toTypedArray()
      for (token in tokens) {
        val ltoken = token.lowercase(Locale.getDefault())
        if (!(ltoken.startsWith("w=") ||
            ltoken.startsWith("width=") ||
            ltoken.startsWith("h=") ||
            ltoken.startsWith("height="))
        ) {
          validTokens.add(token)
        }
      }
    }
    return if (validTokens.size > 0) {
      val sb = StringBuilder(baseUrl).append("?")
      for (i in validTokens.indices) {
        if (i > 0) {
          sb.append("&")
        }
        sb.append(validTokens[i])
      }
      sb.toString()
    } else {
      baseUrl
    }
  }

  fun replaceSpaces(urlParam: String): String {
    var url = urlParam
    if (url.isNotEmpty()) {
      url = url.trim { it <= ' ' }
      if (url.contains(" ")) {
        val spaces = SPACE.matcher(url)
        url = spaces.replaceAll("%20")
      }
    }
    return url
  }

  fun count(str: String, substring: String): Int {
    var c = 0
    val index1 = str.indexOf(substring)
    if (index1 >= 0) {
      c++
      c += count(str.substring(index1 + substring.length), substring)
    }
    return c
  }

  /**
   * Starts reading the encoding from the first valid character until an invalid encoding
   * character occurs.
   */
  fun encodingCleanup(str: String): String {
    val sb = StringBuilder()
    var startedWithCorrectString = false
    for (i in 0 until str.length) {
      val c = str[i]
      if (Character.isDigit(c) || Character.isLetter(c) || c == '-' || c == '_') {
        startedWithCorrectString = true
        sb.append(c)
        continue
      }
      if (startedWithCorrectString) break
    }
    return sb.toString().trim { it <= ' ' }
  }

  /**
   * @return the longest substring as str1.substring(result[0], result[1]);
   */
  fun getLongestSubstring(str1: String, str2: String?): String {
    val res = longestSubstring(str1, str2)
    return if (res == null || res[0] >= res[1]) "" else str1.substring(res[0], res[1])
  }

  fun longestSubstring(str1: String?, str2: String?): IntArray? {
    if (str1 == null || str1.isEmpty() || str2 == null || str2.isEmpty()) return null

    // dynamic programming => save already identical length into array
    // to understand this algo simply print identical length in every entry of the array
    // i+1, j+1 then reuses information from i,j
    // java initializes them already with 0
    val num = Array(str1.length) { IntArray(str2.length) }
    var maxlen = 0
    var lastSubstrBegin = 0
    var endIndex = 0
    for (i in 0 until str1.length) {
      for (j in 0 until str2.length) {
        if (str1[i] == str2[j]) {
          if (i == 0 || j == 0) num[i][j] = 1 else num[i][j] = 1 + num[i - 1][j - 1]
          if (num[i][j] > maxlen) {
            maxlen = num[i][j]
            // generate substring from str1 => i
            lastSubstrBegin = i - num[i][j] + 1
            endIndex = i + 1
          }
        }
      }
    }
    return intArrayOf(lastSubstrBegin, endIndex)
  }

  fun extractHost(url: String): String {
    return extractDomain(url, false)
  }

  fun extractDomain(urlParam: String, aggressive: Boolean): String {
    var url = urlParam
    if (url.startsWith("http://")) url = url.substring("http://".length) else if (url.startsWith("https://")) url =
      url.substring("https://".length)
    if (aggressive) {
      if (url.startsWith("www.")) url = url.substring("www.".length)

      // strip mobile from start
      if (url.startsWith("m.")) url = url.substring("m.".length)
    }
    val slashIndex = url.indexOf("/")
    if (slashIndex > 0) url = url.substring(0, slashIndex)
    return url
  }

  fun isAudio(url: String): Boolean {
    return url.endsWith(".mp3") || url.endsWith(".ogg") || url.endsWith(".m3u") || url.endsWith(".wav")
  }

  fun isDoc(url: String): Boolean {
    return (url.endsWith(".pdf") || url.endsWith(".ppt") || url.endsWith(".doc")
      || url.endsWith(".swf") || url.endsWith(".rtf") || url.endsWith(".xls"))
  }

  fun isPackage(url: String): Boolean {
    return (url.endsWith(".gz") || url.endsWith(".tgz") || url.endsWith(".zip")
      || url.endsWith(".rar") || url.endsWith(".deb") || url.endsWith(".rpm") || url.endsWith(".7z"))
  }

  fun isImage(url: String): Boolean {
    return (url.endsWith(".png") || url.endsWith(".jpeg") || url.endsWith(".gif")
      || url.endsWith(".jpg") || url.endsWith(".bmp") || url.endsWith(".ico") || url.endsWith(".eps"))
  }

  fun urlEncode(str: String?): String {
    return URLEncoder.encode(str, StandardCharsets.UTF_8)
  }

  fun urlDecode(str: String?): String {
    return URLDecoder.decode(str, StandardCharsets.UTF_8)
  }

  fun estimateDate(urlParam: String): String? {
    var url = urlParam
    val index = url.indexOf("://")
    if (index > 0) url = url.substring(index + 3)
    var year = -1
    var yearCounter = -1
    var month = -1
    var monthCounter = -1
    var day = -1
    val strs = url.split("/").toTypedArray()
    for (counter in strs.indices) {
      val str = strs[counter]
      if (str.length == 4) {
        year = try {
          str.toInt()
        } catch (ex: Exception) {
          continue
        }
        if (year < 1970 || year > 3000) {
          year = -1
          continue
        }
        yearCounter = counter
      } else if (str.length == 2) {
        if (monthCounter < 0 && counter == yearCounter + 1) {
          month = try {
            str.toInt()
          } catch (ex: Exception) {
            continue
          }
          if (month < 1 || month > 12) {
            month = -1
            continue
          }
          monthCounter = counter
        } else if (counter == monthCounter + 1) {
          try {
            day = str.toInt()
          } catch (ex: Exception) {
          }
          if (day < 1 || day > 31) {
            day = -1
            continue
          }
          break
        }
      }
    }
    if (year < 0) return null
    val str = StringBuilder()
    str.append(year)
    if (month < 1) return str.toString()
    str.append('/')
    if (month < 10) str.append('0')
    str.append(month)
    if (day < 1) return str.toString()
    str.append('/')
    if (day < 10) str.append('0')
    str.append(day)
    return str.toString()
  }

  fun completeDate(dateStr: String?): String? {
    if (dateStr == null) return null
    var index = dateStr.indexOf('/')
    if (index > 0) {
      index = dateStr.indexOf('/', index + 1)
      return if (index > 0) dateStr else "$dateStr/01"
    }
    return "$dateStr/01/01"
  }

  /**
   * keep in mind: simpleDateFormatter is not thread safe! call completeDate before applying this
   * formatter.
   */
  fun createDateFormatter(): SimpleDateFormat {
    return SimpleDateFormat("yyyy/MM/dd")
  }

  fun countLetters(str: String): Int {
    val len = str.length
    var chars = 0
    for (i in 0 until len) {
      if (Character.isLetter(str[i])) chars++
    }
    return chars
  }
}
