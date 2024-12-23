import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import java.time.Instant
import kotlin.time.measureTime
import kotlin.time.toKotlinDuration

fun readAllText(filePath: String): String = Files.readString(Path.of(filePath))
fun String.linesWithoutLastBlanks(): List<String> = lines().dropLastWhile(String::isEmpty)

inline fun <T> go(expected: T? = null, desc: String = "", op: () -> T) {
    val result = op()
    println("$desc$result")
    if (expected != null) check(result == expected) { "expected $expected" }
}

fun <T, P0, P1, P2> measure(
    input: T,
    duration: Duration = Duration.ofSeconds(5),
    parse: (T) -> P0,
    part1: (P0) -> P1,
    part2: (P0) -> P2
) {
    println("warming up for ${duration.toKotlinDuration()}")
    val start = Instant.now()
    var i = 0L
    val parsed : P0 = parse(input)
    val p1: P1 = part1(parsed)
    val p2: P2 = part2(parsed)
    measureTime {
        do {
            val parse1 = parse(input)
            check(p1 == part1(parse1))
            check(p2 == part2(parse1))
            i++
        } while ((start + duration).isAfter(Instant.now()))
    }.also { println("warmed up for $it ($i times)") }
    measureTime { parse(input) }.also { println("Parsing took $it") }
    measureTime {  check(p1 == part1(parsed)) }.also { println("Part 1 took $it, result is $p1") }
    measureTime {  check(p2 == part2(parsed)) }.also { println("Part 2 took $it, result is $p2") }
}


fun <T, R> List<T>.mapParallel(op: (T) -> R) = runBlocking { map { async(Dispatchers.Default) { op(it) } }.awaitAll() }

