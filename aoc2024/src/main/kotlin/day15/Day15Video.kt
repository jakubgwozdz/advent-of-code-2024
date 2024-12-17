package day15

import display
import drawStringCentered
import interpolated
import scaledInv
import useGraphics
import withAlpha
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Graphics2D
import java.awt.geom.Rectangle2D
import java.awt.image.BufferedImage
import java.util.concurrent.atomic.AtomicReference

data class AnimState(
    val before: State,
    val after: State,
    val report: Report,
    val progress: Int,
)

fun main() {
//    val input = parse(readAllText("local/day15_input.txt")).wide()
    val input = parse(largerTest).wide()
    val robotCollisions: (Pos) -> List<Pos> = { pos -> listOf(pos, pos.left) }
    val boxesCollisions: (Pos) -> List<Pos> = { pos -> listOf(pos, pos.left, pos.right) }

    val initialValue = State(input.robot, input.boxes)
    val state: AtomicReference<AnimState> = AtomicReference(
        AnimState(
            initialValue,
            initialValue,
            Report(
                input.moves.first(),
                true,
                input.robot to input.robot,
                emptyList()
            ),
            0
        )
    )
    val video = Day15Video(input)

    display(state, "Day 15: Warehouse Woes", Dimension(700, 350), op = video::paintOn)
    readln()

    input.moves.forEach { move ->
        state.updateAndGet {
            val (after, info) = makeMove(it.after, move, input.walls, robotCollisions, boxesCollisions)
            AnimState(it.after, after, info, 0)
        }
        repeat(101) { progress ->
            state.updateAndGet { it.copy(progress = progress) }
            Thread.sleep(1)
        }
    }

}

private var font = Font("Source Code Pro", 0, 36)

class Day15Video(val input: Input) {
    private val gridSize = input.walls.maxOf { it.first } + 1
    private val bgColor = Color(15, 15, 35, 100)
    private val fgColor = Color(204, 204, 204)
    private val fgColor2 = Color(0, 153, 0)//.withAlpha(200)
    private val fgColor3 = Color(255, 255, 102, 200)
    private val fgColor3a = Color.RED.withAlpha(80)

    fun paintOn(state: AnimState, image: BufferedImage) = image.useGraphics { g ->
        val scale = image.height.toFloat() / gridSize
        g.color = bgColor
        g.fillRect(0, 0, image.width, image.height)

        g.font = font
        g.color = fgColor
        input.walls.forEach { pos ->
            drawWall(g, pos, scale)
        }

        g.color = fgColor2
        (state.before.boxes - state.report.movedBoxes.map { it.first }.toSet()).forEach { pos ->
            drawBox(g, pos, pos, 0f, scale)
        }

        g.color = fgColor3
        state.report.movedBoxes.forEach { (src, dest) ->
            drawBox(g, src, dest, if (state.report.success) state.progress / 100f else 0f, scale)
        }
        state.report.robot.let { (src, dest) ->
            drawPlayer(g, src, dest, if (state.report.success) state.progress / 100f else 0f, scale)
        }
        if (!state.report.success) {
            g.color = fgColor3a
            (state.report.movedBoxes).forEach { (src, dest) ->
                val p = dest.scaledInv(scale)
                g.fill(Rectangle2D.Float(p.x-0.5f*scale, p.y-0.5f*scale, scale*2, scale))
            }
            state.report.robot.let { (src, dest) ->
                val p = dest.scaledInv(scale)
                g.fill(Rectangle2D.Float(p.x-0.5f*scale, p.y-0.5f*scale, scale, scale))
            }
        }

    }

    private fun drawWall(g: Graphics2D, pos: Pos, scale: Float) {
        g.drawStringCentered("#", pos.scaledInv(scale))
        g.drawStringCentered("#", pos.right.scaledInv(scale))
    }

    private fun drawBox(
        g: Graphics2D,
        src: Pos,
        dest: Pos,
        progress: Float,
        scale: Float
    ) {
        val pos = src.interpolated(dest, progress)
        val posRight = src.right.interpolated(dest.right, progress)
        g.drawStringCentered("[", pos.scaledInv(scale))
        g.drawStringCentered("]", posRight.scaledInv(scale))
    }

    private fun drawPlayer(
        g: Graphics2D,
        src: Pos,
        dest: Pos,
        progress: Float,
        scale: Float
    ) {
        val pos = src.interpolated(dest, progress)
        g.drawStringCentered("@", pos.scaledInv(scale))
    }
}
