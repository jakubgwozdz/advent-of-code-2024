package day4

import readAllText
import grid.*

fun main() {
    val input = readAllText("local/day4_input.txt").lines()
    println(part1(input))
    println(part2(input))
}

fun part1(grid: Grid) = grid.findAll('X')
    .flatMap { (horizontal + vertical + diagonals).map { m -> it to m } }
    .mapNotNull { (p, m) -> (p + m).takeIf { grid[it] == 'M' }?.let { it to m } }
    .mapNotNull { (p, m) -> (p + m).takeIf { grid[it] == 'A' }?.let { it to m } }
    .mapNotNull { (p, m) -> (p + m).takeIf { grid[it] == 'S' }?.let { it to m } }
    .count()

fun part2(grid: Grid): Int = grid.findAll('A').count { p ->
    diagonals.count { m -> grid[p - m] == 'M' && grid[p + m] == 'S' } == 2
}

