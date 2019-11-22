import java.io.File

private const val OPEN = '.'
private const val TREE = '|'
private const val LUMBERYARD = '#'
private const val PART_ONE = 10
private const val PART_TWO = 1000000000
fun main() {
    var map = emptyList<MutableList<Char>>().toMutableList()
    File("src/input/day18-input.txt").forEachLine {
        map.add(it.trim().toCharArray().toMutableList())
    }
    printMap(map)

    for (i in 1..PART_TWO) {
        val newMap = map.copy()
        map.forEachIndexed { y, row ->
            row.forEachIndexed { x, c ->
                when (c) {
                    OPEN -> if (openToTree(x, y, map)) {
                        newMap[y][x] = TREE
                    }
                    TREE -> if (treeToLumber(x, y, map)) {
                        newMap[y][x] = LUMBERYARD
                    }
                    LUMBERYARD -> if (lumberToOpen(x, y, map)) {
                        newMap[y][x] = OPEN
                    }
                }
            }
        }
        map = newMap
        //printMap(map)
        if (i % 10000 == 0) {
            println("After $i minutes....")
            printValue(map)
        }
    }
    printValue(map)
}

fun printValue(map: List<List<Char>>) {
    val numTrees = map.sumBy { row -> row.count { it == TREE } }
    val numLumberyards = map.sumBy { row -> row.count{ it == LUMBERYARD } }
    println("The total resource value is $numTrees * $numLumberyards = ${numLumberyards * numTrees}")
}

fun MutableList<MutableList<Char>>.copy() : MutableList<MutableList<Char>> {
    val list = emptyList<MutableList<Char>>().toMutableList()
    this.forEach { row ->
        val rowCopy = row.map { it }.toMutableList()
        list.add(rowCopy)
    }
    return list
}

fun openToTree(x: Int, y: Int, map: List<List<Char>>) : Boolean {
    return getNeighbors(x, y, map).filter { it == TREE }.size >= 3
}

fun treeToLumber(x: Int, y: Int, map: List<List<Char>>) : Boolean {
    return getNeighbors(x, y, map).filter { it == LUMBERYARD }.size >= 3
}

fun lumberToOpen(x: Int, y: Int, map: List<List<Char>>) : Boolean {
    val neighbors = getNeighbors(x, y, map)
    val hasLumber = neighbors.contains(LUMBERYARD)
    val hasTree = neighbors.contains(TREE)
    return !hasLumber || !hasTree
}

fun getNeighbors(x: Int, y: Int, map: List<List<Char>>) : List<Char> {
    val indices = listOf(x - 1 to y + 1, x to y+1, x+1 to y+1, x-1 to y, x+ 1 to y, x-1 to y-1, x to y-1, x+1 to y-1)
        .filterNot { it.first < 0 || it.second < 0 || it.first >= map.size || it.second >= map.size }
    return indices.map { map[it.second][it.first] }
}

fun printMap(map: List<List<Char>>) {
    map.forEach {
        println(it.joinToString(""))
    }
    println()
}