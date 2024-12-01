import org.slf4j.LoggerFactory
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Duration.ofHours
import java.time.Duration.ofMinutes
import java.time.Duration.ofSeconds
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZoneOffset
import kotlin.io.path.absolute

val localPath: Path = Path.of("local")
private val logger = LoggerFactory.getLogger("main")

fun main(args: Array<String>) {

    logger.info("Local path is ${localPath.absolute()}")

    val cookie = Files.readString(localPath.resolve("cookie")).trim()
    val year = args.firstOrNull { it.matches(Regex("--year=\\d+")) }?.substringAfter("=")?.toInt()
        ?: 2024

    val day = args.firstOrNull { it.matches(Regex("--day=\\d+")) }?.substringAfter("=")?.toInt()
//        ?: today()
        ?: notFetchedYet()

    logger.info("Attempting to download puzzle input for year $year day $day")

    val availableDate = LocalDate.of(year, 12, day).atStartOfDay(ZoneId.of("EST", ZoneId.SHORT_IDS)).toInstant()
    if (Instant.now().isAfter(availableDate)) {
        logger.info("Input is available for ${Duration.between(availableDate, Instant.now()).readable()}")
    }

    while (availableDate.isAfter(Instant.now())) {
        val duration = Duration.between(Instant.now(), availableDate)
        val waitTime = when {
            duration > ofHours(1) -> ofMinutes(30).plusMillis(duration.toMillis() % (1000 * 60 * 30) - 1)
            duration > ofMinutes(15) -> ofMinutes(10).plusMillis(duration.toMillis() % (1000 * 60) - 1)
            duration > ofMinutes(1) -> ofMinutes(1).plusMillis(duration.toMillis() % (1000 * 60) - 1)
            duration > ofSeconds(15) -> ofSeconds(10).plusMillis(duration.toMillis() % 1000 - 1)
            duration > ofSeconds(1) -> ofSeconds(1).plusMillis(duration.toMillis() % 1000 - 1)
            else -> duration.plusMillis(100)
        }
        logger.error("Input will be available in ${duration.readable()}, waiting...")
        Thread.sleep(waitTime.toMillis())
    }

    val input = if (Files.exists(inputPath(day))) Files.readString(inputPath(day))
    else try {
        downloadInput(year, day, cookie)
    } catch (e: TooSoonException) {
        logger.error("Too soon, waiting ${e.duration.readable()} and will retry")
        Thread.sleep(e.duration.toMillis() + 1000)
        downloadInput(year, day, cookie)
    }
    logger.info("Downloaded ${input.length} characters of input")

    Files.writeString(inputPath(day), input)

    logger.info("Attempting to download puzzle text")

    val puzzleText = downloadPuzzleText(year, day, cookie)
    logger.info("Downloaded ${puzzleText.length} characters of puzzle")
    Files.writeString(puzzleTextPath(day), puzzleText)

    val stats = inputStats(input)

    logger.info("My input is $stats")

    val token = Files.readString(localPath.resolve("slack_token")).trim()
    val channelId = Files.readString(localPath.resolve("slack_channel")).trim()
    val url = urlForDay(year, day)
    createSlackThread(token, channelId, puzzleTextTitle(puzzleText), stats, url)
}

fun notFetchedYet(): Int = (1..25).first { Files.notExists(inputPath(it)) }

fun inputPath(i: Int): Path = localPath.resolve("day${i}_input.txt")
fun puzzleTextPath(day: Int): Path = localPath.resolve("day$day.html")

fun today(): Int = LocalDate.now(ZoneOffset.UTC).dayOfMonth

class TooSoonException(val duration: Duration) : RuntimeException("Too soon by ${duration.readable()}")

fun Duration.readable(): String {
    val hours = toHours()
    val days = hours / 24
    return if (days > 1) "over $days days"
    else if (hours > 5) "over $hours hours"
    else "in ${toHoursPart()} hours, ${toMinutesPart()} minutes, ${toSecondsPart()} seconds"
}
