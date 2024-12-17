package day16

import display
import readAllText
import scaledInv
import useGraphics
import withAlpha
import java.awt.Color
import java.awt.Dimension
import java.awt.geom.Line2D
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicReference

data class AnimState(val running: List<List<State>> = emptyList(), val winning: List<List<State>> = emptyList())

fun main() {

    val animState: AtomicReference<AnimState> = AtomicReference(AnimState())

    val text = readAllText("local/day16_input2.txt")
//    val text = m2()
    val input = parse(text)
    val video = Day16Video(input)
    display(animState, "Day 16: Reindeer Maze", Dimension(141 * 5, 141 * 5), op = video::paintOnImage)

    readln()

    var totalCost = Int.MAX_VALUE
    val winning = mutableListOf(emptyList<State>())

    val cache = Cache(input.places)
    val start = State(input.start, Dir.R)
    val s0 = start to (0 to listOf(start))
    val visited = mutableMapOf(start to s0.second.first)
    val queue = PriorityQueue(compareBy { it.second.first }, s0)
    var c = 0

    while (queue.isNotEmpty()) {
        val (state, soFar) = queue.poll()
        val (cost, path) = soFar
        if (state.pos == input.end) {
            if (cost < totalCost) {
                totalCost = cost
                winning.clear()
            }
            winning += path
        }
        (cache.movesFor(state)).forEach { (next, dc) ->
            val nextCost = cost + dc
            if (nextCost <= totalCost && (next !in visited || visited[next]!! >= nextCost)) {
                visited[next] = nextCost
                queue.offer(next to (nextCost to path + next))
            }
        }
        animState.set(AnimState(queue.toList().map { it.second.second }, winning))
//        if (c < cost) {
            Thread.sleep(1)
//            c = cost
//        }
    }
    println("END")
}

class Day16Video(val input: Input) {
    val cache = Cache(input.places)
    val gridSize = 141 to 141
    private val bgColor = Color(15, 15, 35, 10)
    private val fgColor = Color(204, 204, 204).darker().darker().darker().withAlpha(10)
    private val fgColor2 = Color(0, 153, 0)//.withAlpha(200)
    private val fgColor3 = Color(255, 255, 102, 200)

    fun paintOnImage(state: AnimState, image: BufferedImage) = image.useGraphics { g ->
        val scale = image.height.toFloat() / gridSize.second
        g.color = bgColor
        g.fillRect(0, 0, image.width, image.height)

        g.color = fgColor
        input.places.forEach { pos ->
            Dir.entries.forEach { dir ->
                cache.movesFor(State(pos, dir)).forEach { (next, _) ->
                    val shape = Line2D.Float(pos.scaledInv(scale), next.pos.scaledInv(scale))
                    g.draw(shape)
                }
                g.draw(Line2D.Float(pos.scaledInv(scale),pos.scaledInv(scale)))
            }
        }

        g.color = fgColor2
        state.running.flatten().distinct().forEach { state ->
            g.fillOval(state.pos.scaledInv(scale).x.toInt() - 3, state.pos.scaledInv(scale).y.toInt() - 3, 7, 7)
        }
        g.color = fgColor3
        state.winning.flatten().distinct().forEach { state ->
            g.fillOval(state.pos.scaledInv(scale).x.toInt() - 4, state.pos.scaledInv(scale).y.toInt() - 4, 9, 9)
        }
    }
}
