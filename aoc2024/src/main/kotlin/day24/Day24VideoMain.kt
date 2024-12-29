package day24

import display
import linearInterpolation
import readAllText
import java.awt.Dimension
import java.awt.Point
import java.lang.Thread.sleep
import java.util.concurrent.atomic.AtomicReference
import kotlin.concurrent.thread
import kotlin.math.PI
import kotlin.math.sin
import kotlin.random.Random

private const val MAX_ZOOM = 7.5
private const val INIT_POS = 22.0 * MAX_ZOOM //2.9
private const val MIN_SPEED = 0.001
private const val ZOOM_SPEED = 0.02
private const val SCROOL_SPEED = 0.05
private const val FIX_SPEED = 0.002
private const val FIRST_POS = 0.0
private const val LAST_POS = 45.0
private const val SLEEP = 8L

data class AnimState(
    val addersWithSwaps: List<Pair<Adder, List<String>>> = emptyList(),
    val x: Long = Random.nextLong(),
    val y: Long = Random.nextLong(),
    val z: Long = Random.nextLong(),
    val adderToFix: Adder? = null,
//    val swap: List<String> = emptyList(),
    val swapProgress: Double = 0.0,
    val position: Double = INIT_POS,
    val zoom: Double = MAX_ZOOM,
)

fun main() {
    val input = parse(readAllText("local/day24_input.txt"))

    val addersWithSwaps = setUpAdders(input).map { it to fixFullAdder(it) }
//    val x = Random.nextBits(30).toLong() shl 15 or Random.nextBits(15).toLong()
//    val y = Random.nextBits(30).toLong() shl 15 or Random.nextBits(15).toLong()
//    val z = adders.calculate(x, y)
//    val diff = (x + y) xor z
//
//    println("id:       " + (45 downTo 0).joinToString("") { "${it / 10}" })
//    println("          " + (45 downTo 0).joinToString("") { "${it % 10}" })
//    println("x:         ${x.toString(2).padStart(45, '0')}")
//    println("y:         ${y.toString(2).padStart(45, '0')}")
//    println("(x+y):    ${(x + y).toString(2).padStart(46, '0')}")
//    println("z:        ${z.toString(2).padStart(46, '0')}")
//
//    println("diff:     ${diff.toString(2).padStart(46, '0').replace("0", " ").replace("1", "^")}")


    val anim = AtomicReference(AnimState(addersWithSwaps))

    display(
        anim,
        "Day 24: Crossed Wires",
        dimension = Dimension(800, 800),
        location = Point(400, 100),
        op = Day24Video()::paintOnImage
    )

    thread {
        val animSpeed = AtomicReference(0.0)
//        sleep(10000)
        println("Zooming in")
        var zooming = true
        animSpeed.set(MIN_SPEED)
        while (zooming) {
            anim.updateAndGet { state ->
                val zoom = (state.zoom - animSpeed.get()).coerceAtLeast(1.0).let { if (it < 1.0001) 1.0 else it }
                zooming = zoom > 1
                animSpeed.set((sin((zoom - 1) * PI / (MAX_ZOOM - 1)) * ZOOM_SPEED).coerceAtLeast(MIN_SPEED / 10))
                state.copy(zoom = zoom, position = linearInterpolation(1.0, MAX_ZOOM, FIRST_POS, INIT_POS, zoom))
            }
            sleep(SLEEP)
        }

        sleep(500)

        val stops = (setOf(FIRST_POS to (null to emptyList<String>()), LAST_POS to (null to emptyList())) +
                addersWithSwaps.mapIndexed { i, v -> i.toDouble() to v }
                    .filter { (_, v) -> v.second.isNotEmpty() })
            .sortedBy { (i, _) -> i }.zipWithNext()
            .map { (a, b) -> a.first to b.first to b.second }
        stops.forEach { (range, adderWithSwap) ->
            val (start, end) = range
            println("Scrolling from $start to $end")
            var scrolling = true
            animSpeed.set(MIN_SPEED)
            while (scrolling) {
                anim.updateAndGet { state ->
                    val position = (state.position + animSpeed.get()).coerceIn(start, end)
                    scrolling = position < end
                    animSpeed.set((sin((position - start) * PI / (end - start)) * SCROOL_SPEED).coerceAtLeast(MIN_SPEED))
                    state.copy(position = position)
                }
                sleep(SLEEP)
            }
            val (current, swap) = adderWithSwap
            if (current != null && swap.isNotEmpty()) {
                println("Fixing full adder $current with swap $swap")
                var fixing = true
                animSpeed.set(FIX_SPEED)
                anim.updateAndGet { state -> state.copy(swapProgress = -0.5, adderToFix = current) }
                while (fixing) {
                    anim.updateAndGet { state ->
                        val swapProgress = (state.swapProgress + animSpeed.get()).coerceAtMost(1.5)
                        fixing = swapProgress < 1.5
//                        animSpeed.set((sin(swapProgress * PI) * FIX_SPEED).coerceAtLeast(MIN_SPEED))
                        state.copy(swapProgress = swapProgress)
                    }
                    sleep(SLEEP)
                }
                anim.updateAndGet { state ->
                    state.copy(
                        addersWithSwaps = state.addersWithSwaps.map { it ->
                            it.takeIf { it.first != current }
                                ?: (with(current) {
                                    copy(
                                        and1 = and1?.replaceOutput(swap),
                                        xor1 = xor1?.replaceOutput(swap),
                                        and2 = and2?.replaceOutput(swap),
                                        xor2 = xor2?.replaceOutput(swap),
                                        or1 = or1?.replaceOutput(swap),
                                    )
                                } to emptyList())
                        },
                        z = state.addersWithSwaps.map { it.first }.calculate(state.x, state.y),
                        swapProgress = 0.0,
                        adderToFix = null,
                    )
                }
            }

        }

        sleep(500)

        zooming = true
        animSpeed.set(MIN_SPEED / 10)
        while (zooming) {
            anim.updateAndGet { state ->
                val zoom = (state.zoom + animSpeed.get()).coerceAtMost(MAX_ZOOM)//.let { if (it < 1.0001) 1.0 else it }
                zooming = zoom < MAX_ZOOM
                animSpeed.set((sin((zoom - 1) * PI / (MAX_ZOOM - 1)) * ZOOM_SPEED).coerceAtLeast(MIN_SPEED / 10))
                state.copy(zoom = zoom, position = linearInterpolation(1.0, MAX_ZOOM, LAST_POS, INIT_POS, zoom))
            }
            sleep(SLEEP)
        }
        println("Zoomed out")
    }
    thread {
        while (true) {
            anim.updateAndGet {
                val x = Random.nextBits(30).toLong() shl 15 or Random.nextBits(15).toLong()
                val y = Random.nextBits(30).toLong() shl 15 or Random.nextBits(15).toLong()
                it.copy(x = x, y = y, z = it.addersWithSwaps.map { it.first }.calculate(x, y))
            }
            sleep(1000)
        }
    }
}

fun Pair<Gate, String>.replaceOutput(swap: List<String>) =
    if (second !in swap) this else first to swap.single { it != second }

