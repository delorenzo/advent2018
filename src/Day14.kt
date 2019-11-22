

fun main() {
    val GENERATIONS = 540391
    val scores = mutableListOf(3, 7)
    var indices = 0 to 1
    //printLine(scores, indices)
    var lineCount = 2
    while (GENERATIONS.toString() !in scores.takeLast(10).joinToString("")){
        scores += (scores[indices.first]+ scores[indices.second]).toString().map(Character::getNumericValue)
        val firstElf = (indices.first + scores[indices.first]+1) % scores.size
        val secondElf = (indices.second + scores[indices.second]+1) % scores.size
        indices = firstElf to secondElf
    }
    println(scores.joinToString("").indexOf(GENERATIONS.toString()))
    println(scores.subList(GENERATIONS, GENERATIONS+10).joinToString(""))
}

private fun printLine(list : MutableList<Int>, indices : Pair<Int, Int>) {
    list.forEachIndexed{ index: Int, item: Int ->
        if (index == indices.first) {
            print("($item)")
        } else if (index == indices.second) {
            print("[$item]")
        } else {
            print(" $item ")
        }
    }
    println()
}