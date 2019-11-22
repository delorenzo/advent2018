import java.math.BigInteger
import kotlin.math.abs

private enum class Type {
    ROCKY {
        override fun getRisk(): Int {
            return 0
        }

        override fun toString(): String {
            return "."
        }

        override fun allowedTools(): Set<Tool> {
            return setOf(Tool.GEAR, Tool.TORCH)
        }
    },
    NARROW {
        override fun getRisk(): Int {
           return 2
        }

        override fun toString(): String {
            return "|"
        }

        override fun allowedTools(): Set<Tool> {
            return setOf(Tool.TORCH, Tool.NEITHER)
        }
    },
    WET {
        override fun getRisk(): Int {
            return 1
        }

        override fun toString(): String {
            return "="
        }

        override fun allowedTools(): Set<Tool> {
            return setOf(Tool.GEAR, Tool.NEITHER)
        }
    };

    abstract fun getRisk(): Int
    abstract fun allowedTools(): Set<Tool>

    fun isToolAllowed(tool: Tool): Boolean {
        return allowedTools().contains(tool)
    }
}

private enum class Tool {
    TORCH,
    GEAR,
    NEITHER
}

private data class Point(val x: Int, val y: Int)

private data class Region(val type: Type, val erosionLevel: Int, val point: Point, var G: Int = 0, var H: Int = 0) {
    var parent: Region? = null
    var toolUsed: Tool? = null
    fun moveCost(other: Region, equippedTool: Tool) : Pair<Int, Tool> {
        return when {
            // move cost is 0 when the source and destination points are the same
            this.point == other.point -> 0 to equippedTool
            // move cost is 1 when the current tool can be used
            other.type.isToolAllowed(equippedTool) -> 1 to equippedTool
            // move cost is 1 + 7 (tool change) when the tool can and may be changed to move
            mayMoveTo(other) -> 8 to type.allowedTools().first { other.type.isToolAllowed(it) }
            // move cost is infinite when you cannot move there
            else -> Int.MAX_VALUE to equippedTool // u cannot go here sorry
        }
    }

    fun mayMoveTo(other: Region) : Boolean {
        return type.allowedTools().any { tool -> other.type.isToolAllowed(tool) }
    }

    fun toolRequiredToMove(other: Region, equippedTool: Tool) : Tool? {
        return type.allowedTools().firstOrNull { tool -> other.type.isToolAllowed(tool) }
    }

    fun combinedCost() : Int {
        return G + H
    }
}

fun main() {
    val depth = 510
    val target = Point(10, 10)
    val map = buildMap(depth, target)
    printMap(map, target)
    println(getTotalRisk(map, target))

    val pathToReindeer = aStar(map[0][0]!!, map[target.x][target.y]!!, map)
    val minutes = pathToReindeer.sumBy { it.G }
    println("Time:  $minutes minutes")
}

private fun aStar(start: Region, goal: Region, map: List<MutableList<Region?>>) : List<Region> {
    start.toolUsed = Tool.TORCH

    val openSet = mutableSetOf<Region>() // visited but not expanded (have not looked @ children)
    val closedSet = mutableSetOf<Region>() // visited AND expanded

    var current = start
    openSet.add(current)

    while (openSet.isNotEmpty()) {
        // Take the lowest cost value from pending and remove it
        current = openSet.minBy { it.combinedCost() }!!
        openSet.remove(current)
        // Quit if we found the goal
        if (current == goal) {
            // If the torch isn't on, turn it on -- need to find that reindeer!
            if (current.toolUsed != Tool.TORCH) {
                current.G += 7
                current.toolUsed = Tool.TORCH
            }

            val path = mutableListOf<Region>()
            var currentNode : Region? = current
            while (currentNode!!.parent != null) {
                path.add(current)
                currentNode = currentNode.parent
            }
            return path
        }
        // Examine the children
        loop@ for (child: Region? in current.children(map)) {
            val move = current.moveCost(child!!, current.toolUsed!!)
            val newG = current.G + move.first

            when {
                openSet.contains(child) -> {
                    if (child.G < newG) continue@loop
                }
                closedSet.contains(child) -> {
                    if (child.G < newG) continue@loop
                    // if the new G is better (lower) move it back to the open set
                    closedSet.remove(child)
                    openSet.add(child)
                }
                else -> {
                    // set the heuristic of the child and add it to open set
                    child.H = current.manhattanDistance(child)
                    openSet.add(child)
                }
            }

            // Update the child's G, its parent, and the tool required, and add it back to the open set
            child.G = newG
            child.parent = current
            child.toolUsed = move.second

        }
        // Add the current to closed
        closedSet.add(current)
    }
    throw Exception("Path could not be found.  :'(")
}

private fun Region.children(map: List<MutableList<Region?>>) : List<Region?> {
    val (x, y) = this.point
    val adjacent = listOf(x-1 to y, x to y-1, x+1 to y, x to y+1)
        .filterNot { it.first < 0 || it.second < 0 || it.first > map.size || it.second > map[0].size }
    return adjacent.map { map[it.first][it.second] }.filterNot { child -> !this.mayMoveTo(child!!) }
}

private fun Region.manhattanDistance(other:Region) : Int {
    return abs(this.point.x - other.point.x) + abs(this.point.y - other.point.y)
}

private fun getTotalRisk(map: List<List<Region?>>, target: Point) : Int {
    return map.subList(0, target.x+1).map { column -> column.subList(0, target.y+1)
        .sumBy { region -> region?.type?.getRisk() ?: 0 }}.sum()
}

private fun printMap(map: List<List<Region?>>, target: Point) {
    for (y in 0 .. target.y) {
        for (x in 0 .. target.x) {
            when {
                x == 0 && y == 0 -> print("M")
                x == target.x && y == target.y -> print("T")
                else -> print(map[x][y]?.type)
            }
        }
        println()
    }
}

private fun buildMap(depth: Int, target: Point) : List<MutableList<Region?>> {
    val maxCoordinate = maxOf(target.x, target.y)+1
    val map = List(maxCoordinate+1) { MutableList<Region?>(maxCoordinate+1) { null } }

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

    return map
}

private fun getRegion(point: Point, target: Point, depth: Int, map: List<MutableList<Region?>>) : Region {
    val geologicIndex = getGeologicIndex(point, target, map)
    val erosionLevel = getErosionLevel(geologicIndex, depth)
    return Region(getType(erosionLevel), erosionLevel, point)
}

private fun getGeologicIndex(location: Point, target: Point, map: List<MutableList<Region?>>) : BigInteger {
   return when {
       location.x == 0 && location.y == 0 -> BigInteger.ZERO
       location == target -> BigInteger.ZERO
       location.y == 0 -> location.x.toBigInteger() * 16807.toBigInteger()
       location.x == 0 -> location.y.toBigInteger() * 48271.toBigInteger()
       else -> map[location.x][location.y-1]!!.erosionLevel.toBigInteger() * map[location.x-1][location.y]!!.erosionLevel.toBigInteger()
    }
}

fun getErosionLevel(geologicIndex: BigInteger, depth: Int) : Int {
    return (geologicIndex + depth.toBigInteger()).mod(20183.toBigInteger()).intValueExact()
}

private fun getType(erosionLevel: Int) : Type {
    return when (erosionLevel % 3) {
        0 -> Type.ROCKY
        1 -> Type.WET
        2 -> Type.NARROW
        else -> throw Exception("Unexpected erosion level.")
    }
}