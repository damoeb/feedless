package org.migor.feedless.browserautomation

import com.google.gson.Gson
import org.migor.feedless.generated.types.ScrapeResponse
import java.io.Serializable

class BrowserAutomationResponse(private val scrapeResponse: String) : Serializable {

    fun get(): ScrapeResponse {
        return Gson().fromJson(scrapeResponse, ScrapeResponse::class.java)
    }
}
