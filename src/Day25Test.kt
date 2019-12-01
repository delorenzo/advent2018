import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Day25Test {
    @Test
    fun input1() {
        assertTrue { 387 < constellations("src/input/day25-input.txt")}
    }

    @Test
    fun input2() {
        assertEquals(2, constellations("src/input/day25-input2.txt"))
    }

    @Test
    fun input3() {
        assertEquals(4, constellations("src/input/day25-input3.txt"))
    }

    @Test
    fun input4() {
        assertEquals(3, constellations("src/input/day25-input4.txt"))
    }

    @Test
    fun input5() {
        assertEquals(8, constellations("src/input/day25-input5.txt"))
    }
}