import java.nio.file.Files
import java.nio.file.Path

fun readAllText(filePath: String): String = Files.readString(Path.of(filePath))

