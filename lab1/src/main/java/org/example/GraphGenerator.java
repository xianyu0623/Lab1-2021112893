package org.example;

import java.io.*;
import java.util.*;
import guru.nidi.graphviz.attribute.Color;
import guru.nidi.graphviz.attribute.Label;
import guru.nidi.graphviz.engine.Format;
import guru.nidi.graphviz.engine.Graphviz;
import guru.nidi.graphviz.model.MutableGraph;
import guru.nidi.graphviz.model.MutableNode;
import static guru.nidi.graphviz.model.Factory.*;

//IDE git modify/**/
public class GraphGenerator {
    private static Map<String, Map<String, Integer>> graph = new HashMap<>();

    public static void main(String[] args) throws IOException {
        // 创建扫描器对象，从标准输入读取数据
        Scanner scanner = new Scanner(System.in);
        System.out.println("Enter the path to the text file:");
        // 读取文件路径
        String filePath = scanner.nextLine();

        // 打开文件并创建缓冲读取器
        File file = new File(filePath);
        BufferedReader reader = new BufferedReader(new FileReader(file));
        // 用于存储文件内容
        StringBuilder content = new StringBuilder();
        String line;
        // 逐行读取文件内容
        while ((line = reader.readLine()) != null) {
            content.append(line).append(" ");
        }
        // 关闭读取器
        reader.close();

        // 将文件内容处理成小写并去除非字母字符，连续空格替换为单个空格
        String processed = content.toString().toLowerCase().replaceAll("[^a-z\\s]", "").replaceAll("\\s+", " ");
        // 分割成单词数组
        String[] words = processed.split("\\s+");
        // 构建单词图
        for (int i = 0; i < words.length - 1; i++) {
            // 获取当前单词的后继单词计数映射，如果不存在则创建一个新的映射
            graph.computeIfAbsent(words[i], k -> new HashMap<>())
                    .merge(words[i + 1], 1, Integer::sum); // 合并后继单词，计数加一
        }

        boolean running = true;
        // 运行用户交互循环
        while (running) {
            System.out.println("\nChoose an option:");
            System.out.println("1: Display the graph");
            System.out.println("2: Find bridge words");
            System.out.println("3: Generate new text with bridge words");
            System.out.println("4: Calculate shortest path");
            System.out.println("5: Perform random walk");
            System.out.println("6: Exit");

            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    showDirectedGraph();
                    break;
                case "2":
                    System.out.println("Enter word1:");
                    String word1 = scanner.nextLine().trim().toLowerCase();
                    System.out.println("Enter word2:");
                    String word2 = scanner.nextLine().trim().toLowerCase();
                    String result = queryBridgeWords(word1, word2);
                    System.out.println(result);
                    break;
                case "3":
                    System.out.println("Enter the new text:");
                    String inputText = scanner.nextLine();
                    String newText = generateNewText(inputText);
                    System.out.println("Generated text with bridge words: " + newText);
                    break;
                case "4":
                    System.out.println("Enter the first word:");
                    String start = scanner.nextLine().trim().toLowerCase();
                    System.out.println("Enter the second word (or leave empty to find paths to all other words):");
                    String end = scanner.nextLine().trim().toLowerCase();
                    String pathResult = calcShortestPath(start, end.isEmpty() ? null : end);
                    System.out.println(pathResult);
                    break;
                case "5":
                    try {
                        String walkResult = randomWalk();
                        System.out.println("Random walk: " + walkResult);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                case "6":
                    running = false;
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid option, please try again.");
                    break;
            }
        }

        // 关闭扫描器
        scanner.close();
    }

    // 显示有向图的方法
    private static void showDirectedGraph() throws IOException {
        // 打印每个边及其权重
        for (String source : graph.keySet()) {
            for (String target : graph.get(source).keySet()) {
                int weight = graph.get(source).get(target);
                System.out.println(source + " -> " + target + " [weight=" + weight + "]");
            }
        }

        // 创建一个可变的图对象，设置为有向图
        MutableGraph g = mutGraph("example").setDirected(true);

        // 遍历全局图，将每个节点及其连接添加到可变图对象中
        graph.forEach((source, targets) -> {
            // 创建源节点
            MutableNode sourceNode = mutNode(source);
            // 遍历源节点的所有目标节点
            targets.forEach((target, weight) -> {
                // 创建目标节点，并添加连接，设置标签和颜色
                sourceNode.addLink(to(mutNode(target)).with(Label.of(String.valueOf(weight)), Color.rgb("addd8e")));
            });
            // 将源节点添加到图中
            g.add(sourceNode);
        });

        // 渲染图像并保存为PNG文件
        Graphviz.fromGraph(g).render(Format.PNG).toFile(new File("graph.png"));
        System.out.println("Graph image saved as graph.png");
    }


    private static String queryBridgeWords(String word1, String word2) {
        if (!graph.containsKey(word1) && !graph.containsKey(word2)) {
            return "No " + word1 + " and " + word2 + " in the graph!";
        } else if (!graph.containsKey(word1)) {
            return "No " + word1 + " in the graph!";
        } else if (!graph.containsKey(word2)) {
            return "No " + word2 + " in the graph!";
        }

        List<String> bridgeWords = new ArrayList<>();
        Map<String, Integer> targetMap = graph.get(word1);
        if (targetMap != null) {
            for (String middleWord : targetMap.keySet()) {
                if (graph.containsKey(middleWord) && graph.get(middleWord).containsKey(word2)) {
                    bridgeWords.add(middleWord);
                }
            }
        }

        if (bridgeWords.isEmpty()) {
            return "No bridge words from " + word1 + " to " + word2 + "!";
        } else {
            if (bridgeWords.size() == 1) {
                return "The bridge word from " + word1 + " to " + word2 + " is: " + bridgeWords.get(0) + ".";
            } else {
                String lastWord = bridgeWords.remove(bridgeWords.size() - 1);
                return "The bridge words from " + word1 + " to " + word2 + " are: " + String.join(", ", bridgeWords) + " and " + lastWord + ".";
            }
        }
    }
    private static String generateNewText(String inputText) {
        // 将输入文本转换为小写，去除非字母字符，并替换连续空格为单个空格
        String processedText = inputText.toLowerCase().replaceAll("[^a-z\\s]", "").replaceAll("\\s+", " ");
        String[] inputWords = processedText.split("\\s+");

        StringBuilder newText = new StringBuilder();

        Random random = new Random();
        for (int i = 0; i < inputWords.length - 1; i++) {
            String word1 = inputWords[i];
            String word2 = inputWords[i + 1];

            newText.append(word1).append(" ");

            if (graph.containsKey(word1) && graph.containsKey(word2)) {
                List<String> bridgeWords = new ArrayList<>();
                Map<String, Integer> targetMap = graph.get(word1);

                for (String middleWord : targetMap.keySet()) {
                    if (graph.containsKey(middleWord) && graph.get(middleWord).containsKey(word2)) {
                        bridgeWords.add(middleWord);
                    }
                }

                if (!bridgeWords.isEmpty()) {
                    String bridgeWord = bridgeWords.get(random.nextInt(bridgeWords.size()));
                    newText.append(bridgeWord).append(" ");
                }
            }
        }

        newText.append(inputWords[inputWords.length - 1]);
        return newText.toString();
    }
    private static String calcShortestPath(String word1, String word2) {
        if (!graph.containsKey(word1)) {
            return "No " + word1 + " in the graph!";
        }

        // 使用Dijkstra算法计算从word1到所有其他单词的最短路径
        Map<String, Integer> distances = new HashMap<>();
        Map<String, String> previousNodes = new HashMap<>();
        PriorityQueue<String> nodes = new PriorityQueue<>(Comparator.comparingInt(distances::get));

        for (String node : graph.keySet()) {
            if (node.equals(word1)) {
                distances.put(node, 0);
            } else {
                distances.put(node, Integer.MAX_VALUE);
            }
            nodes.add(node);
        }

        while (!nodes.isEmpty()) {
            String current = nodes.poll();
            Map<String, Integer> neighbors = graph.get(current);
            if (neighbors == null) continue;

            for (String neighbor : neighbors.keySet()) {
                int currentDist = distances.get(current);
                int edgeWeight = neighbors.get(neighbor);
                if (currentDist == Integer.MAX_VALUE) continue; // 防止溢出
                int newDist = currentDist + edgeWeight;
                if (newDist < distances.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    distances.put(neighbor, newDist);
                    previousNodes.put(neighbor, current);
                    nodes.remove(neighbor);
                    nodes.add(neighbor);
                }
            }
        }

        // 如果word2不为空，则计算从word1到word2的路径
        if (word2 != null && !word2.isEmpty()) {
            if(word1 == word2){
                return "Same node! Distance 0!";
            }
            if (!graph.containsKey(word2)) {
                return "No " + word2 + " in the graph!";
            }
            if (distances.getOrDefault(word2, Integer.MAX_VALUE) == Integer.MAX_VALUE) {
                return word1 + " and " + word2 + " are not reachable!";
            }

            // 构建从word1到word2的最短路径
            List<String> path = new LinkedList<>();
            for (String at = word2; at != null; at = previousNodes.get(at)) {
                path.add(at);
            }
            Collections.reverse(path);

            // 显示路径
            StringBuilder pathStr = new StringBuilder();
            pathStr.append("The shortest path from ").append(word1).append(" to ").append(word2).append(" is: ");
            for (int i = 0; i < path.size(); i++) {
                pathStr.append(path.get(i));
                if (i != path.size() - 1) {
                    pathStr.append(" -> ");
                }
            }
            pathStr.append(". The total weight is ").append(distances.get(word2)).append(".");

            // 生成图像并突出显示路径
            try {
                highlightPath(path);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return pathStr.toString();
        } else {
            // 计算从word1到所有其他单词的最短路径
            StringBuilder allPaths = new StringBuilder();
            for (String target : graph.keySet()) {
                if (!target.equals(word1)) {
                    List<String> path = new LinkedList<>();
                    for (String at = target; at != null; at = previousNodes.get(at)) {
                        path.add(at);
                    }
                    Collections.reverse(path);
                    if (path.size() == 1) continue; // 跳过不可达的单词

                    allPaths.append("The shortest path from ").append(word1).append(" to ").append(target).append(" is: ");
                    for (int i = 0; i < path.size(); i++) {
                        allPaths.append(path.get(i));
                        if (i != path.size() - 1) {
                            allPaths.append(" -> ");
                        }
                    }
                    allPaths.append(". The total weight is ").append(distances.get(target)).append(".\n");
                }
            }
            return allPaths.toString();
        }
    }

    private static void highlightPath(List<String> path) throws IOException {
        MutableGraph g = mutGraph("highlightedPath").setDirected(true);

        for (String source : graph.keySet()) {
            for (String target : graph.get(source).keySet()) {
                int weight = graph.get(source).get(target);
                MutableNode sourceNode = mutNode(source);
                MutableNode targetNode = mutNode(target);
                if (path.contains(source) && path.contains(target) && path.indexOf(target) == path.indexOf(source) + 1) {
                    sourceNode.addLink(to(targetNode).with(Label.of(String.valueOf(weight)), Color.RED));
                } else {
                    sourceNode.addLink(to(targetNode).with(Label.of(String.valueOf(weight)), Color.rgb("addd8e")));
                }
                g.add(sourceNode);
            }
        }

        // 指定保存路径，例如保存到用户目录下的图像文件夹
        String userHome = System.getProperty("user.home");
        File outputFile = new File("highlighted_path.png");

        Graphviz.fromGraph(g).render(Format.PNG).toFile(outputFile);
        System.out.println("Highlighted path image saved as " + outputFile.getAbsolutePath());
    }
    private static String randomWalk() throws IOException {
        if (graph.isEmpty()) {
            return "The graph is empty!";
        }

        Random random = new Random();
        List<String> nodes = new ArrayList<>(graph.keySet());
        String currentNode = nodes.get(random.nextInt(nodes.size()));
        Set<String> visitedEdges = new HashSet<>();
        StringBuilder walkPath = new StringBuilder();
        walkPath.append(currentNode);

        Scanner scanner = new Scanner(System.in);
        boolean running = true;

        while (running && graph.containsKey(currentNode) && !graph.get(currentNode).isEmpty()) {
            List<String> neighbors = new ArrayList<>(graph.get(currentNode).keySet());
            String nextNode = neighbors.get(random.nextInt(neighbors.size()));
            String edge = currentNode + " -> " + nextNode;

            // 检查是否已经访问过此边
            if (visitedEdges.contains(edge)) {
                break;
            }

            visitedEdges.add(edge);
            currentNode = nextNode;
            walkPath.append(" -> ").append(currentNode);

            System.out.println("Current walk: " + walkPath.toString());
            System.out.println("Press 'Enter' to continue walking or type 'stop' to end the walk:");

            String userInput = scanner.nextLine();
            if ("stop".equalsIgnoreCase(userInput)) {
                running = false;
            }
        }

        // 将遍历的路径写入文件
        String userHome = System.getProperty("user.home");
        File outputFile = new File(userHome + "/random_walk.txt");
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            writer.write(walkPath.toString());
        }

        System.out.println("Random walk saved to " + outputFile.getAbsolutePath());
        return walkPath.toString();
    }
}
