import java.math.BigInteger
import java.util.*
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

private enum class Type(val risk : Int, val allowedTools: Set<Tool>) {
    ROCKY(0, setOf(Tool.GEAR, Tool.TORCH)) {
        override fun toString(): String {
            return "."
        }
    },
    NARROW(2, setOf(Tool.TORCH, Tool.NEITHER)) {
        override fun toString(): String {
            return "|"
        }
    },
    WET(1, setOf(Tool.GEAR, Tool.NEITHER)) {
        override fun toString(): String {
            return "="
        }
    };

    fun isToolAllowed(tool: Tool): Boolean {
        return allowedTools.contains(tool)
    }
}

private enum class Tool {
    TORCH,
    GEAR,
    NEITHER
}

private data class Point(val x: Int, val y: Int)

private data class Region(
    val type: Type, val erosionLevel: Int, val point: Point,
    var G: Int = 0, var H: Int = 0, var toolUsed: Tool? = null
) : Comparable<Region> {
    var parent: Region? = null

    fun moveCost(other: Region, equippedTool: Tool, goal: Point): Pair<Int, Tool> {
        return when {
            //special rules for the goal -- there is an extra cost associated with lighting the torch
            other.point == goal -> moveCostToGoal(other, equippedTool)
            // move cost is 0 when the source and destination points are the same
            this.point == other.point -> 0 to equippedTool
            // move cost is 1 when the current tool can be used
            other.type.isToolAllowed(equippedTool) -> 1 to equippedTool
            // move cost is 1 + 7 (tool change) when the tool can and may be changed to move
            mayMoveTo(other) -> 8 to type.allowedTools.first { other.type.isToolAllowed(it) }
            // move cost is infinite when you cannot move there
            else -> Int.MAX_VALUE to equippedTool
        }
    }

    // special cost calculation if the destination is the goal (torch needed ahead)
    fun moveCostToGoal(other: Region, equippedTool: Tool): Pair<Int, Tool> {
        return when {
            // we are there and we have the torch.  good to go
            this.point == other.point && equippedTool == Tool.TORCH -> 0 to Tool.TORCH
            // On the same square, it costs 7 to switch to the torch
            this.point == other.point && equippedTool != Tool.TORCH -> 7 to Tool.TORCH
            // Only cost 1 to move to the goal if the torch is equipped
            equippedTool == Tool.TORCH -> 1 to Tool.TORCH
            // If the current region lets us switch to the torch and go, it costs 8
            other.type.allowedTools.contains(Tool.TORCH) -> 8 to Tool.TORCH
            // Or if we can move there and then switch, it also costs 8
            other.type.isToolAllowed(equippedTool) -> 8 to Tool.TORCH
            // If we have to make two switches it's an earth shattering 15 cost
            mayMoveTo(other) -> 15 to Tool.TORCH
            // Otherwise we can't go here
            else -> Int.MAX_VALUE to equippedTool
        }
    }

    fun mayMoveTo(other: Region): Boolean {
        return type.allowedTools.any { tool -> other.type.isToolAllowed(tool) }
    }

    fun combinedCost(): Int {
        return G + H
    }

    override fun compareTo(other: Region): Int {
        return this.combinedCost().compareTo(other.combinedCost())
    }
}

fun main() {
    search(510, 10, 10)
}

// 999 is too high.

fun search(depth: Int, X: Int, Y: Int): Pair<Int, Int> {
    lateinit var result: Pair<Int, Int>
    val timeElapsed = measureTimeMillis {
        val target = Point(X, Y)
        val map = buildMap(depth, target)
        //printMap(map, target)
        val risk = getTotalRisk(map, target)
        println("Risk:  $risk")
        val time = aStar(map[0][0]!!, map[target.x][target.y]!!, map)
        println("Minutes:  $time")
        result = risk to time
    }
    println("Time to search:  $timeElapsed")
    return result
}

private fun reconstructPath(destination: Region): Int {
    val timeElapsed = measureTimeMillis {
        println("Reconstructing path.....")
        if (destination.toolUsed != Tool.TORCH) {
            throw Exception("That's not allowed, you need a torch!")
        }

        var current: Region? = destination
        while (current?.parent != null && current.parent != current) {
            //println("${current.point} :  ${current.G}  ${current.toolUsed} ${current.type.name}")
            current = current.parent
        }

    }
    println("Reconstructing path:  $timeElapsed")
    return destination.G
}

//591
private fun aStar(start: Region, goal: Region, map: List<MutableList<Region?>>): Int {
    val openSet = PriorityQueue<Region>()
    val closedSet = mutableMapOf<Region, Boolean>().withDefault { false }
    val gScore = mutableMapOf<Region, Int>().withDefault { 0 }
    val twinMap = mutableMapOf<Region, Region>()

    var current = start
    current.G = 0
    current.H = current.manhattanDistance(goal)
    current.parent = null
    current.toolUsed = Tool.TORCH
    openSet.add(current)

    var nodeTimes = 0L
    while (openSet.isNotEmpty()) {
        val nodeTime = measureTimeMillis {
            current = openSet.poll()

            // Quit if we found the goal
            if (current.point == goal.point) {
                println("Goal:  ${current.point}")
                println("Goal's parent:  ${current.parent?.point}")
                println("Total time:  $nodeTimes")
                return reconstructPath(current)
            }

            // Examine the neighbors
            loop@ for (neighbor: Region? in current.neighbors(map)) {
                if (neighbor == null) continue@loop
                if (closedSet[neighbor] == true) continue@loop

                val move = current.moveCost(neighbor, current.toolUsed!!, goal.point)
                val newG = gScore.getValue(current) + move.first
                val neighborG = gScore.getValue(neighbor)
                if (newG < neighborG || neighborG == 0) {
                    val successor =
                        neighbor.copy(G = newG, H = neighbor.manhattanDistance(goal), toolUsed = move.second)
                    // The start's parent should be null
                    if (successor.point != start.point) {
                        successor.parent = current
                    }
                    gScore[successor] = newG
                    val addOpenSetTime = measureTimeMillis {
                        val twin = twinMap[successor]
                        if (twin == null) {
                            openSet.add(successor)
                        } else {
                            twin.G = successor.G
                        }
                        twinMap[successor] = successor
                    }
                    if (addOpenSetTime > 0) {
                        //println("Add time:  $addOpenSetTime")
                    }
                }
            }
        }
        if (nodeTime > 0) {
            //println("Node time:  $nodeTime")
        }
        nodeTimes+=nodeTime
        closedSet[current] = true
    }
    throw Exception("Path could not be found.  :'(")
}

private fun Region.neighbors(map: List<MutableList<Region?>>): List<Region?> {
    val (x, y) = this.point
    val adjacent = listOf(x - 1 to y, x to y - 1, x + 1 to y, x to y + 1)
        .filterNot { it.first < 0 || it.second < 0 || it.first >= map.size || it.second >= map[0].size }
    return adjacent.map { map[it.first][it.second] }.filter { child -> child != null && this.mayMoveTo(child) }
}


private fun Region.manhattanDistance(other: Region): Int {
    return abs(this.point.x - other.point.x) + abs(this.point.y - other.point.y)
}

private fun Region.euclideanDistance(other: Region): Double {
    return sqrt((this.point.x - other.point.x).toDouble().pow(2) + (this.point.y - other.point.y).toDouble().pow(2))
}

private fun Region.chebyshevDistance(other: Region): Int {
    return maxOf(abs(this.point.x - other.point.x), abs(this.point.y - other.point.y))
}

private fun getTotalRisk(map: List<List<Region?>>, target: Point): Int {
    var risk = 0
    val elapsedTime = measureTimeMillis {
        risk = map.subList(0, target.x + 1).map { column ->
            column.subList(0, target.y + 1)
                .sumBy { region -> region?.type?.risk ?: 0 }
        }.sum()
    }
    println("Time to get total risk:  $elapsedTime")
    return risk
}

private fun printMap(map: List<List<Region?>>, target: Point) {
    for (y in 0 until map.size - 1) {
        for (x in 0 until map[0].size - 1) {
            when {
                x == 0 && y == 0 -> print("M")
                x == target.x && y == target.y -> print("T")
                else -> print(map[x][y]?.type)
            }
        }
        println()
    }
}

private fun buildMap(depth: Int, target: Point): List<MutableList<Region?>> {
    lateinit var map: List<MutableList<Region?>>
    val timeElapsed = measureTimeMillis {
        val maxCoordinate = maxOf(target.x, target.y) + 10 // we may have to move past the goal
        map = List(maxCoordinate + 1) { MutableList<Region?>(maxCoordinate + 1) { null } }

        for (i in 0..maxCoordinate) {
            map.forEachIndexed { x, column ->
                when (i) {
                    x -> {
                        for (y in i until maxCoordinate) {
                            column[y] = getRegion(Point(i, y), target, depth, map)
                        }
                    }
                    else -> {
                        column[i] = getRegion(Point(x, i), target, depth, map)
                    }
                }
            }
        }
    }
    println("Time to build map:  $timeElapsed")
    return map
}

private fun getRegion(point: Point, target: Point, depth: Int, map: List<MutableList<Region?>>): Region {
    val geologicIndex = getGeologicIndex(point, target, map)
    val erosionLevel = getErosionLevel(geologicIndex, depth)
    return Region(getType(erosionLevel), erosionLevel, point)
}

private fun getGeologicIndex(location: Point, target: Point, map: List<MutableList<Region?>>): BigInteger {
    return when {
        location.x == 0 && location.y == 0 -> BigInteger.ZERO
        location == target -> BigInteger.ZERO
        location.y == 0 -> location.x.toBigInteger() * 16807.toBigInteger()
        location.x == 0 -> location.y.toBigInteger() * 48271.toBigInteger()
        else -> map[location.x][location.y - 1]!!.erosionLevel.toBigInteger() * map[location.x - 1][location.y]!!.erosionLevel.toBigInteger()
    }
}

fun getErosionLevel(geologicIndex: BigInteger, depth: Int): Int {
    return (geologicIndex + depth.toBigInteger()).mod(20183.toBigInteger()).intValueExact()
}

private fun getType(erosionLevel: Int): Type {
    return when (erosionLevel % 3) {
        0 -> Type.ROCKY
        1 -> Type.WET
        2 -> Type.NARROW
        else -> throw Exception("Unexpected erosion level.")
    }
}
