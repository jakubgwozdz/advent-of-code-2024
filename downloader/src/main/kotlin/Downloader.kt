import org.slf4j.LoggerFactory
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import kotlin.jvm.optionals.getOrNull
import kotlin.use

private val logger = LoggerFactory.getLogger("downloader")

fun downloadInput(year: Int, day: Int, cookie: String): String {
    val url = "${urlForDay(year, day)}/input"
    val availableDate = LocalDate.of(year, 12, day).atStartOfDay(ZoneId.of("EST", ZoneId.SHORT_IDS)).toInstant()
    return download(url, cookie, availableDate)
}

fun downloadPuzzleText(year: Int, day: Int, cookie: String): String {
    val url = urlForDay(year, day)
    val availableDate = LocalDate.of(year, 12, day).atStartOfDay(ZoneId.of("EST", ZoneId.SHORT_IDS)).toInstant()
    return download(url, cookie, availableDate)
}

internal fun urlForDay(year: Int, day: Int): String = "https://adventofcode.com/$year/day/$day"

fun download(url: String, cookie: String, availableDate: Instant?): String {
    val request = HttpRequest.newBuilder()
        .GET().uri(URI(url)).header("Cookie", cookie)
        .build()
    val responseHandler = HttpResponse.BodyHandlers.ofString()

    return HttpClient.newHttpClient().use { client ->
        val response = client.send(request, responseHandler)
        if (response.statusCode() !in (200..299)) {
            logger.error("Got ${response.statusCode()} from ${response.uri()}")
            logger.error(response.body().lineSequence().firstOrNull())
            val responseDate = response.headers().firstValue("date").getOrNull()?.toHttpDateOrNull()
            if (responseDate != null && responseDate.isBefore(availableDate)) {
                val duration = Duration.between(responseDate, availableDate)
                logger.error("Data is not available yet, check again in ${duration.readable()}")
                throw TooSoonException(duration)
            }
            throw RuntimeException("Failed to download: ${response.statusCode()}")
        }
        response.body()
    }
}

fun String.toHttpDateOrNull(): Instant? = try {
    ZonedDateTime.parse(this, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant()
} catch (_: Exception) {
    null
}

