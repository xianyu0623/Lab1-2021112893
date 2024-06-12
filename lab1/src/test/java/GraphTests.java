import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import java.util.HashMap;
import java.util.Map;

public class GraphTests {
    private static Map<String, Map<String, Integer>> graph;

    @Before
    public void setUp() {
        graph = new HashMap<>(); // Assuming Graph is a simple Map for this example
        addEdge("apple", "orange");
        addEdge("orange", "banana");
        addEdge("cat", "dog");
        addEdge("summer", "fall");
        addEdge("fall", "winter");
        addEdge("summer", "spring");
        addEdge("spring", "winter");
        addEdge("tree", "house");
        addEdge("start", "end");
        addNode("echo");
    }

    private void addEdge(String source, String target) {
        graph.putIfAbsent(source, new HashMap<>());
        graph.putIfAbsent(target, new HashMap<>());
        graph.get(source).put(target, 1); // Assume edge weight is 1 for simplicity
    }

    private void addNode(String node) {
        graph.putIfAbsent(node, new HashMap<>());
    }

    private String queryBridgeWords(String word1, String word2) {
        if (!graph.containsKey(word1) || !graph.containsKey(word2)) {
            return "No " + word1 + " or " + word2 + " in the graph!";
        }

        Map<String, Integer> targets = graph.get(word1);
        StringBuilder bridgeWords = new StringBuilder();
        for (String target : targets.keySet()) {
            if (graph.containsKey(target) && graph.get(target).containsKey(word2)) {
                if (bridgeWords.length() > 0) {
                    bridgeWords.append(", ");
                }
                bridgeWords.append(target);
            }
        }

        if (bridgeWords.length() == 0) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        } else {
            return "The bridge words from " + word1 + " to " + word2 + " are: " + bridgeWords.toString() + ".";
        }
    }


@Test
public void testBridgeWordExists() {
    String result = queryBridgeWords("apple", "banana");
    assertEquals("The bridge words from apple to banana are: orange.", result);
}
@Test
public void testNoBridgeWord() {
    String result = queryBridgeWords("cat", "dog");
    assertEquals("No bridge words from cat to dog!", result);
}

@Test
public void testWordNotInGraph() {
    String result = queryBridgeWords("cat", "dragon");
    assertEquals("No cat or dragon in the graph!", result);
}

@Test
public void testBothWordsNotInGraph() {
    String result = queryBridgeWords("ghost", "phantom");
    assertEquals("No ghost or phantom in the graph!", result);
}

@Test
public void testIdenticalWords() {
    String result = queryBridgeWords("echo", "echo");
    assertEquals("No bridge words from echo to echo!", result);
}

@Test
public void testMultipleBridgeWords() {
    String result = queryBridgeWords("summer", "winter");
    assertEquals("The bridge words from summer to winter are: spring, fall.", result);
}

@Test
public void testDirectConnectionNoBridge() {
    String result = queryBridgeWords("tree", "house");
    assertEquals("No bridge words from tree to house!", result);
}

@Test
public void testOnlyTwoConnectedNodes() {
    String result = queryBridgeWords("start", "end");
    assertEquals("No bridge words from start to end!", result);
}
}
