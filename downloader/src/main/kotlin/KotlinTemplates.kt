import org.intellij.lang.annotations.Language
import java.nio.file.Files
import java.nio.file.Path

fun main() {

    val projectPath = "aoc2024"

    @Language("kotlin") val content = """
        package day0
        
        import go
        import linesWithoutLastBlanks
        import measure
        import readAllText
        
        typealias Input = List<String>
        
        fun part1(input: String) = input.lineSequence().filterNot(String::isBlank)
        fun part1(input: Input) = input
            .count()
        
        fun part2(input: String) = input.lineSequence().filterNot(String::isBlank)
        fun part2(input: Input) = input
            .count()
        
        fun parse(text: String) = text.linesWithoutLastBlanks()
        
        fun main() {
            val text = readAllText("local/day0_input.txt")
            val input = parse(text)
            go() { part1(input) }
            go() { part2(input) }
            TODO()
            measure(text, parse = ::parse, part1 = ::part1, part2 = ::part2)
        }

        
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
