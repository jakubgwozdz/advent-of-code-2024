import com.slack.api.Slack
import java.nio.file.Files
import com.slack.api.methods.kotlin_extension.request.chat.blocks


fun createSlackThread(token: String, channelId: String, puzzleTitle: String, inputStats: String, url: String) {
    val client = Slack.getInstance().methods(token)
    val icon = listOf(
        ":christmas_tree:",
        ":santa:",
        ":snowman:",
        ":gift:",
        ":bell:",
        ":snowflake:",
        ":christmas-tree:",
        ":star:"
    ).random()
    val message = """
        |*${puzzleTitle}*
        |My input is $inputStats
    """.trimMargin()
    client.chatPostMessage { builder ->
        builder.channel(channelId)
            .unfurlLinks(false)
            .text(message)
            .blocks {
                section { markdownText("$icon *<$url|$puzzleTitle>* $icon") }
                section { markdownText("Input is $inputStats") }
            }
    }
}


fun main() {
    val token = Files.readString(localPath.resolve("slack_token")).trim()
    val channelId = Files.readString(localPath.resolve("slack_channel")).trim()
    val puzzleText = puzzleTextTitle(Files.readString(puzzleTextPath(1)))
    val input = inputStats(Files.readString(inputPath(1)))
    val url = "https://adventofcode.com/2024/day/1"
    createSlackThread(token, channelId, puzzleText, input, url)
}