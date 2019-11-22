data class Node (val metadataCount: Int, val numChildren: Int, val position: Int, var value: Int = 0,
                 val children: MutableList<Int> = mutableListOf()) {
}