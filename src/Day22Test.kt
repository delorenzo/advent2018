import org.junit.Test
import kotlin.test.assertEquals

class Day22Test {
    @Test
    fun exampleInput() {
        assertEquals(search(510, 10, 10), 114 to 45)
    }

    @Test
    fun exampleInput2() {
        assertEquals(search(510, 29, 9), 271 to 65)
    }

    @Test
    fun exampleInput3() {
        assertEquals(search(11820, 7, 782), 6318 to 1075)
    }

    @Test
    fun realInput() {
        assertEquals(search(11394, 7, 701), 5637 to 969)
    }
}