package uk.ac.tees.mad.payclock.features.timelog.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

/**
 * Reverse geocode using BigDataCloud's free reverse-geocode client.
 * Example endpoint:
 * https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=37.42159&longitude=-122.0837&localityLanguage=en
 *
 * Returns a best-effort formatted address (locality, admin region, country) or null on failure.
 * The function name is intentionally left unchanged for backward compatibility with existing callers.
 */
@Suppress("UNUSED_PARAMETER")
suspend fun reverseGeocodeWithBigDataCloud(context: Context, lat: Double, lng: Double): String? {
    return withContext(Dispatchers.IO) {
        try {
            val lang = Locale.getDefault().language.ifBlank { "en" }
            val urlStr = "https://api.bigdatacloud.net/data/reverse-geocode-client?latitude=${lat}&longitude=${lng}&localityLanguage=${lang}"
            val url = URL(urlStr)
            val conn = url.openConnection() as HttpURLConnection
            conn.requestMethod = "GET"
            conn.connectTimeout = 10_000
            conn.readTimeout = 10_000

            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val reader = BufferedReader(InputStreamReader(stream))
            val sb = StringBuilder()
            var line: String?
            while (reader.readLine().also { line = it } != null) {
                sb.append(line)
            }
            reader.close()
            conn.disconnect()

            val json = JSONObject(sb.toString())
            // BigDataCloud returns fields like: locality, principalSubdivision, countryName, city
            val locality = json.optString("locality")
            val city = json.optString("city")
            val admin = json.optString("principalSubdivision")
            val country = json.optString("countryName")

            val parts = listOf(locality, city, admin, country).map { it.trim() }.filter { it.isNotEmpty() }
            if (parts.isEmpty()) return@withContext null
            return@withContext parts.joinToString(", ")
        } catch (_: Throwable) {
            null
        }
    }
}
