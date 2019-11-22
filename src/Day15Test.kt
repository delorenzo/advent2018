import org.junit.Test
import java.lang.IllegalStateException
import kotlin.test.assertEquals

class Day15Tests() {
    @Test
    fun part1Input2() {
        assertEquals(27730, game("src/input/day15-input2.txt"))
    }

    @Test
    fun part1Input3() {
        assertEquals(36334, game("src/input/day15-input3.txt"))
    }

    @Test
    fun part1Input4() {
        assertEquals(39514, game("src/input/day15-input4.txt"))
    }

    @Test
    fun part1Input5() {
        assertEquals(27755, game("src/input/day15-input5.txt"))
    }

    @Test
    fun part1Input6() {
        assertEquals(28944, game("src/input/day15-input6.txt"))
    }

    @Test
    fun part1Input7() {
        assertEquals(18740, game("src/input/day15-input7.txt"))
    }

    @Test
    fun part1UserInput() {
        assertEquals(215168, game("src/input/day15-input8.txt"))
    }

    @Test
    fun part1Solution() {
        assertEquals(227290, game("src/input/day15-input.txt"))
    }


    @Test
    fun part2Input2() {
        assertEquals(4988, game("src/input/day15-input2.txt", true, 15))
    }

    @Test(expected = IllegalStateException::class)
    fun part2Input2FailsWithLess() {
        game("src/input/day15-input2.txt", true, 14)
    }

    @Test
    fun part2Input4() {
        assertEquals(   31284, game("src/input/day15-input4.txt", true, 4))
    }

    @Test(expected = IllegalStateException::class)
    fun part2Input4FailsWithLess() {
        game("src/input/day15-input4.txt", true, 3)
    }

    @Test
    fun part2Input5() {
        assertEquals(3478, game("src/input/day15-input5.txt", true, 15))
    }

    @Test(expected = IllegalStateException::class)
    fun part2Input5FailsWithLess() {
        game("src/input/day15-input5.txt", true, 14)
    }

    @Test
    fun part2Input6() {
        assertEquals(   6474, game("src/input/day15-input6.txt", true, 12))
    }

    @Test(expected = IllegalStateException::class)
    fun part2Input6FailsWithLess() {
        game("src/input/day15-input6.txt", true, 11)
    }


    @Test
    fun part2Input7() {
        assertEquals(1140, game("src/input/day15-input7.txt", true, 34))
    }

    @Test(expected = IllegalStateException::class)
    fun part2Input7FailsWithLess() {
        game("src/input/day15-input7.txt", true, 33)
    }

    @Test
    fun part2UserInput() {
        assertEquals(52374, game("src/input/day15-input8.txt", true, 16))
    }

    @Test
    fun part2UserInput2() {
        assertEquals(60864, game("src/input/day15-input12.txt", true, 15, true))
    }
}
