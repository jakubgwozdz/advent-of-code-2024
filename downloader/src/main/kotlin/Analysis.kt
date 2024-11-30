fun inputStats(input: String): String {
    val lengths = input.lines().dropLastWhile { it.isBlank() }.map { it.length }
    val stats = when {
        lengths.isEmpty() -> "empty"
        lengths.size == 1 -> "in a single line"
        lengths.distinct().size == 1 -> {
            val maybeMap = if (input.count { it in ".#" } * 100 / input.length > 80) " (maybe a map?)" else ""
            "in ${lengths.size} lines, all ${lengths.first()} chars$maybeMap"
        }
        else -> "in ${lengths.size} lines, between ${lengths.min()}-${lengths.max()} chars each (${
            lengths.average().toInt()
        } on average)"
    }
    return "${input.length} long, $stats"
}

fun puzzleTextTitle(puzzleText: String): String {
    return puzzleText.substringAfter("<article").substringBefore("</h")
        .substringAfter("<h").substringAfter(">").substringBefore("<")
}
