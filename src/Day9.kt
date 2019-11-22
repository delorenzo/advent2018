import java.math.BigInteger
import java.util.Collections.max

const val NUM_PLAYERS = 429
const val HIGHEST_MARBLE = 7090100

fun main(args: Array<String>) {
    val circle = mutableListOf<Int>()
    val scores = MutableList(NUM_PLAYERS) { BigInteger.ZERO }
    var currentPlayer = 0
    var currentIndex = 0
    var currentMarble = 0

    for (i in 0..HIGHEST_MARBLE) {
        if (i > 0 && i % (HIGHEST_MARBLE/100) == 0) {
            println("${(i * 100)/HIGHEST_MARBLE}% done")
        }
        when {
            i > 0 && (i.rem(23) == 0) -> {
                scores[currentPlayer] = scores[currentPlayer].add(BigInteger.valueOf(i.toLong()))
                val removedItemIndex = getRemoveIndex(currentIndex, circle.size)
                val removedItem = circle[removedItemIndex]
                scores[currentPlayer] = scores[currentPlayer].add(BigInteger.valueOf(removedItem.toLong()))
                circle.remove(removedItem)
                currentMarble = circle[removedItemIndex]
            }
            circle.size < 2 || (currentIndex + 2 == circle.size) -> {
                circle.add(currentMarble)
            }
            else -> {
                val newIndex = getNewIndex(currentIndex, circle.size)
                circle.add(newIndex, currentMarble)
            }
        }

        //printGraph(currentPlayer, circle, currentMarble)

        // update items
        if (currentMarble in circle) {
            currentIndex = circle.indexOf(currentMarble)
        }
        currentPlayer = (currentPlayer +1) % NUM_PLAYERS
        currentMarble = i+1
    }

    println("High score is ${max(scores)}")
}

fun getNewIndex(currentIndex: Int, circleSize: Int): Int {
    var newIndex = currentIndex
    for (i in 1..2) {
        newIndex = (newIndex + 1) % circleSize
    }
    return newIndex
}

fun getRemoveIndex(currentIndex: Int, circleSize: Int) : Int {
    var removeIndex = currentIndex
    for (i in 1..7) {
        removeIndex = (removeIndex -1)
        if (removeIndex < 0) {
            removeIndex = circleSize -1
        }
    }
    return removeIndex
}

fun printGraph(currentPlayer: Int, circle: MutableList<Int>, currentMarble: Int) {
    print("[${currentPlayer+1}]:")
    for (marble in circle) {
        when(marble) {
            currentMarble -> print(" ($marble)")
            else -> print(" $marble ")
        }
    }
    println()
}
