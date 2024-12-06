import java.nio.file.Files
import java.nio.file.Path
import kotlin.time.measureTime

fun readAllText(filePath: String): String = Files.readString(Path.of(filePath))
fun String.linesWithoutLastBlanks(): List<String> = lines().dropLastWhile(String::isEmpty)

inline fun<T> go(expected: T? = null, op: ()->T) {
    measureTime {
        val result = op()
        println(result)
        if (expected!=null) check(result==expected) { "expected $expected"}
    }.also { println("took $it") }
}