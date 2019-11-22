import java.io.BufferedReader
import java.io.File
import java.util.*

var metadataTotal = 0

fun main(args: Array<String>) {
    val input = Scanner(File("src/input/day8-input2.txt"))
    val numChildren = input.nextInt()
    val metadataCount = input.nextInt()
    val root = Node(metadataCount, numChildren, 0)
    val graph = mutableListOf(root)
    traverseChildren(root, graph, input)

    val rootValue = graph[0].value
    println("Root's value is $rootValue")
    println("Metadata total value is $metadataTotal")
    print(graph)
    input.close()
}

fun traverseChildren(currentNode: Node, graph: MutableList<Node>, input: Scanner) {
    println("Children:  ${currentNode.numChildren}  metadataCount:  ${currentNode.metadataCount}")
    println("Traversing children for node.....")
    for (i in 1..currentNode.numChildren) {
        val childChildren = input.nextInt()
        val childMetadata = input.nextInt()
        val childNode = Node(   childMetadata, childChildren, graph.size)
        graph.add(childNode)
        currentNode.children.add(graph.size)
        traverseChildren(childNode, graph, input)
    }
    println("--")
    getMetadata(currentNode, input, graph)
}

fun getMetadata(currentNode: Node, input: Scanner, graph: MutableList<Node>) {
    println("Getting ${currentNode.metadataCount} metadata for node with ${currentNode.numChildren} children")
    var total = 0
    for (i in 1..currentNode.metadataCount) {
        val metadata = input.nextInt()
        metadataTotal += metadata
        println("Metadata:  $metadata")
        when {
         metadata == 0 -> println("Skipping value of 0")
         currentNode.numChildren == 0 -> total += metadata
         metadata > currentNode.numChildren -> println("$metadata is higher than ${currentNode.numChildren}")
         else -> {
             val childNode = graph[currentNode.children[metadata-1]-1]
             println("Child $metadata's value is ${childNode.value}")
             total += childNode.value
         }
        }
    }
    println("Node ${currentNode.position}'s value is $total")
    println("--")
    graph[currentNode.position].value = total
}