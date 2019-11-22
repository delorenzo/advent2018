const val GRID_SERIAL_NUM = 9995

fun main (args: Array<String>) {
    val grid = MutableList (300) { MutableList (300) { 0 } }
    for (x in 0..299) {
        for (y in 0..299) {
            grid[x][y] = powerLevel(x, y)
        }
    }
    var topLeft = Array (3) {0}
    var largestPower = 0
    for (x in 0..297) {
        for (y in 0..297) {
            for (size in 1..Math.sqrt(300.0).toInt()) {
                val total = getTotal(x, y, size, grid)
                if (total > largestPower) {
                    largestPower = total
                    topLeft = arrayOf(x, y, size)
                }
            }
        }
    }
    println("The top-left square is ${topLeft[0]},${topLeft[1]},${topLeft[2]} with a total of $largestPower")
}

fun getTotal(x: Int, y: Int, size: Int, grid: MutableList<MutableList<Int>>) : Int {
    if (x + size > grid.size) return 0
    if (y + size > grid.size) return 0
    var total = 0
    for (i in 0 until size) {
        for (j in 0 until size) {
            total += grid[x+i][y+j]
        }
    }
    return total

}

fun rackId(x: Int) : Int {
    return x + 10
}

fun powerLevel(x: Int, y: Int) : Int {
    val rackId = rackId(x)
    var power = ((rackId * y) + GRID_SERIAL_NUM) * rackId
    power = power / 100 % 10
    return power - 5
}