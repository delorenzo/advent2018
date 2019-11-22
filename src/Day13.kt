import java.io.File
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import kotlin.random.Random


const val INTERSECTION = '+'
const val VERTICAL = '|'
const val HORIZONTAL = '-'
const val CURVE_LEFT = '\\'
const val CURVE_RIGHT = '/'

enum class Direction {
    UP {
        override fun getMovement(): Movement {
            return Movement(0, -1)
        }
    },
    DOWN {
        override fun getMovement(): Movement {
            return Movement(0, 1)
        }
    },
    LEFT {
        override fun getMovement(): Movement {
            return Movement(-1, 0)
        }
    },
    RIGHT {
        override fun getMovement(): Movement {
            return Movement(1 , 0)
        }
    };

    abstract fun getMovement() : Movement
}

enum class Turn {
    LEFT {
        override fun getNewDirection(direction: Direction): Direction {
            return when (direction) {
                Direction.UP -> Direction.LEFT
                Direction.DOWN -> Direction.RIGHT
                Direction.LEFT -> Direction.DOWN
                Direction.RIGHT -> Direction.UP
            }
        }
    },
    STRAIGHT {
        override fun getNewDirection(direction: Direction): Direction {
            return direction
        }
    },
    RIGHT {
        override fun getNewDirection(direction: Direction): Direction {
            return when (direction) {
                Direction.UP -> Direction.RIGHT
                Direction.DOWN -> Direction.LEFT
                Direction.LEFT -> Direction.UP
                Direction.RIGHT -> Direction.DOWN
            }
        }
    };

    abstract fun getNewDirection(direction: Direction) : Direction
}

data class Movement(val x: Int, val y: Int)
data class Location(val x: Int, val y: Int) : Comparable<Location> {
    fun plus(movement: Movement) : Location {
        return Location(x + movement.x, y + movement.y)
    }

    override fun compareTo(other: Location) : Int {
        if (this.x == other.x && this.y == other.y) return 0 //equal
        if (this.y < other.y) return -1
        if (this.y > other.y) return 1
        return if (this.x < other.x) -1 else return 1
    }
}

data class Cart(var symbol: Char, var location: Location) {
    var printID: String
    var direction: Direction
       private set
    var crashed: Boolean = false
        private set

    init {
        direction = getCartDirection(symbol)
        printID = Random.nextInt(1000000000).toString()
    }

    private var numTurns : Int = 0
    private val turnOrder = listOf(Turn.LEFT, Turn.STRAIGHT, Turn.RIGHT)

    private fun nextTurn() : Turn {
        println("$numTurns: ${numTurns.rem(turnOrder.size)}")
        val result = turnOrder[numTurns % turnOrder.size]
        numTurns += 1
        return result
    }

    private fun getCartDirection(symbol: Char) : Direction {
        return when (symbol) {
            '^' -> Direction.UP
            'v' -> Direction.DOWN
            '>' -> Direction.RIGHT
            '<' -> Direction.LEFT
            else -> throw IllegalArgumentException("Not a cart.")
        }
    }

    fun setCrashed() {
        crashed = true
    }

    fun toSymbol() : String {
        return when (direction) {
            Direction.UP -> "^"
            Direction.LEFT -> "<"
            Direction.RIGHT -> ">"
            Direction.DOWN -> "v"
        }
    }

    fun doTurn() : Location {
        val turn = nextTurn()
        //println("$printID:  $turn")
        direction = turn.getNewDirection(direction)
        return location.plus(direction.getMovement())
    }

    fun doVertical() : Location {
        if (direction == Direction.LEFT || direction == Direction.RIGHT) {
            throw IllegalStateException("Cart is in an invalid state")
        }
        return location.plus(direction.getMovement())
    }

    fun doHorizontal() : Location {
        if (direction == Direction.UP || direction == Direction.DOWN) {
            throw IllegalStateException("Cart is in an invalid state")
        }
        return location.plus(direction.getMovement())
    }

    fun doCurveLeft() : Location {
        when (direction) {
            Direction.UP -> {
                direction = Direction.LEFT
                return location.plus(direction.getMovement())
            }
            Direction.DOWN -> {
                direction = Direction.RIGHT
                return location.plus(direction.getMovement())
            }
            Direction.RIGHT -> {
                direction = Direction.DOWN
                return location.plus(direction.getMovement())
            }
            Direction.LEFT -> {
                direction = Direction.UP
                return location.plus(direction.getMovement())
            }
        }
    }

    fun doCurveRight() : Location {
        when (direction) {
            Direction.UP -> {
                direction = Direction.RIGHT
                return location.plus(direction.getMovement())
            }
            Direction.DOWN -> {
                direction = Direction.LEFT
                return location.plus(direction.getMovement())
            }
            Direction.RIGHT -> {
                direction = Direction.UP
                return location.plus(direction.getMovement())
            }
            Direction.LEFT -> {
                direction = Direction.DOWN
                return location.plus(direction.getMovement())
            }
        }
    }

    fun move(map: List<String>) : Location {
        //println("Moving cart at : $location")
        return move(map[location.y][location.x])
    }

    private fun move(symbol: Char) : Location {
        //println("Moving cart on:  $symbol")
        return when (symbol) {
            INTERSECTION -> doTurn()
            VERTICAL -> doVertical()
            HORIZONTAL -> doHorizontal()
            CURVE_LEFT -> doCurveLeft()
            CURVE_RIGHT -> doCurveRight()
            else -> throw IllegalArgumentException("Symbol is not recognized:  $symbol")
        }
    }
}

fun main() {
    val carts = mutableListOf<Cart>()
    val map = mutableListOf<String>()

    var y = 0
    println("Map.....")
    File("src/input/day13-input.txt").forEachLine { line->
        var newLine = line;
        line.forEachIndexed { index, c ->
            if (isCart(c)) {
                val cart = Cart(c, Location(index, y))
                carts.add(cart)
                newLine = newLine.substring(0, index) +
                        getMapFromCart(cart) + newLine.substring(index+1)
            }
        }
        map.add(newLine)
        println(newLine)
        y++
    }
    println("Starting -----")
    printMap(carts, map)
    while (true) {
        carts.sortBy { it.location }

        //println(carts)
        //println(carts)
        carts.forEach { cart ->
            if (!cart.crashed) {

                val location = cart.move(map)
                cart.location = location
                val cartsWithLocation = carts.filter { it.location == location && !it.crashed }
                //println(cartsWithLocation)
                if (cartsWithLocation.size > 1) {
                    System.out.println("Collision at $location")
                    cartsWithLocation.forEach {
                        it.setCrashed()
                    }
                }
                if (carts.filterNot { it.crashed }.size <= 1 && !cart.crashed) {
                    println("final cart location " + cart.location)
                    return
                }
            }

            //printMap(carts, map)
            //println()
        }
    }
}

fun printMap(carts : List<Cart>, map: List<String>) {
    val mapWithCarts = map.toMutableList()

    carts.forEach{
        if (!it.crashed) {
            val line = mapWithCarts[it.location.y]
            val lineWithCart = line.substring(0, it.location.x) +
                    it.toSymbol() + line.substring(it.location.x + 1)
            mapWithCarts[it.location.y] = lineWithCart
        }
    }
    val longestLine = mapWithCarts.maxBy { it.length } ?: ""
    print(" ")
    for (i in 0..longestLine.length)
        print(i/100)
    println()
    print(" ")
    for (i in 0..longestLine.length)
        print(i / 10 % 10)
    println()
    print(" ")
    for (i in 0..longestLine.length)
        print(i % 10)
    println()
    mapWithCarts.forEachIndexed{ index, line ->
        println("$index$line")
    }
}

fun isCart(symbol: Char) : Boolean {
    return when (symbol) {
        '^' -> true
        'v' -> true
        '>' -> true
        '<' -> true
        else -> false
    }
}

fun getMapFromCart(cart: Cart) : Char {
    return when (cart.direction) {
        Direction.UP -> VERTICAL
        Direction.DOWN -> VERTICAL
        Direction.RIGHT -> HORIZONTAL
        Direction.LEFT -> HORIZONTAL
    }
}