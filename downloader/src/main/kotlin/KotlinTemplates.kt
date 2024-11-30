import org.intellij.lang.annotations.Language
import java.nio.file.Files
import java.nio.file.Path

fun main() {

    val projectPath = "aoc2024"

    @Language("kotlin") val content = """
        package day0

        import readAllText

        fun main() {
            println(part1(readAllText("local/day0_input.txt")))
            println(part2(readAllText("local/day0_input.txt")))
        }

        fun part1(input: String) = input.lineSequence().filterNot(String::isBlank)
            .count()

        fun part2(input: String) = input.lineSequence().filterNot(String::isBlank)
            .count()

    """.trimIndent()

    (1..25).forEach { day ->
        val p = Path.of(projectPath, "src/main/kotlin/day${day}/Day${day}.kt")
        if (!Files.exists(p)) {
            println(p)
            Files.createDirectories(p.parent)
            Files.writeString(p, content.replace("day0", "day$day"))
        }
    }

}