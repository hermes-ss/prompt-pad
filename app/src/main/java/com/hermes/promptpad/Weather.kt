package com.hermes.promptpad

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.math.roundToInt

data class WeatherLocation(val label: String, val latitude: Double, val longitude: Double)
data class WeatherCache(
    val temperatureC: Double,
    val symbolCode: String,
    val fetchedAt: Long,
    val expiresAt: Long,
    val lastModified: String,
)

fun weatherEmoji(symbolCode: String): String {
    val code = symbolCode.lowercase()
    return when {
        code.contains("thunder") -> "⛈️"
        code.contains("snow") -> "❄️"
        code.contains("rain") || code.contains("drizzle") || code.contains("sleet") -> "🌧️"
        code.startsWith("partlycloudy") -> "⛅"
        code.startsWith("clearsky") || code.startsWith("fair") -> if (code.endsWith("_night")) "🌙" else "☀️"
        code.startsWith("cloudy") -> "☁️"
        code.startsWith("fog") -> "🌫️"
        else -> "?"
    }
}

fun weatherText(temperatureC: Double, symbolCode: String) =
    "${weatherEmoji(symbolCode)} ${temperatureC.roundToInt()}°C"

fun weatherRefreshRequired(enabled: Boolean, hasLocation: Boolean, hasCache: Boolean, expiresAt: Long, now: Long) =
    enabled && hasLocation && (!hasCache || now >= expiresAt)

object Weather {
    private const val USER_AGENT = "Prompt-Pad/1.0 https://github.com/hermes-ss/prompt-pad"
    private const val TIMEOUT_MS = 10_000

    private data class Response(
        val code: Int,
        val body: String,
        val expires: String?,
        val lastModified: String?,
    )

    suspend fun searchLocations(query: String): List<WeatherLocation> {
        if (query.isBlank()) return emptyList()
        val encoded = URLEncoder.encode(query.trim(), StandardCharsets.UTF_8.toString())
        return try {
            JSONObject(request("https://geocoding-api.open-meteo.com/v1/search?name=$encoded&count=5").body)
                .optJSONArray("results")?.let { results ->
                    List(results.length()) { index ->
                        val item = results.getJSONObject(index)
                        val label = listOf(
                            item.optString("name"), item.optString("admin1"), item.optString("country"),
                        ).filter { it.isNotBlank() }.distinct().joinToString(", ")
                        WeatherLocation(label, item.getDouble("latitude"), item.getDouble("longitude"))
                    }
                } ?: emptyList()
        } catch (_: Exception) {
            currentCoroutineContext().ensureActive()
            emptyList()
        }
    }

    suspend fun current(prefs: Prefs, now: Long = System.currentTimeMillis()): WeatherCache? {
        val cached = prefs.weatherCache()
        if (!weatherRefreshRequired(prefs.showWeather, prefs.hasWeatherLocation, cached != null, cached?.expiresAt ?: 0, now)) {
            return cached
        }
        val latitude = prefs.weatherLatitude ?: return cached
        val longitude = prefs.weatherLongitude ?: return cached
        val result = try {
            fetch(latitude, longitude, cached, now)
        } catch (_: Exception) {
            currentCoroutineContext().ensureActive()
            cached
        }
        currentCoroutineContext().ensureActive()
        if (!prefs.showWeather || prefs.weatherLatitude != latitude || prefs.weatherLongitude != longitude) {
            return prefs.weatherCache()
        }
        result?.let(prefs::saveWeatherCache)
        return result
    }

    private suspend fun fetch(latitude: Double, longitude: Double, cached: WeatherCache?, now: Long): WeatherCache? {
        val lat = String.format(Locale.US, "%.4f", latitude)
        val lon = String.format(Locale.US, "%.4f", longitude)
        val headers = buildMap {
            put("User-Agent", USER_AGENT)
            cached?.lastModified?.takeIf { it.isNotBlank() }?.let { put("If-Modified-Since", it) }
        }
        val response = request(
            "https://api.met.no/weatherapi/locationforecast/2.0/compact?lat=$lat&lon=$lon",
            headers,
        )
        return when (response.code) {
                HttpURLConnection.HTTP_NOT_MODIFIED -> cached?.copy(
                    fetchedAt = now,
                    expiresAt = expiry(response.expires, now),
                    lastModified = response.lastModified ?: cached.lastModified,
                )
                HttpURLConnection.HTTP_OK -> {
                    val root = JSONObject(response.body)
                    val data = root.getJSONObject("properties").getJSONArray("timeseries")
                        .getJSONObject(0).getJSONObject("data")
                    val temperature = data.getJSONObject("instant").getJSONObject("details")
                        .getDouble("air_temperature")
                    val next = data.optJSONObject("next_1_hours") ?: data.optJSONObject("next_6_hours")
                    val symbol = next?.getJSONObject("summary")?.getString("symbol_code") ?: "other"
                    WeatherCache(
                        temperature, symbol, now, expiry(response.expires, now),
                        response.lastModified.orEmpty(),
                    )
                }
                else -> cached
            }
    }

    private suspend fun request(url: String, headers: Map<String, String> = mapOf("User-Agent" to USER_AGENT)): Response =
        withContext(Dispatchers.IO) {
            val connection = (URI(url).toURL().openConnection() as HttpURLConnection).apply {
                connectTimeout = TIMEOUT_MS
                readTimeout = TIMEOUT_MS
                headers.forEach(::setRequestProperty)
            }
            suspendCancellableCoroutine { continuation ->
                continuation.invokeOnCancellation { connection.disconnect() }
                try {
                    val code = connection.responseCode
                    val body = if (code == HttpURLConnection.HTTP_OK) {
                        connection.inputStream.bufferedReader().use { it.readText() }
                    } else ""
                    if (continuation.isActive) continuation.resume(
                        Response(
                            code,
                            body,
                            connection.getHeaderField("Expires"),
                            connection.getHeaderField("Last-Modified"),
                        ),
                    )
                } catch (error: Throwable) {
                    if (continuation.isActive) continuation.resumeWithException(error)
                } finally {
                    connection.disconnect()
                }
            }
        }

    private fun expiry(value: String?, now: Long): Long = runCatching {
        ZonedDateTime.parse(value, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant().toEpochMilli()
    }.getOrDefault(now + 30 * 60_000L)
}
