import java.io.File
import java.util.*
import kotlin.collections.HashMap

private const val WALL = "#"
private const val OPEN = "."
private const val UNKNOWN = "?"
private const val START = "X"
private const val VERTICAL_DOOR = "-"
private const val HORIZONTAL_DOOR = "|"
private const val SIZE = 1000
private const val ORIGIN = 500
private const val NORTH = 'N'
private const val EAST = 'E'
private const val WEST = 'W'
private const val SOUTH = 'S'

private data class Room(val id : Int, val x: Int, val y: Int, val directions: MutableList<Pair<Char, Int>>) {
    fun neighbors(rooms: List<Room>) : List<Room> {
       return directions.map { rooms[it.second] }
    }

    fun updateMap(map: List<MutableList<String>>) {
        //room itself is OPEN "."
        map[x][y] = if (id == 0) "X" else OPEN
        // diagonals of a room are always WALLS "#"
        map[x-1][y-1] = WALL
        map[x-1][y+1] = WALL
        map[x+1][y-1] = WALL
        map[x+1][y+1] = WALL
        // initially set boundaries to "?"
        map[x-1][y] = UNKNOWN
        map[x+1][y] = UNKNOWN
        map[x][y+1] = UNKNOWN
        map[x][y-1] = UNKNOWN
        // set doors
        directions.forEach {
            when (it.first) {
                NORTH ->  map[x-1][y] = VERTICAL_DOOR
                SOUTH ->  map[x+1][y] = VERTICAL_DOOR
                EAST ->  map[x][y+1] = HORIZONTAL_DOOR
                WEST ->  map[x][y-1] = HORIZONTAL_DOOR
            }
        }
    }
}

private fun handleDirection(direction: Char, currentRoom: Room, rooms: MutableList<Room>,
                            map: List<MutableList<String>>) : Room {
    when (direction) {
        NORTH -> {
            var newRoom = Room(rooms.size, currentRoom.x-2, currentRoom.y, mutableListOf(SOUTH to currentRoom.id))
            val roomCheck = rooms.firstOrNull { it.x == newRoom.x && it.y == newRoom.y }
            if (roomCheck != null) {
                newRoom = roomCheck
                newRoom.directions.add(SOUTH to currentRoom.id)
            } else {
                rooms.add(newRoom)
            }
            newRoom.updateMap(map)
            currentRoom.directions.add(NORTH to newRoom.id)
            currentRoom.updateMap(map)
            return newRoom
        }
        EAST -> {
            var newRoom = Room(rooms.size, currentRoom.x, currentRoom.y+2, mutableListOf(WEST to currentRoom.id))
            val roomCheck = rooms.firstOrNull { it.x == newRoom.x && it.y == newRoom.y }
            if (roomCheck != null) {
                newRoom = roomCheck
                newRoom.directions.add(SOUTH to currentRoom.id)
            } else {
                rooms.add(newRoom)
            }
            newRoom.updateMap(map)
            currentRoom.directions.add(EAST to newRoom.id)
            currentRoom.updateMap(map)
            return newRoom
        }
        WEST -> {
            var newRoom = Room(rooms.size, currentRoom.x, currentRoom.y-2, mutableListOf(EAST to currentRoom.id))
            val roomCheck = rooms.firstOrNull { it.x == newRoom.x && it.y == newRoom.y }
            if (roomCheck != null) {
                newRoom = roomCheck
                newRoom.directions.add(SOUTH to currentRoom.id)
            } else {
                rooms.add(newRoom)
            }
            newRoom.updateMap(map)
            currentRoom.directions.add(WEST to newRoom.id)
            currentRoom.updateMap(map)
            return newRoom
        }
        SOUTH -> {
            var newRoom = Room(rooms.size, currentRoom.x+2, currentRoom.y, mutableListOf(NORTH to currentRoom.id))
            val roomCheck = rooms.firstOrNull { it.x == newRoom.x && it.y == newRoom.y }
            if (roomCheck != null) {
                newRoom = roomCheck
                newRoom.directions.add(SOUTH to currentRoom.id)
            } else {
                rooms.add(newRoom)
            }
            newRoom.updateMap(map)
            currentRoom.directions.add(SOUTH to newRoom.id)
            currentRoom.updateMap(map)
            return newRoom
        }
        else -> {
            throw Exception("Was actually $direction")
        }
    }
}

fun main() {
    val mainRegex = Regex("\\^([NEWS]+)(.*)\\\$")
    val directions = File("src/input/day20-input.txt").bufferedReader().readText()
    val match = mainRegex.matchEntire(directions)
    val map = List(SIZE) { MutableList(SIZE) { " " } }
    val origin = Room(0, ORIGIN, ORIGIN, mutableListOf())
    origin.updateMap(map)
    val rooms = mutableListOf(origin)
    map[ORIGIN][ORIGIN] = START

    match?.let{
        val initialDirections = it.groups[1]?.value
        val paths = it.groups[2]?.value
        println(initialDirections)
        println(paths)
        var currentRoom = rooms[0]
        initialDirections?.forEach { direction ->
            currentRoom = handleDirection(direction, currentRoom, rooms, map)
        }
        val branchCurrent = ArrayDeque<Room>()
        var previousCharacter = '.'
        paths?.forEach {
            when (it) {
                '|' -> currentRoom = branchCurrent.peek()
                '(' -> branchCurrent.push(currentRoom)
                ')' -> {
                    if (previousCharacter == '|') {
                        // This indicates an empty option in a list of options - in other words,
                        // that these options indicated detours.  So we want to REMOVE the previous room from
                        // the stack but leave the placement at that location
                        currentRoom = branchCurrent.pop()
                    } else {
                        branchCurrent.pop()
                    }
                }
                else -> currentRoom = handleDirection(it, currentRoom, rooms, map)
            }
            previousCharacter = it
        }
        //println(rooms)
        printMap(map)
        val shortestPathsMemo = List(rooms.size) { MutableList(rooms.size) { -2 } }
        val shortestPaths = shortestPathsToRooms(rooms, shortestPathsMemo)
        println(shortestPaths)
        println("Doors:  ${shortestPaths.max()}")
        val doorsWithAtLeast1000 = shortestPaths.filter{ path -> path >= 1000}
        println("At least 1000 doors:  ${doorsWithAtLeast1000.size}")
    } ?: println("Failure parsing.")
}

private fun shortestPathsToRooms(rooms: List<Room>, shortestPaths: List<MutableList<Int>>) : List<Int> {
    val result = mutableListOf<Int>()
    rooms.forEach { a ->
        result.add(shortestPath(rooms[0], a, shortestPaths, rooms))
    }
    return result
}

private fun shortestPath(a : Room, b: Room, shortestPaths: List<MutableList<Int>>, rooms: List<Room>) : Int {
    // A room has 0 doors between itself and itself
    if (a.id == b.id) return 0
    //If the room has already been memo-ized, return that
    if (shortestPaths[a.id][b.id] != -2) {
        return shortestPaths[a.id][b.id]
    }
    if (shortestPaths[b.id][a.id] != -2) {
        return shortestPaths[b.id][a.id]
    }
    //Start BFS
    val neighbors = ArrayDeque<Pair<Room, Int>>()
    val visited = HashMap<Room, Boolean>()
    a.neighbors(rooms).forEach {
        neighbors.add(it to 1)
    }
    while (!neighbors.isEmpty()) {
        val next = neighbors.poll()
        if (next.first == b) {
            shortestPaths[a.id][b.id] = next.second
            shortestPaths[b.id][a.id] = next.second
            //println("Path from ${a.id} to ${b.id} is ${next.second}")
            return next.second
        }
        if (visited[next.first] == true) continue
        visited[next.first] = true
        next.first.neighbors(rooms).forEach {
            neighbors.add(it to next.second+1)
        }
    }
    return -1 // no path
}

private fun printMap(map: List<List<String>> ) {
    println("Current map:   ")
    println("----------------")
    val yMin = map.indexOfFirst { it.contains(WALL) || it.contains(UNKNOWN) }
    val yMax = map.indexOfLast { it.contains(WALL) || it.contains(UNKNOWN) } + 1
    val xMin = map.filter { it.contains(WALL) || it.contains(UNKNOWN) }.map { row -> row.indexOfFirst { item -> item == UNKNOWN || item == WALL} }.min() ?: 0 -1
    val xMax = map.filter { it.contains(WALL) || it.contains(UNKNOWN) }.map { row -> row.indexOfLast { item -> item == UNKNOWN || item == WALL } }.max() ?: 0 -1


    for (y in yMin..yMax) {
        for (x in xMin..xMax) {
            print(map[y][x])
        }
        println()
    }
}