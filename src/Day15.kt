
import java.io.File
import java.lang.IllegalStateException
import java.lang.Math.abs
import java.util.*
import kotlin.collections.HashMap

private const val OPEN = '.'
private const val WALL = '#'
private const val GOBLIN = 'G'
private const val ELF = 'E'
data class Target(val target: Unit, val path: List<Pair<Int,Int>>, val pathLength: Int)

data class UNode(val x: Int, val y: Int, val parent: UNode?) {
    override fun equals(other: Any?): Boolean {
        if (other !is UNode) return false
        return other.x == x && other.y == y
    }
}

abstract class Unit(var x : Int, var y: Int) : Comparable<Unit> {
    override fun compareTo(other: Unit): Int {
        //top to bottom, left to right
        if (this.y < other.y) return -1
        if (this.y > other.y) return 1
        if (this.x < other.x) return -1
        if (this.x > other.x) return 1
        return 0
    }
    abstract fun getSymbol() : Char
    abstract val targetSymbol: Char

    /**
     * Return true if this unit is in range of another unit (1 movement point L->R or T->B)
     */
    private fun inRange(other: Unit): Boolean {
        return abs(other.x - this.x) + abs(other.y-this.y) <= 1
    }

    /**
     * Return the shortest path to a target.  If multiple targets are the same distance away,
     * prefer reading order (top to bottom, left to right)
     */
    private fun shortestPath(map:  List<CharArray>, units: List<Unit>): Target? {
        val queue = LinkedList<UNode>()
        val visited = HashMap<Pair<Int,Int>, Boolean>()
        val root = UNode(x, y, null)
        queue.add(root)
        val paths = mutableListOf<Target>()
        while (!queue.isEmpty()) {
            val current = queue.poll()
            if (visited[current.x to current.y] == true) continue
            visited[current.x to current.y] = true
            if (map[current.y][current.x] == targetSymbol) {
                var node = current.parent
                val path = mutableListOf<Pair<Int,Int>>()
                while (node != null && node != root) {
                    path.add(0, node.x to node.y)
                    node = node.parent
                }
                paths.add(Target(units.find { it.x == current.x && it.y == current.y }
                    ?: throw IllegalStateException("Found non-existent unit ${current.x} ${current.y}"), path, path.size))
            }
            val neighbors = map.neighborsOf(current.x, current.y, listOf(WALL, getSymbol()))
            neighbors.forEach{
                queue.add(UNode(it.first, it.second, current))
            }
        }
        val shortestLength = paths.minBy { it.pathLength }?.pathLength
        val shortestPaths = paths.filter{ it.pathLength == shortestLength }.toMutableList()
        shortestPaths.sortBy { it.target }
        return if (shortestPaths.isNotEmpty()) { shortestPaths.first() } else { null }
    }

    /**
     * Find a new target to move to
     */
    private fun findNewTarget(map: List<CharArray>, units: List<Unit>) : Target? {
        val shortestPath = shortestPath(map, units)
        shortestPath?.let {
            target = shortestPath.target
        }
        return shortestPath
    }

    /**
     * Get the adjacent target to the unit with the lowest HP
     * When multiple targets have the same (lowest) HP value, sort by reading order -
     * left to right, top to bottom
     */
    private fun getAdjacentTarget(map: List<CharArray>, units: List<Unit>) : Unit? {
        val adjacentList = map.neighborsOf(x, y, listOf(WALL, getSymbol(), OPEN))
        val unitList = getUnitsFrom(adjacentList, units)
        // Return if there is 1 or less valid unit targets
        when (unitList.size) {
            0 -> return null
            1 -> return unitList.first()
        }
        // Filter the list to the subset of units with the lowest HP value
        val lowestHp = unitList.minBy { it.hitPoints }?.hitPoints
        val lowestList = unitList.filter { it.hitPoints == lowestHp }.toMutableList()
        // Sort by reading order
        lowestList.sort()
        return lowestList.first()
    }

    /**
     * Retrieve a list of units from a list of coordinates
     */
    private fun getUnitsFrom(coordinates: List<Pair<Int, Int>>, units: List<Unit>) : List<Unit> {
        val unitList = mutableListOf<Unit>()
        coordinates.forEach { coordinate ->
            val unit = units.find { it.x == coordinate.first && it.y == coordinate.second && it.isAlive }
            unit?.let {
                unitList.add(unit)
            }
        }
        return unitList
    }

    fun move(map: List<CharArray>, units: MutableList<Unit>, i: Int) {
        if (target?.isAlive == true && target?.inRange(this) == true) return
        val target = findNewTarget(map, units)
        if (target != null && target.path.isNotEmpty()) {
            //System.out.println("Found a target: $target")
            val coordinates = target.path.first()
            map[y][x] = OPEN
            x = coordinates.first
            y = coordinates.second
            units[i] = this
            map[y][x] = getSymbol()

            //println("Moved to $x,$y")
        }
    }
    fun attack(map: List<CharArray>, units: MutableList<Unit>) : Unit? {
        target = getAdjacentTarget(map, units)
        target?.let {
            if (it.inRange(this) && it.isAlive) {
                it.damage(attackPower, map)
                //println("Did $attackPower damage.")
            }
            if (!it.isAlive) target = null
            return it
        }
        return target
    }
    private fun damage(attack: Int, map: List<CharArray>) {
        hitPoints -= attack
        if (hitPoints <= 0) {
            isAlive = false
            //println("Unit $this died.")
            map[y][x] = OPEN
        }
    }

    override fun toString(): String {
        return "Unit:  ${getSymbol()} $x,$y HP: $hitPoints"
    }

    abstract val attackPower: Int
    var hitPoints = 200
        private set
    var isAlive = true
        private set

    var target: Unit? = null
}

class Elf(x: Int, y: Int, override val attackPower: Int) : Unit(x, y) {
    override fun getSymbol(): Char {
        return ELF
    }

    override val targetSymbol: Char = GOBLIN
}

class Goblin(x: Int, y: Int) : Unit(x, y) {
    override fun getSymbol(): Char {
        return GOBLIN
    }

    override val targetSymbol: Char = ELF

    override val attackPower: Int = 3
}

fun List<CharArray>.neighborsOf(x: Int, y: Int, blacklist: List<Char>) : List<Pair<Int, Int>> {
    val neighbors = listOf(x to y-1, x-1 to y, x+1 to y, x to y+1)
    return neighbors.filter{ !  blacklist.contains(this[it.second][it.first]) }
}

fun main() {
    part1()
    part2()
}

fun part1() {
    println("Part 1:")
    game("src/input/day15-input.txt")
}

fun part2() {
    println("Part 2:")
    var attackPower = 4
    while (true) {
        try {
            val output = game("src/input/day15-input.txt", true, attackPower, false)
            println("Succeeded with attack power $attackPower and got score $output")
            return
        } catch (error: IllegalStateException) {
            println("Failed with attack power $attackPower")
            attackPower++
        }
    }
}

fun game(input: String, elvesMustLive: Boolean = false, elfAttackPower: Int = 3, printOutput: Boolean = false): Int {
    val units = mutableListOf<Unit>()
    val map = mutableListOf<CharArray>()
    var y = 0;
    File(input).forEachLine { line ->
        val trimmedLine = line.trim()
        map.add(trimmedLine.toCharArray())
        trimmedLine.forEachIndexed{ x, c ->
            if (c == GOBLIN) {
                units.add(Goblin(x, y))
            }
            else if (c == ELF) {
                units.add(Elf(x, y, elfAttackPower))
            }
        }
        y++
    }

    var rounds = 0
    var fullRound : Boolean
    if (printOutput) {
        printMap(map, units, rounds)
    }
    game@ while (true) {
        fullRound = true
        units.sort()
        units.forEachIndexed {index, unit ->
            // Second part -- enforce no elves dying
            if (elvesMustLive) {
                checkForDeadElves(units)
            }
            // Check that potential targets remain
            val potentialTargets = units.filter { it.isAlive && it.getSymbol() == unit.targetSymbol }
            if (potentialTargets.isEmpty()) {
                fullRound = false
                return@forEachIndexed
            }
            if (unit.isAlive) {
                //println("Moving unit:  $unit")
                unit.move(map, units, index)
                unit.attack(map, units)
            }
        }
        if (elvesMustLive) {
            checkForDeadElves(units)
        }
        units.removeIf{ unit -> !unit.isAlive}
        if (!fullRound) {
            break
        }
        rounds++

        if (printOutput) {
            printMap(map, units, rounds)
        }
    }
    units.removeIf{ unit -> !unit.isAlive}
    val totalHP = units.sumBy { it.hitPoints }
    val score = rounds * totalHP
    println("Outcome:  $rounds * $totalHP = $score")
    return score
}

private fun checkForDeadElves(units : List<Unit>) {
    val deadElves = units.filter { !it.isAlive && it.getSymbol() == ELF }
    if (deadElves.isNotEmpty()) {
        throw IllegalStateException("Mission failure")
    }
}

fun printMap(map: List<CharArray>, units: List<Unit>, round: Int) {
    println("After $round rounds:")
    map.forEachIndexed { index, chars ->
        print(chars)
        val unitList = units.filter { it.y == index }.sorted()
        unitList.forEach {
            print(" ${it.getSymbol()}(${it.hitPoints})")
        }
        println()
    }

    println()
}